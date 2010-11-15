/*
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.everrest.core.servlet;

import org.everrest.core.DependencySupplier;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.impl.ResourceBinderImpl;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.core.Application;

/**
 * Initialize required components of JAX-RS framework and deploy single JAX-RS
 * application.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: RestInitializedListener.java 436 2009-10-28 06:47:29Z aparfonov
 *          $
 */
public class EverrestInitializedListener implements ServletContextListener
{

   /**
    * {@inheritDoc}
    */
   public void contextDestroyed(ServletContextEvent event)
   {
   }

   /**
    * {@inheritDoc}
    */
   public void contextInitialized(ServletContextEvent event)
   {
      ServletContext sctx = event.getServletContext();
      EverrestServletContextInitializer initializer = new EverrestServletContextInitializer(sctx);

      Application application = initializer.getApplication();
      DependencySupplier dependencySupplier = (DependencySupplier)sctx.getAttribute(DependencySupplier.class.getName());
      if (dependencySupplier == null)
         dependencySupplier = new ServletContextDependencySupplier(sctx);
      EverrestConfiguration config = initializer.getConfiguration();
      ResourceBinder resources = new ResourceBinderImpl();
      RequestDispatcher dispatcher = (RequestDispatcher)sctx.getAttribute(RequestDispatcher.class.getName());
      if (dispatcher == null)
         dispatcher = new RequestDispatcher(resources);
      ApplicationProviderBinder providers = new ApplicationProviderBinder();
      EverrestProcessor processor =
         new EverrestProcessor(resources, providers, dispatcher, dependencySupplier, config, application);
      sctx.setAttribute(DependencySupplier.class.getName(), dependencySupplier);
      sctx.setAttribute(ResourceBinder.class.getName(), resources);
      sctx.setAttribute(ApplicationProviderBinder.class.getName(), providers);
      sctx.setAttribute(RequestDispatcher.class.getName(), dispatcher);
      sctx.setAttribute(EverrestProcessor.class.getName(), processor);
   }
}
