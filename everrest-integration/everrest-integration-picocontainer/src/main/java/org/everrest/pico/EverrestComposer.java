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
import org.picocontainer.web.script.ScriptedWebappComposer;

import java.util.Collection;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
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
   public enum Scope
   {
      APPLICATION, SESSION, REQUEST
   }

   /**
    * Default EverrestComposer implementation. It gets application's FQN from context-param javax.ws.rs.Application and
    * instantiate it. If such parameter is not specified then scan web application's folders WEB-INF/classes and
    * WEB-INF/lib for classes which contains JAX-RS annotations. Interesting for three annotations {@link Path},
    * {@link Provider} and {@link Filter} .
    */
   public static class DefaultComposer extends EverrestComposer
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

   private static final class PicoEverrestProcessorDestroyer implements Disposable
   {
      private final EverrestProcessor processor;

      private PicoEverrestProcessorDestroyer(EverrestProcessor processor)
      {
         this.processor = processor;
      }

      /** @see org.picocontainer.Disposable#dispose() */
      @Override
      public void dispose()
      {
         processor.stop();
      }
   }

   private static final class PicoFileCollectorDestroyer implements Disposable
   {
      private final FileCollectorDestroyer fileCollectorDestroyer;

      public PicoFileCollectorDestroyer(FileCollectorDestroyer fileCollectorDestroyer)
      {
         this.fileCollectorDestroyer = fileCollectorDestroyer;
      }

      /** @see org.picocontainer.Disposable#dispose() */
      @Override
      public void dispose()
      {
         fileCollectorDestroyer.stopFileCollector();
      }
   }

   protected ApplicationProviderBinder providers;
   protected ResourceBinder resources;

   private ScriptedWebappComposer scriptedComposer;
   private String sessionScript;
   private String requestScript;

   public final void composeApplication(MutablePicoContainer container, ServletContext servletContext)
   {
      String applicationScript = servletContext.getInitParameter("application-script");
      if (applicationScript == null)
      {
         applicationScript = ScriptedWebappComposer.DEFAULT_APPLICATION_SCRIPT;
      }

      sessionScript = servletContext.getInitParameter("session-script");
      if (sessionScript == null)
      {
         sessionScript = ScriptedWebappComposer.DEFAULT_SESSION_SCRIPT;
      }

      requestScript = servletContext.getInitParameter("request-script");
      if (requestScript == null)
      {
         requestScript = ScriptedWebappComposer.DEFAULT_REQUEST_SCRIPT;
      }

      String containerBuilder = servletContext.getInitParameter("scripted-container-builder");
      if (containerBuilder == null)
      {
         containerBuilder = ScriptedWebappComposer.DEFAULT_CONTAINER_BUILDER;
      }

      scriptedComposer = new ScriptedWebappComposer(containerBuilder, applicationScript, sessionScript, requestScript);

      EverrestServletContextInitializer everrestInitializer = new EverrestServletContextInitializer(servletContext);
      this.resources = new ResourceBinderImpl();
      this.providers = new ApplicationProviderBinder();
      DependencySupplier dependencySupplier = new PicoDependencySupplier();
      EverrestConfiguration config = everrestInitializer.getConfiguration();
      Application application = everrestInitializer.getApplication();
      EverrestApplication everrest = new EverrestApplication(config);
      everrest.addApplication(application);
      EverrestProcessor processor = new EverrestProcessor(resources, providers, dependencySupplier, config, everrest);
      container.addComponent(new PicoEverrestProcessorDestroyer(processor));
      container.addComponent(new PicoFileCollectorDestroyer(makeFileCollectorDestroyer()));

      servletContext.setAttribute(EverrestConfiguration.class.getName(), config);
      servletContext.setAttribute(DependencySupplier.class.getName(), dependencySupplier);
      servletContext.setAttribute(ResourceBinder.class.getName(), resources);
      servletContext.setAttribute(ApplicationProviderBinder.class.getName(), providers);
      servletContext.setAttribute(EverrestProcessor.class.getName(), processor);

      if (isResourceAvailable(applicationScript))
      {
         scriptedComposer.composeApplication(container, servletContext);
      }

      doComposeApplication(container, servletContext);
      processComponents(container, Scope.APPLICATION);
   }

   protected FileCollectorDestroyer makeFileCollectorDestroyer()
   {
      return new FileCollectorDestroyer();
   }

   public final void composeSession(MutablePicoContainer container)
   {
      if (isResourceAvailable(sessionScript))
      {
         scriptedComposer.composeSession(container);
      }

      doComposeSession(container);
      processComponents(container, Scope.SESSION);
   }

   public final void composeRequest(MutablePicoContainer container)
   {
      if (isResourceAvailable(requestScript))
      {
         scriptedComposer.composeRequest(container);
      }

      doComposeRequest(container);
      processComponents(container, Scope.REQUEST);
   }

   private boolean isResourceAvailable(String resource)
   {
      return Thread.currentThread().getContextClassLoader().getResource(resource) != null;
   }

   /**
    * Compose components with application scope.
    * <p/>
    * <pre>
    * // Do this if need to keep default everrest framework behaviour.
    * processor.addApplication(everrestInitializer.getApplication());
    * // Register components in picocontainer.
    * container.addComponent(MyApplicationScopeResource.class);
    * container.addComponent(MyApplicationScopeProvider.class);
    * </pre>
    *
    * @param container
    *    picocontainer
    * @param servletContext
    *    servlet context
    */
   protected abstract void doComposeApplication(MutablePicoContainer container, ServletContext servletContext);

   /**
    * Compose components with request scope.
    * <p/>
    * <pre>
    * container.addComponent(MyRequestScopeResource.class);
    * container.addComponent(MyRequestScopeProvider.class);
    * </pre>
    *
    * @param container
    *    picocontainer
    */
   protected abstract void doComposeRequest(MutablePicoContainer container);

   /**
    * Compose components with session scope.
    * <p/>
    * <pre>
    * container.addComponent(MySessionScopeResource.class);
    * container.addComponent(MySessionScopeProvider.class);
    * </pre>
    *
    * @param container
    *    picocontainer
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
            {
               providers.addContextResolver(new PicoObjectFactory<ProviderDescriptor>(pDescriptor));
            }

            if (ExceptionMapper.class.isAssignableFrom(clazz))
            {
               providers.addExceptionMapper(new PicoObjectFactory<ProviderDescriptor>(pDescriptor));
            }

            if (MessageBodyReader.class.isAssignableFrom(clazz))
            {
               providers.addMessageBodyReader(new PicoObjectFactory<ProviderDescriptor>(pDescriptor));
            }

            if (MessageBodyWriter.class.isAssignableFrom(clazz))
            {
               providers.addMessageBodyWriter(new PicoObjectFactory<ProviderDescriptor>(pDescriptor));
            }
         }
         else if (clazz.getAnnotation(Filter.class) != null)
         {
            FilterDescriptorImpl fDescriptor = new FilterDescriptorImpl(clazz, lifeCycle);
            fDescriptor.accept(rdv);

            if (MethodInvokerFilter.class.isAssignableFrom(clazz))
            {
               providers.addMethodInvokerFilter(new PicoObjectFactory<FilterDescriptor>(fDescriptor));
            }

            if (RequestFilter.class.isAssignableFrom(clazz))
            {
               providers.addRequestFilter(new PicoObjectFactory<FilterDescriptor>(fDescriptor));
            }

            if (ResponseFilter.class.isAssignableFrom(clazz))
            {
               providers.addResponseFilter(new PicoObjectFactory<FilterDescriptor>(fDescriptor));
            }
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
