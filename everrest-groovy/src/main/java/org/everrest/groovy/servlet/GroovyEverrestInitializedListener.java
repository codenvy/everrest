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

package org.everrest.groovy.servlet;

import org.everrest.core.DependencySupplier;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.servlet.EverrestServletContextInitializer;
import org.everrest.core.servlet.ServletContextDependencySupplier;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.core.Application;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: GroovyEverrestInitializedListener.java 77 2010-10-26 15:20:15Z
 *          andrew00x $
 */
public class GroovyEverrestInitializedListener implements ServletContextListener
{

   public void contextDestroyed(ServletContextEvent event)
   {
   }

   public void contextInitialized(ServletContextEvent event)
   {
      ServletContext sctx = event.getServletContext();
      EverrestServletContextInitializer initializer = new GroovyEverrestServletContextInitializer(sctx);
      Application application = initializer.getApplication();
      DependencySupplier dependencySupplier = null;
      String dependencyInjectorFQN = initializer.getParameter(DependencySupplier.class.getName());
      if (dependencyInjectorFQN != null)
      {
         try
         {
            Class<?> cl = Thread.currentThread().getContextClassLoader().loadClass(dependencyInjectorFQN.trim());
            dependencySupplier = (DependencySupplier)cl.newInstance();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      if (dependencySupplier == null)
      {
         dependencySupplier = new ServletContextDependencySupplier(sctx);
      }
      EverrestConfiguration config = initializer.getConfiguration();
      ResourceBinder resources = new ResourceBinderImpl();
      RequestDispatcher dispatcher = (RequestDispatcher)sctx.getAttribute(RequestDispatcher.class.getName());
      if (dispatcher == null)
      {
         dispatcher = new RequestDispatcher(resources);
      }
      ProviderBinder providers = new ApplicationProviderBinder();
      EverrestProcessor processor =
         new EverrestProcessor(resources, providers, dispatcher, dependencySupplier, config, application);
      sctx.setAttribute(EverrestProcessor.class.getName(), processor);
   }
}
