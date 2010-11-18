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

import org.everrest.core.ApplicationContext;
import org.everrest.core.InitialProperties;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.util.Logger;
import org.everrest.pico.ComponentScopedWrapper.Scope;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.adapters.AbstractAdapter;
import org.picocontainer.web.PicoServletContainerFilter;

import java.lang.reflect.Type;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
@SuppressWarnings("serial")
public class EverrestPicoFilter extends PicoServletContainerFilter
{
   private static final Logger log = Logger.getLogger(EverrestPicoFilter.class);

   public static class HttpHeadersInjector extends AbstractAdapter<HttpHeaders>
   {
      public HttpHeadersInjector()
      {
         super(HttpHeaders.class, HttpHeaders.class);
      }

      public HttpHeaders getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException
      {
         ApplicationContext context = ApplicationContextImpl.getCurrent();
         if (context == null)
            throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
         return context.getHttpHeaders();
      }

      public String getDescriptor()
      {
         return "HttpHeaders";
      }

      public void verify(PicoContainer container) throws PicoCompositionException
      {
      }
   }

   public static class InitialPropertiesInjector extends AbstractAdapter<InitialProperties>
   {
      public InitialPropertiesInjector()
      {
         super(InitialProperties.class, InitialProperties.class);
      }

      public InitialProperties getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException
      {
         ApplicationContext context = ApplicationContextImpl.getCurrent();
         if (context == null)
            throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
         return context.getInitialProperties();
      }

      public String getDescriptor()
      {
         return "InitialProperties";
      }

      public void verify(PicoContainer container) throws PicoCompositionException
      {
      }
   }

   public static class ProvidersInjector extends AbstractAdapter<Providers>
   {
      public ProvidersInjector()
      {
         super(Providers.class, Providers.class);
      }

      public Providers getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException
      {
         ApplicationContext context = ApplicationContextImpl.getCurrent();
         if (context == null)
            throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
         return context.getProviders();
      }

      public String getDescriptor()
      {
         return "Providers";
      }

      public void verify(PicoContainer container) throws PicoCompositionException
      {
      }
   }

   public static class RequestInjector extends AbstractAdapter<Request>
   {
      public RequestInjector()
      {
         super(Request.class, Request.class);
      }

      public Request getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException
      {
         ApplicationContext context = ApplicationContextImpl.getCurrent();
         if (context == null)
            throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
         return context.getRequest();
      }

      public String getDescriptor()
      {
         return "Request";
      }

      public void verify(PicoContainer container) throws PicoCompositionException
      {
      }
   }

   public static class SecurityContextInjector extends AbstractAdapter<SecurityContext>
   {
      public SecurityContextInjector()
      {
         super(SecurityContext.class, SecurityContext.class);
      }

      public SecurityContext getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException
      {
         ApplicationContext context = ApplicationContextImpl.getCurrent();
         if (context == null)
            throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
         return context.getSecurityContext();
      }

      public String getDescriptor()
      {
         return "SecurityContext";
      }

      public void verify(PicoContainer container) throws PicoCompositionException
      {
      }
   }

   public static class ServletConfigInjector extends AbstractAdapter<ServletConfig>
   {
      public ServletConfigInjector()
      {
         super(ServletConfig.class, ServletConfig.class);
      }

      public ServletConfig getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException
      {

         EnvironmentContext context = EnvironmentContext.getCurrent();
         if (context == null)
            throw new IllegalStateException("EverRest EnvironmentContext is not initialized.");
         return (ServletConfig)context.get(ServletConfig.class);
      }

      public String getDescriptor()
      {
         return "ServletConfig";
      }

      public void verify(PicoContainer container) throws PicoCompositionException
      {
      }
   }

   public static class ServletContextInjector extends AbstractAdapter<ServletContext>
   {
      public ServletContextInjector()
      {
         super(ServletContext.class, ServletContext.class);
      }

      public ServletContext getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException
      {

         EnvironmentContext context = EnvironmentContext.getCurrent();
         if (context == null)
            throw new IllegalStateException("EverRest EnvironmentContext is not initialized.");
         return (ServletContext)context.get(ServletContext.class);
      }

      public String getDescriptor()
      {
         return "ServletContext";
      }

      public void verify(PicoContainer container) throws PicoCompositionException
      {
      }
   }

