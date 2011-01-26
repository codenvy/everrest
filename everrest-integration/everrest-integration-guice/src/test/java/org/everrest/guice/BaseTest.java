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

package org.everrest.guice;

import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;
import junit.framework.TestCase;

import org.everrest.core.DependencySupplier;
import org.everrest.core.RequestHandler;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.RequestHandlerImpl;
import org.everrest.core.tools.ResourceLauncher;
import org.everrest.guice.servlet.EverrestGuiceContextListener;
import org.everrest.test.mock.MockServletContext;

import java.util.List;

import javax.servlet.ServletContextEvent;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public abstract class BaseTest extends TestCase
{
   private class Listener extends EverrestGuiceContextListener
   {
      protected ServletModule getServletModule()
      {
         // Do not need servlet in test.
         return new ServletModule();
      }

      protected List<Module> getModules()
      {
         return BaseTest.this.getModules();
      }
   }

   protected ResourceLauncher launcher;

   public void setUp() throws Exception
   {
      MockServletContext sctx = new MockServletContext();
      Listener listener = new Listener();
      listener.contextInitialized(new ServletContextEvent(sctx));

      DependencySupplier dependencies = (DependencySupplier)sctx.getAttribute(DependencySupplier.class.getName());
      ResourceBinder resources = (ResourceBinder)sctx.getAttribute(ResourceBinder.class.getName());
      ApplicationProviderBinder providers =
         (ApplicationProviderBinder)sctx.getAttribute(ApplicationProviderBinder.class.getName());
      RequestHandler requestHandler =
         new RequestHandlerImpl(resources, providers, dependencies, new EverrestConfiguration());
      launcher = new ResourceLauncher(requestHandler);
   }

   protected abstract List<Module> getModules();
}
