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

package org.everrest.pico;

import org.everrest.core.ComponentLifecycleScope;
import org.everrest.core.DependencySupplier;
import org.everrest.core.Filter;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResourceBinder;
import org.everrest.core.ResponseFilter;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.FileCollectorDestroyer;
import org.everrest.core.impl.FilterDescriptorImpl;
import org.everrest.core.impl.InternalException;
import org.everrest.core.impl.LifecycleComponent;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.impl.provider.ProviderDescriptorImpl;
import org.everrest.core.impl.resource.AbstractResourceDescriptorImpl;
import org.everrest.core.impl.resource.ResourceDescriptorValidator;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.provider.ProviderDescriptor;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.servlet.EverrestApplication;
import org.everrest.core.servlet.EverrestServletContextInitializer;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.Disposable;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.web.PicoServletContainerListener;
import org.picocontainer.web.WebappComposer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * Register components of containers with different webapp scopes (application, session, request) in EverRest framework
 * if they are annotated with &#64;Path, &#64;Provider or &#64;Filter annotation.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 * @see WebappComposer
 */
public abstract class EverrestComposer implements WebappComposer
{
   public enum Scope {
      APPLICATION, SESSION, REQUEST
   }

   /**
    * Default EverrestComposer implementation. It gets application's FQN from context-param javax.ws.rs.Application and
    * instantiate it. If such parameter is not specified then scan web application's folders WEB-INF/classes and
    * WEB-INF/lib for classes which contains JAX-RS annotations. Interesting for three annotations {@link Path},
    * {@link Provider} and {@link Filter} .
    */
   public static class DefaultComposser extends EverrestComposer
   {
      /**
       * Do nothing in default implementation but can be overridden in subclasses to add component with application
       * scope.
       * 
       * @see EverrestServletContextInitializer#getApplication()
       * @see PicoServletContainerListener
       */
      @Override
      protected void doComposeApplication(MutablePicoContainer container, ServletContext servletContext)
      {
      }

      /**
       * Do nothing in default implementation but can be overridden in subclasses to add component with request scope.
       * 
       * @see PicoServletContainerListener
       */
      @Override
      protected void doComposeRequest(MutablePicoContainer container)
      {
      }

      /**
       * Do nothing in default implementation but can be overridden in subclasses to add component with session scope.
       * 
       * @see PicoServletContainerListener
       */
      @Override
      protected void doComposeSession(MutablePicoContainer container)
      {
      }
   }

   private static final class LifecycleDestructor implements Disposable
   {
      private final List<WeakReference<Object>> toDestroy;

      private LifecycleDestructor(List<WeakReference<Object>> toDestroy)
      {
         this.toDestroy = toDestroy;
      }

      @Override
      public void dispose()
      {
         if (toDestroy != null && toDestroy.size() > 0)
         {
            RuntimeException exception = null;
            for (WeakReference<Object> ref : toDestroy)
            {
               Object o = ref.get();
               if (o != null)
               {
                  try
                  {
                     new LifecycleComponent(o).destroy();
                  }
                  catch (WebApplicationException e)
                  {
                     if (exception == null)
                        exception = e;
                  }
                  catch (InternalException e)
                  {
                     if (exception == null)
                        exception = e;
                  }
               }
            }
            toDestroy.clear();
            if (exception != null)
               throw exception;
         }
      }
   }

   private static final class PicoFileCollectorDestroyer extends FileCollectorDestroyer implements Disposable
   {
      /**
       * @see org.picocontainer.Disposable#dispose()
       */
      @Override
      public void dispose()
      {
         stopFileCollector();
      }
   }

   protected ApplicationProviderBinder providers;

   protected ResourceBinder resources;

   protected EverrestServletContextInitializer everrestInitializer;

   protected EverrestProcessor processor;

   public final void composeApplication(MutablePicoContainer container, ServletContext servletContext)
   {
      this.everrestInitializer = new EverrestServletContextInitializer(servletContext);
      this.resources = new ResourceBinderImpl();
      this.providers = new ApplicationProviderBinder();
      DependencySupplier dependencySupplier = new PicoDependencySupplier();

      EverrestConfiguration config = everrestInitializer.getConfiguration();
      Application application = everrestInitializer.getApplication();

      EverrestApplication everrest = new EverrestApplication(config);
      everrest.addApplication(application);

      Set<Object> singletons = everrest.getSingletons();
      // Do not prevent GC remove objects if they are removed somehow from ResourceBinder or ProviderBinder.
      // NOTE We provider life cycle control ONLY for components loaded via Application and do nothing for components
      // obtained from container. Container must take care about its components.  
      List<WeakReference<Object>> l = new ArrayList<WeakReference<Object>>(singletons.size());
      for (Object o : singletons)
      {
         l.add(new WeakReference<Object>(o));
      }

      container.addComponent(new LifecycleDestructor(l));
      container.addComponent(makeFileCollectorDestroyer());

      processor = new EverrestProcessor(resources, providers, dependencySupplier, config, everrest);

      servletContext.setAttribute(EverrestConfiguration.class.getName(), config);
      servletContext.setAttribute(DependencySupplier.class.getName(), dependencySupplier);
      servletContext.setAttribute(ResourceBinder.class.getName(), resources);
      servletContext.setAttribute(ApplicationProviderBinder.class.getName(), providers);
      servletContext.setAttribute(EverrestProcessor.class.getName(), processor);

      doComposeApplication(container, servletContext);
      processComponents(container, Scope.APPLICATION);
   }

