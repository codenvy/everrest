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
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.core.impl.method.filter.SecurityConstraint;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.ContextResolver;

/**
 * Initialize required components of JAX-RS framework and deploy single JAX-RS application.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class EverrestInitializedListener implements ServletContextListener
{
   /**
    * {@inheritDoc}
    */
   public void contextDestroyed(ServletContextEvent event)
   {
      ServletContext sctx = event.getServletContext();
      ApplicationProviderBinder providers =
         (ApplicationProviderBinder)sctx.getAttribute(ApplicationProviderBinder.class.getName());
      if (providers != null)
      {
         ContextResolver<AsynchronousJobPool> asynchJobsResolver =
            providers.getContextResolver(AsynchronousJobPool.class, null);
         if (asynchJobsResolver != null)
            asynchJobsResolver.getContext(null).stop();
      }
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
      ApplicationProviderBinder providers = new ApplicationProviderBinder();
      
      // Add some internal components depends to configuration.
      if (config.isAsynchronousSupported())
      {
         providers.addContextResolver(new AsynchronousJobPool(config));
         resources.addResource(AsynchronousJobService.class, null);
      }
      if (config.isCheckSecurity())
      {
         providers.addMethodInvokerFilter(new SecurityConstraint());
      }

      EverrestProcessor processor =
         new EverrestProcessor(resources, providers, dependencySupplier, config, application);
      
      sctx.setAttribute(EverrestConfiguration.class.getName(), config);
      sctx.setAttribute(DependencySupplier.class.getName(), dependencySupplier);
      sctx.setAttribute(ResourceBinder.class.getName(), resources);
      sctx.setAttribute(ApplicationProviderBinder.class.getName(), providers);
      sctx.setAttribute(EverrestProcessor.class.getName(), processor);
   }
}
