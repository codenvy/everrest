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

import junit.framework.TestCase;

import org.everrest.core.DependencySupplier;
import org.everrest.core.RequestHandler;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.impl.RequestHandlerImpl;
import org.everrest.core.tools.ResourceLauncher;
import org.everrest.pico.servlet.EverrestPicoFilter;
import org.everrest.test.mock.MockServletContext;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.Caching;

import javax.servlet.ServletContext;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public abstract class BaseTest extends TestCase
{
   protected abstract class Composser extends EverrestComposer
   {
      protected void doComposeApplication(MutablePicoContainer container, ServletContext servletContext)
      {
      }

      protected void doComposeRequest(MutablePicoContainer container)
      {
      }

      protected void doComposeSession(MutablePicoContainer container)
      {
      }
   }

   protected ResourceLauncher launcher;
   private DefaultPicoContainer appContainer;

   public void setUp() throws Exception
   {
      appContainer = new DefaultPicoContainer(new Caching());
      appContainer.start();
      DefaultPicoContainer sesContainer = new DefaultPicoContainer();
      DefaultPicoContainer reqContainer = new DefaultPicoContainer();
      Composser composser = getComposser();
      MockServletContext sctx = new MockServletContext();
      composser.composeApplication(appContainer, sctx);
      composser.composeSession(sesContainer);
      composser.composeRequest(reqContainer);

      // NOTE Injection for constructors will not work properly. Just set up scoped containers here.
      EverrestPicoFilter picoFilter = new EverrestPicoFilter();
      picoFilter.setAppContainer(appContainer);
      picoFilter.setSessionContainer(sesContainer);
      picoFilter.setRequestContainer(reqContainer);

      DependencySupplier dependencies = (DependencySupplier)sctx.getAttribute(DependencySupplier.class.getName());
      ResourceBinder resources = (ResourceBinder)sctx.getAttribute(ResourceBinder.class.getName());
      ApplicationProviderBinder providers =
         (ApplicationProviderBinder)sctx.getAttribute(ApplicationProviderBinder.class.getName());

      RequestHandler requestHandler =
         new RequestHandlerImpl(new RequestDispatcher(resources), providers, dependencies, new EverrestConfiguration());
      launcher = new ResourceLauncher(requestHandler);
   }

   @Override
   protected void tearDown() throws Exception
   {
      appContainer.stop();
      appContainer.dispose();
      super.tearDown();
   }

   protected abstract Composser getComposser();
}
