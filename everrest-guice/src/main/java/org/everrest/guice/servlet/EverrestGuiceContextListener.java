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

package org.everrest.guice.servlet;

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.internal.BindingImpl;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

import org.everrest.core.ComponentLifecycleScope;
import org.everrest.core.DependencySupplier;
import org.everrest.core.Filter;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResourceBinder;
import org.everrest.core.ResponseFilter;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.FilterDescriptorImpl;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.impl.provider.ProviderDescriptorImpl;
import org.everrest.core.impl.resource.AbstractResourceDescriptorImpl;
import org.everrest.core.impl.resource.ResourceDescriptorValidator;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.provider.ProviderDescriptor;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.servlet.EverrestServletContextInitializer;
import org.everrest.guice.EverrestModule;
import org.everrest.guice.GuiceDependencySupplier;
import org.everrest.guice.GuiceObjectFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.ws.rs.Path;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public abstract class EverrestGuiceContextListener extends GuiceServletContextListener
{

   /**
    * Default EverrestGuiceContextListener implementation. It gets application's
    * FQN from context-param <i>javax.ws.rs.Application</i> and instantiate it.
    * If such parameter is not specified then scan (if scanning is enabled) web
    * application's folders WEB-INF/classes and WEB-INF/lib for classes which
    * contains JAX-RS annotations. Interesting for three annotations
    * {@link Path}, {@link Provider} and {@link Filter}. Scanning of JAX-RS
    * components is managed by contex-param <i>org.everrest.scan.components</i>.
    * This parameter must be <i>true</i> to enable scanning.
    */
   public static class DefaultListener extends EverrestGuiceContextListener
   {
      @Override
      protected List<Module> getModules()
      {
         return Collections.emptyList();
      }
   }

   protected EverrestServletContextInitializer everrestInitializer;

   protected ResourceBinderImpl resources;

   protected ApplicationProviderBinder providers;

   protected EverrestProcessor processor;

   /**
    * {@inheritDoc}
    */
   @Override
   public final void contextInitialized(ServletContextEvent servletContextEvent)
   {
      super.contextInitialized(servletContextEvent);
      ServletContext servletContext = servletContextEvent.getServletContext();
      Injector injector = getInjector(servletContext);

      this.everrestInitializer = new EverrestServletContextInitializer(servletContext);
      this.resources = new ResourceBinderImpl();
      this.providers = new ApplicationProviderBinder();
      DependencySupplier dependencySupplier = new GuiceDependencySupplier(injector);
      RequestDispatcher dispatcher = new RequestDispatcher(resources);
      processor =
         new EverrestProcessor(resources, providers, dispatcher, dependencySupplier, everrestInitializer
            .getConfiguration(), everrestInitializer.getApplication());
      servletContext.setAttribute(DependencySupplier.class.getName(), dependencySupplier);
      servletContext.setAttribute(ResourceBinder.class.getName(), resources);
      servletContext.setAttribute(ApplicationProviderBinder.class.getName(), providers);
      servletContext.setAttribute(RequestDispatcher.class.getName(), dispatcher);
      servletContext.setAttribute(EverrestProcessor.class.getName(), processor);

      processBindings(injector);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected final Injector getInjector()
   {
      return Guice.createInjector(createModules());
   }

   private List<Module> createModules()
   {
      List<Module> all = new ArrayList<Module>();
      ServletModule servletModule = getServletModule();
      if (servletModule != null)
         all.add(servletModule);
      all.add(new EverrestModule());
      List<Module> modules = getModules();
      if (modules != null && modules.size() > 0)
         all.addAll(modules);
      return all;
   }

   /**
    * Implementor can provide set of own {@link Module} for JAX-RS components.
    *
    * <pre>
    * protected List&lt;Module&gt; getModules()
    * {
    *    List&lt;Module&gt; modules = new ArrayList&lt;Module&gt;(1);
    *    modules.add(new Module()
    *    {
    *       public void configure(Binder binder)
    *       {
    *          binder.bind(MyResource.class);
    *          binder.bind(MyProvider.class);
    *       }
    *    });
    *    return modules;
    * }
    * </pre>
    *
    * @return JAX-RS modules
    */
   protected abstract List<Module> getModules();

   /**
    * Create servlet module. By default return module with one component
    * GuiceEverrestServlet.
    *
    * @return ServletModule
    */
   protected ServletModule getServletModule()
   {
      return new ServletModule()
      {
         @Override
         protected void configureServlets()
         {
            serve("/*").with(GuiceEverrestServlet.class);
         }
      };
   }

   protected Injector getInjector(ServletContext servletContext)
   {
      return (Injector)servletContext.getAttribute(Injector.class.getName());
   }

   @SuppressWarnings("unchecked")
   protected void processBindings(Injector injector)
   {
      ResourceDescriptorValidator rdv = ResourceDescriptorValidator.getInstance();
      for (Binding<?> binding : injector.getBindings().values())
      {
         Type type = binding.getKey().getTypeLiteral().getType();
         if (type instanceof Class)
         {
            Class clazz = (Class)type;
            // Get scope on binding. Will not initialize fields for object with
            // scope other then Scopes.NO_SCOPE.
            boolean noScope = ((BindingImpl<?>)binding).getScoping().isNoScope();
            if (clazz.getAnnotation(Provider.class) != null)
            {
               ProviderDescriptor pDescriptor =
                  new ProviderDescriptorImpl(clazz, ComponentLifecycleScope.IoC_CONTAINER);
               com.google.inject.Provider<?> guiceProvider = binding.getProvider();
               pDescriptor.accept(rdv);
               if (ContextResolver.class.isAssignableFrom(clazz))
                  providers.addContextResolver(new GuiceObjectFactory<ProviderDescriptor>(pDescriptor, guiceProvider,
                     noScope));

               if (ExceptionMapper.class.isAssignableFrom(clazz))
                  providers.addExceptionMapper(new GuiceObjectFactory<ProviderDescriptor>(pDescriptor, guiceProvider,
                     noScope));

               if (MessageBodyReader.class.isAssignableFrom(clazz))
                  providers.addMessageBodyReader(new GuiceObjectFactory<ProviderDescriptor>(pDescriptor, guiceProvider,
                     noScope));

               if (MessageBodyWriter.class.isAssignableFrom(clazz))
                  providers.addMessageBodyWriter(new GuiceObjectFactory<ProviderDescriptor>(pDescriptor, guiceProvider,
                     noScope));
            }
            else if (clazz.getAnnotation(Filter.class) != null)
            {
               FilterDescriptorImpl fDescriptor =
                  new FilterDescriptorImpl(clazz, ComponentLifecycleScope.IoC_CONTAINER);
               fDescriptor.accept(rdv);
               com.google.inject.Provider<?> guiceProvider = binding.getProvider();

               if (MethodInvokerFilter.class.isAssignableFrom(clazz))
                  providers.addMethodInvokerFilter(new GuiceObjectFactory<FilterDescriptor>(fDescriptor, guiceProvider,
                     noScope));

               if (RequestFilter.class.isAssignableFrom(clazz))
                  providers.addRequestFilter(new GuiceObjectFactory<FilterDescriptor>(fDescriptor, guiceProvider,
                     noScope));

               if (ResponseFilter.class.isAssignableFrom(clazz))
                  providers.addResponseFilter(new GuiceObjectFactory<FilterDescriptor>(fDescriptor, guiceProvider,
                     noScope));
            }
            else if (clazz.getAnnotation(Path.class) != null)
            {
               AbstractResourceDescriptor rDescriptor =
                  new AbstractResourceDescriptorImpl(clazz, ComponentLifecycleScope.IoC_CONTAINER);
               rDescriptor.accept(rdv);
               com.google.inject.Provider<?> guiceProvider = binding.getProvider();

               resources.addResource(new GuiceObjectFactory<AbstractResourceDescriptor>(rDescriptor, guiceProvider,
                  noScope));
            }
         }
         binding.getKey();
      }
   }

}
