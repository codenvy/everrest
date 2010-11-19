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
import org.everrest.core.Filter;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.InitialProperties;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResourceBinder;
import org.everrest.core.ResponseFilter;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.FilterDescriptorImpl;
import org.everrest.core.impl.provider.ProviderDescriptorImpl;
import org.everrest.core.impl.resource.AbstractResourceDescriptorImpl;
import org.everrest.core.impl.resource.ResourceDescriptorValidator;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.provider.ProviderDescriptor;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

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
 * This loader registers any bean annotated with &#64;Path, &#64;Provider or
 * &#64;Filter in the EverRest framework.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class SpringComponentsLoader implements BeanFactoryPostProcessor
{

   protected ResourceBinder resources;

   protected ApplicationProviderBinder providers;

   protected SpringComponentsLoader()
   {
   }

   public SpringComponentsLoader(ResourceBinder resources, ApplicationProviderBinder providers)
   {
      this.resources = resources;
      this.providers = providers;
   }

   public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
   {
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
               getProviders().addContextResolver(new SpringObjectFactory<ProviderDescriptor>(pDescriptor, beanName, beanFactory));

            if (ExceptionMapper.class.isAssignableFrom(beanClass))
               getProviders().addExceptionMapper(new SpringObjectFactory<ProviderDescriptor>(pDescriptor, beanName, beanFactory));

            if (MessageBodyReader.class.isAssignableFrom(beanClass))
               getProviders().addMessageBodyReader(new SpringObjectFactory<ProviderDescriptor>(pDescriptor, beanName, beanFactory));

            if (MessageBodyWriter.class.isAssignableFrom(beanClass))
               getProviders().addMessageBodyWriter(new SpringObjectFactory<ProviderDescriptor>(pDescriptor, beanName, beanFactory));
         }
         else if (beanClass.getAnnotation(Filter.class) != null)
         {
            FilterDescriptorImpl fDescriptor = new FilterDescriptorImpl(beanClass, lifeCycle);
            fDescriptor.accept(rdv);

            if (MethodInvokerFilter.class.isAssignableFrom(beanClass))
               getProviders().addMethodInvokerFilter(new SpringObjectFactory<FilterDescriptor>(fDescriptor, beanName, beanFactory));

            if (RequestFilter.class.isAssignableFrom(beanClass))
               getProviders().addRequestFilter(new SpringObjectFactory<FilterDescriptor>(fDescriptor, beanName, beanFactory));

            if (ResponseFilter.class.isAssignableFrom(beanClass))
               getProviders().addResponseFilter(new SpringObjectFactory<FilterDescriptor>(fDescriptor, beanName, beanFactory));
         }
         else if (beanClass.getAnnotation(Path.class) != null)
         {
            AbstractResourceDescriptor rDescriptor = new AbstractResourceDescriptorImpl(beanClass, lifeCycle);
            rDescriptor.accept(rdv);
            getResources().addResource(new SpringObjectFactory<AbstractResourceDescriptor>(rDescriptor, beanName, beanFactory));
         }
      }
   }

   /**
    * Add binding for HttpHeaders, InitialProperties, Request, SecurityContext,
    * UriInfo. All this types will be supported for injection in constructor or
    * fields of component of Spring IoC container.
    *
    * @param beanFactory bean factory
    * @see org.springframework.beans.factory.annotation.Autowired
    */
   protected void addAutowiredDependencies(ConfigurableListableBeanFactory beanFactory)
   {
      beanFactory.registerResolvableDependency(HttpHeaders.class, new ObjectFactory<HttpHeaders>() {
         public HttpHeaders getObject() {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null)
               throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
            return context.getHttpHeaders();
         }
      });
      beanFactory.registerResolvableDependency(InitialProperties.class, new ObjectFactory<InitialProperties>() {
         public InitialProperties getObject() {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null)
               throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
            return context.getInitialProperties();
         }
      });
      beanFactory.registerResolvableDependency(Request.class, new ObjectFactory<Request>() {
         public Request getObject() {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null)
               throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
            return context.getRequest();
         }
      });
      beanFactory.registerResolvableDependency(SecurityContext.class, new ObjectFactory<SecurityContext>() {
         public SecurityContext getObject() {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null)
               throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
            return context.getSecurityContext();
         }
      });
      beanFactory.registerResolvableDependency(UriInfo.class, new ObjectFactory<UriInfo>() {
         public UriInfo getObject() {
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

}