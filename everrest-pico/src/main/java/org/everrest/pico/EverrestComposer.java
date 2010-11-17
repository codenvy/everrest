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
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.web.PicoServletContainerListener;
import org.picocontainer.web.WebappComposer;

import java.util.Collection;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * Register components of containers with different webapp scopes (application,
 * session, request) in EverRest framework if they are annotated with &#64;Path,
 * &#64;Provider or &#64;Filter annotation.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 * @see WebappComposer
 */
public abstract class EverrestComposer implements WebappComposer
{

   /**
    * Default EverrestComposer implementation. It gets application's FQN from
    * context-param javax.ws.rs.Application and instantiate it. If such
    * parameter is not specified then scan web application's folders
    * WEB-INF/classes and WEB-INF/lib for classes which contains JAX-RS
    * annotations. Interesting for three annotations {@link Path},
    * {@link Provider} and {@link Filter} .
    */
   public static class DefaultComposser extends EverrestComposer
   {

      /**
       * Do nothing in default implementation but can be overridden in
       * subclasses to add component with application scope.
       *
       * @see EverrestServletContextInitializer#getApplication()
       * @see PicoServletContainerListener
       */
      @Override
      protected void doComposeApplication(MutablePicoContainer container, ServletContext servletContext)
      {
      }

      /**
       * Do nothing in default implementation but can be overridden in
       * subclasses to add component with request scope.
       *
       * @see PicoServletContainerListener
       */
      @Override
      protected void doComposeRequest(MutablePicoContainer container)
      {
      }

      /**
       * Do nothing in default implementation but can be overridden in
       * subclasses to add component with session scope.
       *
       * @see PicoServletContainerListener
       */
      @Override
      protected void doComposeSession(MutablePicoContainer container)
      {
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
      RequestDispatcher dispatcher = new RequestDispatcher(resources);
      processor =
         new EverrestProcessor(resources, providers, dispatcher, dependencySupplier, everrestInitializer
            .getConfiguration(), everrestInitializer.getApplication());

      servletContext.setAttribute(DependencySupplier.class.getName(), dependencySupplier);
      servletContext.setAttribute(ResourceBinder.class.getName(), resources);
      servletContext.setAttribute(ApplicationProviderBinder.class.getName(), providers);
      servletContext.setAttribute(RequestDispatcher.class.getName(), dispatcher);
      servletContext.setAttribute(EverrestProcessor.class.getName(), processor);

      doComposeApplication(container, servletContext);
      processComponents(container);
   }

   public final void composeRequest(MutablePicoContainer container)
   {
      doComposeRequest(container);
      processComponents(container);
   }

   public final void composeSession(MutablePicoContainer container)
   {
      doComposeSession(container);
      processComponents(container);
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

   protected void processComponents(MutablePicoContainer container)
   {
      Collection<ComponentAdapter<?>> adapters = container.getComponentAdapters();
      ResourceDescriptorValidator rdv = ResourceDescriptorValidator.getInstance();
      for (ComponentAdapter<?> adapter : adapters)
      {
         Class<?> clazz = adapter.getComponentImplementation();
         if (clazz.getAnnotation(Provider.class) != null)
         {
            ProviderDescriptor pDescriptor = new ProviderDescriptorImpl(clazz, ComponentLifecycleScope.IoC_CONTAINER);
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
            FilterDescriptorImpl fDescriptor = new FilterDescriptorImpl(clazz, ComponentLifecycleScope.IoC_CONTAINER);
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
            AbstractResourceDescriptor descriptor =
               new AbstractResourceDescriptorImpl(clazz, ComponentLifecycleScope.IoC_CONTAINER);
            descriptor.accept(rdv);

            resources.addResource(new PicoObjectFactory<AbstractResourceDescriptor>(descriptor));
         }
      }
   }

}
