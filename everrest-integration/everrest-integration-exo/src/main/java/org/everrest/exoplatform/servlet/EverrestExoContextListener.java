/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.everrest.exoplatform.servlet;

import org.everrest.core.ComponentLifecycleScope;
import org.everrest.core.DependencySupplier;
import org.everrest.core.Filter;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResourceBinder;
import org.everrest.core.ResponseFilter;
import org.everrest.core.SingletonObjectFactory;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.FilterDescriptorImpl;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.impl.provider.ProviderDescriptorImpl;
import org.everrest.core.impl.resource.AbstractResourceDescriptorImpl;
import org.everrest.core.impl.resource.ResourceDescriptorValidator;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.provider.ProviderDescriptor;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.servlet.EverrestServletContextInitializer;
import org.everrest.exoplatform.ExoDependencySupplier;
import org.exoplatform.container.ExoContainer;
import org.picocontainer.ComponentAdapter;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public abstract class EverrestExoContextListener implements ServletContextListener
{
   /**
    * Default EverrestExoContextListener implementation. It gets application's
    * FQN from context-param <i>javax.ws.rs.Application</i> and instantiate it.
    * If such parameter is not specified then scan (if scanning is enabled) web
    * application's folders WEB-INF/classes and WEB-INF/lib for classes which
    * contains JAX-RS annotations. Interesting for three annotations
    * {@link Path}, {@link Provider} and {@link Filter}. Scanning of JAX-RS
    * components is managed by contex-param <i>org.everrest.scan.components</i>.
    * This parameter must be <i>true</i> to enable scanning.
    */
   public static class DefaultListener extends EverrestExoContextListener
   {
      /**
       * @see org.everrest.exoplatform.servlet.EverrestExoContextListener#getContainer(javax.servlet.ServletContext)
       */
      @Override
      protected ExoContainer getContainer(ServletContext servletContext)
      {
         return null;
      }
   }

   /* ================================================================================ */
   
   protected EverrestServletContextInitializer everrestInitializer;

   protected ResourceBinderImpl resources;

   protected ApplicationProviderBinder providers;

   protected EverrestProcessor processor;

   /**
    * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
    */
   @Override
   public final void contextInitialized(ServletContextEvent servletContextEvent)
   {
      ServletContext servletContext = servletContextEvent.getServletContext();
      this.everrestInitializer = new EverrestServletContextInitializer(servletContext);
      this.resources = new ResourceBinderImpl();
      this.providers = new ApplicationProviderBinder();
      DependencySupplier dependencySupplier = new ExoDependencySupplier();
      processor =
         new EverrestProcessor(resources, providers, dependencySupplier, everrestInitializer.getConfiguration(),
            everrestInitializer.getApplication());
      servletContext.setAttribute(DependencySupplier.class.getName(), dependencySupplier);
      servletContext.setAttribute(ResourceBinder.class.getName(), resources);
      servletContext.setAttribute(ApplicationProviderBinder.class.getName(), providers);
      servletContext.setAttribute(EverrestProcessor.class.getName(), processor);

      processComponents(servletContext);
   }

   /**
    * @param container eXo Container instance.
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   protected void processComponents(ServletContext servletContext)
   {
      ExoContainer container = getContainer(servletContext);
      if (container != null)
      {
         Collection adapters = container.getComponentAdapters();
         if (adapters != null && !adapters.isEmpty())
         {
            ResourceDescriptorValidator rdv = ResourceDescriptorValidator.getInstance();
            // Assume all components loaded from ExoContainer are singleton (it is common behavior for ExoContainer).
            // If need more per-request component then use javax.ws.rs.core.Application for deploy.
            ComponentLifecycleScope lifeCycle = ComponentLifecycleScope.SINGLETON;
            
            for (Iterator iter = adapters.iterator(); iter.hasNext();)
            {
               ComponentAdapter cadapter = (ComponentAdapter)iter.next();

               Class clazz = cadapter.getComponentImplementation();
               if (Application.class.isAssignableFrom(clazz))
               {
                  processor.addApplication((Application)cadapter.getComponentInstance(container));
               }
               else
               {
                  if (clazz.getAnnotation(Provider.class) != null)
                  {
                     ProviderDescriptor pDescriptor = new ProviderDescriptorImpl(clazz, lifeCycle);
                     pDescriptor.accept(rdv);
                     
                     if (ContextResolver.class.isAssignableFrom(clazz))
                        providers.addContextResolver(new SingletonObjectFactory<ProviderDescriptor>(pDescriptor,
                           cadapter.getComponentInstance(container)));

                     if (ExceptionMapper.class.isAssignableFrom(clazz))
                        providers.addExceptionMapper(new SingletonObjectFactory<ProviderDescriptor>(pDescriptor,
                                 cadapter.getComponentInstance(container)));

                     if (MessageBodyReader.class.isAssignableFrom(clazz))
                        providers.addMessageBodyReader(new SingletonObjectFactory<ProviderDescriptor>(pDescriptor,
                                 cadapter.getComponentInstance(container)));

                     if (MessageBodyWriter.class.isAssignableFrom(clazz))
                        providers.addMessageBodyWriter(new SingletonObjectFactory<ProviderDescriptor>(pDescriptor,
                                 cadapter.getComponentInstance(container)));
                  }
                  else if (clazz.getAnnotation(Filter.class) != null)
                  {
                     FilterDescriptorImpl fDescriptor = new FilterDescriptorImpl(clazz, lifeCycle);
                     fDescriptor.accept(rdv);
                     
                     if (MethodInvokerFilter.class.isAssignableFrom(clazz))
                        providers.addMethodInvokerFilter(new SingletonObjectFactory<FilterDescriptor>(fDescriptor,
                                 cadapter.getComponentInstance(container)));

                     if (RequestFilter.class.isAssignableFrom(clazz))
                        providers
                           .addRequestFilter(new SingletonObjectFactory<FilterDescriptor>(fDescriptor, cadapter.getComponentInstance(container)));

                     if (ResponseFilter.class.isAssignableFrom(clazz))
                        providers
                           .addResponseFilter(new SingletonObjectFactory<FilterDescriptor>(fDescriptor, cadapter.getComponentInstance(container)));
                  }
                  else if (clazz.getAnnotation(Path.class) != null)
                  {
                     AbstractResourceDescriptor rDescriptor = new AbstractResourceDescriptorImpl(clazz, lifeCycle);
                     rDescriptor.accept(rdv);
                     
                     resources.addResource(new SingletonObjectFactory<AbstractResourceDescriptor>(rDescriptor,
                              cadapter.getComponentInstance(container)));
                  }
               }
            }
         }
      }
   }

   /**
    * Get ExoContainer instance. Typically instance of container is used for look up
    * classes which annotated with &#064;javax.ws.rs.Path, &#064;javax.ws.rs.ext.Provider,
    * &#064;org.everrest.core.Filter annotations or subclasses of javax.ws.rs.core.Application
    * from it. If not need to load any components from ExoContainer this method must
    * return <code>null</code>.
    * 
    * @param servletContext servlet context
    * @return ExoContainer instance or <code>null</code>
    */
   protected abstract ExoContainer getContainer(ServletContext servletContext);

   /**
    * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
    */
   @Override
   public void contextDestroyed(ServletContextEvent servletContextEvent)
   {
   }
}
