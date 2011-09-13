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
import org.everrest.core.impl.InternalException;
import org.everrest.core.impl.LifecycleComponent;
import org.everrest.core.impl.ResourceBinderImpl;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;

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
      @SuppressWarnings("unchecked")
      List<WeakReference<Object>> l =
         (List<WeakReference<Object>>)sctx.getAttribute("org.everrest.lifecycle.Singletons");
      if (l != null && l.size() > 0)
      {
         RuntimeException exception = null;
         for (WeakReference<Object> ref : l)
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
         l.clear();
         if (exception != null)
            throw exception;
      }
   }

   /**
    * {@inheritDoc}
    */
   public void contextInitialized(ServletContextEvent event)
   {
      ServletContext sctx = event.getServletContext();
      DependencySupplier dependencySupplier = (DependencySupplier)sctx.getAttribute(DependencySupplier.class.getName());
      if (dependencySupplier == null)
         dependencySupplier = new ServletContextDependencySupplier(sctx);
      ResourceBinder resources = new ResourceBinderImpl();
      ApplicationProviderBinder providers = new ApplicationProviderBinder();

      EverrestServletContextInitializer initializer = new EverrestServletContextInitializer(sctx);
      EverrestConfiguration config = initializer.getConfiguration();
      Application application = initializer.getApplication();

      EverrestApplication everrest = new EverrestApplication(config);
      everrest.addApplication(application);

      Set<Object> singletons = everrest.getSingletons();
      // Do not prevent GC remove objects if they are removed somehow from ResourceBinder or ProviderBinder.
      List<WeakReference<Object>> l = new ArrayList<WeakReference<Object>>(singletons.size());
      for (Object o : singletons)
         l.add(new WeakReference<Object>(o));
      sctx.setAttribute("org.everrest.lifecycle.Singletons", l);

      EverrestProcessor processor = new EverrestProcessor(resources, providers, dependencySupplier, config, everrest);

      sctx.setAttribute(EverrestConfiguration.class.getName(), config);
      sctx.setAttribute(DependencySupplier.class.getName(), dependencySupplier);
      sctx.setAttribute(ResourceBinder.class.getName(), resources);
      sctx.setAttribute(ApplicationProviderBinder.class.getName(), providers);
      sctx.setAttribute(EverrestProcessor.class.getName(), processor);
   }
}
