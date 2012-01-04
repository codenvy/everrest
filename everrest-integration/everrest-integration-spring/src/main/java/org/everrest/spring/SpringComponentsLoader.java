/**
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.everrest.spring;

import org.everrest.core.ApplicationContext;
import org.everrest.core.ComponentLifecycleScope;
import org.everrest.core.DependencySupplier;
import org.everrest.core.Filter;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.InitialProperties;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResourceBinder;
import org.everrest.core.ResponseFilter;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.FileCollectorDestroyer;
import org.everrest.core.impl.FilterDescriptorImpl;
import org.everrest.core.impl.provider.ProviderDescriptorImpl;
import org.everrest.core.impl.resource.AbstractResourceDescriptorImpl;
import org.everrest.core.impl.resource.ResourceDescriptorValidator;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.provider.ProviderDescriptor;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.servlet.EverrestApplication;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * This loader registers any bean annotated with &#64;Path, &#64;Provider or &#64;Filter in the EverRest framework.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class SpringComponentsLoader implements BeanFactoryPostProcessor, HandlerMapping
{
   private static abstract class Destroyer implements org.springframework.context.Lifecycle
   {
      private final AtomicBoolean started = new AtomicBoolean(true);

      @Override
      public final void start()
      {
      }

      @Override
      public final boolean isRunning()
      {
         return started.get();
      }
   }

   private static final class SpringEverrestProcessorDestroyer extends Destroyer
   {
      private final EverrestProcessor processor;

      private SpringEverrestProcessorDestroyer(EverrestProcessor processor)
      {
         this.processor = processor;
      }

      @Override
      public void stop()
      {
         processor.stop();
      }
   }

   private static final class SpringFileCollectorDestroyer extends Destroyer
   {
      private final FileCollectorDestroyer fileCollectorDestroyer;

      public SpringFileCollectorDestroyer(FileCollectorDestroyer fileCollectorDestroyer)
      {
         this.fileCollectorDestroyer = fileCollectorDestroyer;
      }

      @Override
      public void stop()
      {
         fileCollectorDestroyer.stopFileCollector();
      }
   }

   protected ResourceBinder resources;
   protected ApplicationProviderBinder providers;
   protected EverrestProcessor processor;
   protected EverrestConfiguration configuration;

   public SpringComponentsLoader(ResourceBinder resources, ApplicationProviderBinder providers,
      DependencySupplier dependencies)
   {
      this(resources, providers, new EverrestConfiguration(), dependencies);
   }

   public SpringComponentsLoader(ResourceBinder resources, ApplicationProviderBinder providers,
      EverrestConfiguration configuration, DependencySupplier dependencies)
   {
      this.resources = resources;
      this.providers = providers;
      this.configuration = configuration;
      this.processor = new EverrestProcessor(resources, providers, dependencies, configuration, null);
   }

   protected SpringComponentsLoader()
   {
   }

   /**
    * @see org.springframework.web.servlet.HandlerMapping#getHandler(javax.servlet.http.HttpServletRequest)
    */
   @Override
   public HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception
   {
      return new HandlerExecutionChain(processor);
   }

   /**
    * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(
    * org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
    */
   @Override
   public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
   {
      beanFactory.registerSingleton("org.everrest.lifecycle.SpringEverrestProcessorDestroyer", //
         new SpringEverrestProcessorDestroyer(processor));
      beanFactory.registerSingleton("org.everrest.lifecycle.SpringFileCollectorDestroyer", //
         new SpringFileCollectorDestroyer(makeFileCollectorDestroyer()));
      processor.addApplication(makeEverrestApplication());

      ResourceDescriptorValidator rdv = ResourceDescriptorValidator.getInstance();
      addAutowiredDependencies(beanFactory);
      for (String beanName : beanFactory.getBeanDefinitionNames())
      {
         Class<?> beanClass = beanFactory.getType(beanName);
         BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
         // Avoid unnecessary for bean with life cycle other then 'prototype' (creation new instance for each call).
         ComponentLifecycleScope lifeCycle =
            beanDefinition.isPrototype() ? ComponentLifecycleScope.PER_REQUEST : ComponentLifecycleScope.SINGLETON;

         if (beanClass.getAnnotation(Provider.class) != null)
         {
            ProviderDescriptor pDescriptor = new ProviderDescriptorImpl(beanClass, lifeCycle);
            pDescriptor.accept(rdv);

            if (ContextResolver.class.isAssignableFrom(beanClass))
               providers.addContextResolver(new SpringObjectFactory<ProviderDescriptor>(pDescriptor, beanName,
                  beanFactory));

            if (ExceptionMapper.class.isAssignableFrom(beanClass))
               providers.addExceptionMapper(new SpringObjectFactory<ProviderDescriptor>(pDescriptor, beanName,
                  beanFactory));

            if (MessageBodyReader.class.isAssignableFrom(beanClass))
               providers.addMessageBodyReader(new SpringObjectFactory<ProviderDescriptor>(pDescriptor, beanName,
                  beanFactory));

            if (MessageBodyWriter.class.isAssignableFrom(beanClass))
               providers.addMessageBodyWriter(new SpringObjectFactory<ProviderDescriptor>(pDescriptor, beanName,
                  beanFactory));
         }
         else if (beanClass.getAnnotation(Filter.class) != null)
         {
            FilterDescriptorImpl fDescriptor = new FilterDescriptorImpl(beanClass, lifeCycle);
            fDescriptor.accept(rdv);

            if (MethodInvokerFilter.class.isAssignableFrom(beanClass))
               providers.addMethodInvokerFilter(new SpringObjectFactory<FilterDescriptor>(fDescriptor, beanName,
                  beanFactory));

            if (RequestFilter.class.isAssignableFrom(beanClass))
               providers
                  .addRequestFilter(new SpringObjectFactory<FilterDescriptor>(fDescriptor, beanName, beanFactory));

            if (ResponseFilter.class.isAssignableFrom(beanClass))
               providers
                  .addResponseFilter(new SpringObjectFactory<FilterDescriptor>(fDescriptor, beanName, beanFactory));
         }
         else if (beanClass.getAnnotation(Path.class) != null)
         {
            AbstractResourceDescriptor rDescriptor = new AbstractResourceDescriptorImpl(beanClass, lifeCycle);
            rDescriptor.accept(rdv);
            resources.addResource(new SpringObjectFactory<AbstractResourceDescriptor>(rDescriptor, beanName,
               beanFactory));
         }
      }
   }

   protected FileCollectorDestroyer makeFileCollectorDestroyer()
   {
      return new FileCollectorDestroyer();
   }

   protected EverrestApplication makeEverrestApplication()
   {
      return new EverrestApplication(configuration);
   }

   /**
    * Add binding for HttpHeaders, InitialProperties, Request, SecurityContext, UriInfo. All this types will be
    * supported for injection in constructor or fields of component of Spring IoC container.
    * 
    * @param beanFactory bean factory
    * @see org.springframework.beans.factory.annotation.Autowired
    */
   protected void addAutowiredDependencies(ConfigurableListableBeanFactory beanFactory)
   {
      beanFactory.registerResolvableDependency(HttpHeaders.class, new ObjectFactory<HttpHeaders>()
      {
         public HttpHeaders getObject()
         {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null)
               throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
            return context.getHttpHeaders();
         }
      });
      beanFactory.registerResolvableDependency(InitialProperties.class, new ObjectFactory<InitialProperties>()
      {
         public InitialProperties getObject()
         {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null)
               throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
            return context.getInitialProperties();
         }
      });
      beanFactory.registerResolvableDependency(Request.class, new ObjectFactory<Request>()
      {
         public Request getObject()
         {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null)
               throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
            return context.getRequest();
         }
      });
      beanFactory.registerResolvableDependency(SecurityContext.class, new ObjectFactory<SecurityContext>()
      {
         public SecurityContext getObject()
         {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null)
               throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
            return context.getSecurityContext();
         }
      });
      beanFactory.registerResolvableDependency(UriInfo.class, new ObjectFactory<UriInfo>()
      {
         public UriInfo getObject()
         {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null)
               throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
            return context.getUriInfo();
         }
      });
   }

   protected ResourceBinder getResources()
   {
      return resources;
   }

   protected ApplicationProviderBinder getProviders()
   {
      return providers;
   }

   protected EverrestProcessor getProcessor()
   {
      return processor;
   }
}