   protected Disposable makeFileCollectorDestroyer()
   {
      return new PicoFileCollectorDestroyer();
   }
   
   public final void composeRequest(MutablePicoContainer container)
   {
      doComposeRequest(container);
      processComponents(container, Scope.REQUEST);
   }

   public final void composeSession(MutablePicoContainer container)
   {
      doComposeSession(container);
      processComponents(container, Scope.SESSION);
   }

   /**
    * Compose components with application scope.
    * 
    * <pre>
    * // Do this if need to keep default everrest framework behaviour.
    * processor.addApplication(everrestInitializer.getApplication());
    * // Register components in picocontainer.
    * container.addComponent(MyApplicationScopeResource.class);
    * container.addComponent(MyApplicationScopeProvider.class);
    * </pre>
    * 
    * @param container picocontainer
    * @param servletContext servlet context
    */
   protected abstract void doComposeApplication(MutablePicoContainer container, ServletContext servletContext);

   /**
    * Compose components with request scope.
    * 
    * <pre>
    * container.addComponent(MyRequestScopeResource.class);
    * container.addComponent(MyRequestScopeProvider.class);
    * </pre>
    * 
    * @param container picocontainer
    */
   protected abstract void doComposeRequest(MutablePicoContainer container);

   /**
    * Compose components with session scope.
    * 
    * <pre>
    * container.addComponent(MySessionScopeResource.class);
    * container.addComponent(MySessionScopeProvider.class);
    * </pre>
    * 
    * @param container picocontainer
    */
   protected abstract void doComposeSession(MutablePicoContainer container);

   protected void processComponents(MutablePicoContainer container, Scope scope)
   {
      // Avoid unnecessary of fields and constructors for components with scope other then request.
      ComponentLifecycleScope lifeCycle =
         scope == Scope.REQUEST ? ComponentLifecycleScope.PER_REQUEST : ComponentLifecycleScope.SINGLETON;

      Collection<ComponentAdapter<?>> adapters = container.getComponentAdapters();
      ResourceDescriptorValidator rdv = ResourceDescriptorValidator.getInstance();
      for (ComponentAdapter<?> adapter : adapters)
      {
         Class<?> clazz = adapter.getComponentImplementation();
         if (clazz.getAnnotation(Provider.class) != null)
         {
            ProviderDescriptor pDescriptor = new ProviderDescriptorImpl(clazz, lifeCycle);
            pDescriptor.accept(rdv);
            if (ContextResolver.class.isAssignableFrom(clazz))
               providers.addContextResolver(new PicoObjectFactory<ProviderDescriptor>(pDescriptor));

            if (ExceptionMapper.class.isAssignableFrom(clazz))
               providers.addExceptionMapper(new PicoObjectFactory<ProviderDescriptor>(pDescriptor));

            if (MessageBodyReader.class.isAssignableFrom(clazz))
               providers.addMessageBodyReader(new PicoObjectFactory<ProviderDescriptor>(pDescriptor));

            if (MessageBodyWriter.class.isAssignableFrom(clazz))
               providers.addMessageBodyWriter(new PicoObjectFactory<ProviderDescriptor>(pDescriptor));
         }
         else if (clazz.getAnnotation(Filter.class) != null)
         {
            FilterDescriptorImpl fDescriptor = new FilterDescriptorImpl(clazz, lifeCycle);
            fDescriptor.accept(rdv);

            if (MethodInvokerFilter.class.isAssignableFrom(clazz))
               providers.addMethodInvokerFilter(new PicoObjectFactory<FilterDescriptor>(fDescriptor));

            if (RequestFilter.class.isAssignableFrom(clazz))
               providers.addRequestFilter(new PicoObjectFactory<FilterDescriptor>(fDescriptor));

            if (ResponseFilter.class.isAssignableFrom(clazz))
               providers.addResponseFilter(new PicoObjectFactory<FilterDescriptor>(fDescriptor));
         }
         else if (clazz.getAnnotation(Path.class) != null)
         {
            AbstractResourceDescriptor descriptor = new AbstractResourceDescriptorImpl(clazz, lifeCycle);
            descriptor.accept(rdv);
            resources.addResource(new PicoObjectFactory<AbstractResourceDescriptor>(descriptor));
         }
      }
   }
}