   public static class UriInfoInjector extends AbstractAdapter<UriInfo>
   {
      public UriInfoInjector()
      {
         super(UriInfo.class, UriInfo.class);
      }

      public UriInfo getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException
      {
         ApplicationContext context = ApplicationContextImpl.getCurrent();
         if (context == null)
            throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
         return context.getUriInfo();
      }

      public String getDescriptor()
      {
         return "UriInfo";
      }

      public void verify(PicoContainer container) throws PicoCompositionException
      {
      }
   }

   private static final ThreadLocal<MutablePicoContainer> currentAppContainer = new ThreadLocal<MutablePicoContainer>();
   private static final ThreadLocal<MutablePicoContainer> currentSessionContainer = new ThreadLocal<MutablePicoContainer>();
   private static final ThreadLocal<MutablePicoContainer> currentRequestContainer = new ThreadLocal<MutablePicoContainer>();

   public static <T> ComponentScopedWrapper<T> getComponent(Class<T> type)
   {
      // Since containers are inherited start lookup components from top container.
      // It is application scope container in our case.
      T object = getAppContainer().getComponent(type);
      if (object != null)
         return new ComponentScopedWrapper<T>(object, Scope.APPLICATION);
      object = getSessionContainer().getComponent(type);
      if (object != null)
         return new ComponentScopedWrapper<T>(object, Scope.SESSION);
      object = getRequestContainer().getComponent(type);
      if (object != null)
         return new ComponentScopedWrapper<T>(object, Scope.REQUEST);
      if (log.isDebugEnabled())
         log.debug("Component with type " + type.getName() + " not found in any containers.");
      return null;
   }

   static MutablePicoContainer getAppContainer()
   {
      MutablePicoContainer container = currentAppContainer.get();
      if (container == null)
         throw new IllegalStateException("No container was found in application scope. ");
      return container;
   }

   static MutablePicoContainer getRequestContainer()
   {
      MutablePicoContainer container = currentRequestContainer.get();
      if (container == null)
         throw new IllegalStateException("No container was found in request scope. ");
      return container;
   }

   static MutablePicoContainer getSessionContainer()
   {
      MutablePicoContainer container = currentSessionContainer.get();
      if (container == null)
         throw new IllegalStateException("No container was found in session scope. ");
      return container;
   }

   @Override
   public void destroy()
   {
      super.destroy();
      try
      {
         currentAppContainer.remove();
         currentSessionContainer.remove();
         currentRequestContainer.remove();
      }
      catch (Exception e)
      {
         log.error(e.getMessage(), e);
      }
   }

   @Override
   protected void initAdditionalScopedComponents(MutablePicoContainer sessionContainer,
      MutablePicoContainer reqContainer)
   {
      // Add injectors for some components required by JAX-RS resources and providers.

      // TODO Still have issue with injected components via constructors. JAX-RS
      // specification provide wide set of annotations that can be applied to
      // wide set of Java types, e.g. @CookieParam, @QueryParam, etc. See
      // section 3.1.2 of JAX-RS specification. How to do it with picocontainer ???
      // This issue ONLY for constructor parameters, all fields for components
      // of 'request container' will be initialized in
      // PicoObjectFactory.getInstance(ApplicationContext).
      reqContainer.as(Characteristics.NO_CACHE).addAdapter(new InitialPropertiesInjector());
      reqContainer.as(Characteristics.NO_CACHE).addAdapter(new HttpHeadersInjector());
      reqContainer.as(Characteristics.NO_CACHE).addAdapter(new ProvidersInjector());
      reqContainer.as(Characteristics.NO_CACHE).addAdapter(new RequestInjector());
      reqContainer.as(Characteristics.NO_CACHE).addAdapter(new SecurityContextInjector());
      reqContainer.as(Characteristics.NO_CACHE).addAdapter(new UriInfoInjector());
      reqContainer.as(Characteristics.NO_CACHE).addAdapter(new ServletConfigInjector());
      reqContainer.as(Characteristics.NO_CACHE).addAdapter(new ServletContextInjector());
   }

   protected void setAppContainer(MutablePicoContainer container)
   {
      currentAppContainer.set(container);
   }

   protected void setRequestContainer(MutablePicoContainer container)
   {
      currentRequestContainer.set(container);
   }

   protected void setSessionContainer(MutablePicoContainer container)
   {
      currentSessionContainer.set(container);
   }

}
