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
package org.everrest.exoplatform;

import org.everrest.core.DependencySupplier;
import org.everrest.core.RequestHandler;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.impl.RequestHandlerImpl;
import org.everrest.core.tools.ResourceLauncher;
import org.everrest.exoplatform.servlet.EverrestExoContextListener;
import org.everrest.test.mock.MockServletContext;
import org.exoplatform.container.ExoContainer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * Emulate start of JAXR-RS application via {@link EverrestExoContextListener}.
 * EverRest itself is not configured as ExoContainer components. Container
 * used for delivering JAXR-RS components only.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public abstract class WebAppBaseTest extends BaseTest
{
   protected ResourceLauncher launcher;
   protected ResourceBinder resources;
   protected DependencySupplier dependencies;
   protected ApplicationProviderBinder providers;
   private EverrestExoContextListener listener;
   private MockServletContext sctx;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      sctx = new MockServletContext();
      listener = new EverrestExoContextListener() {
         @Override
         protected ExoContainer getContainer(ServletContext servletContext)
         {
            return container;
         }
      };

      listener.contextInitialized(new ServletContextEvent(sctx));

      dependencies = (DependencySupplier)sctx.getAttribute(DependencySupplier.class.getName());
      resources = (ResourceBinder)sctx.getAttribute(ResourceBinder.class.getName());
      providers = (ApplicationProviderBinder)sctx.getAttribute(ApplicationProviderBinder.class.getName());

      RequestHandler requestHandler =
         new RequestHandlerImpl(new RequestDispatcher(resources), providers, dependencies, new EverrestConfiguration());
      launcher = new ResourceLauncher(requestHandler);
   }

   @Override
   protected void tearDown() throws Exception
   {
      listener.contextDestroyed(new ServletContextEvent(sctx));
      super.tearDown();
   }
   
}
