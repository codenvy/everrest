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
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.tools.ResourceLauncher;

/**
 * Initialize EverRest framework by ExoContainer.
 * EverRest itself and JAX-RS application are configured as ExoContainer components.
 *  
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public abstract class StandaloneBaseTest extends BaseTest
{
   protected ResourceLauncher launcher;
   protected ResourceBinder resources;
   protected DependencySupplier dependencies;
   protected ProvidersRegistry providersRegistry;
   protected RequestHandler requestHandler;
   protected RequestDispatcher requestDispatcher;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      dependencies = (DependencySupplier)container.getComponentInstanceOfType(DependencySupplier.class);
      resources = (ResourceBinder)container.getComponentInstanceOfType(ResourceBinder.class);
      providersRegistry = (ProvidersRegistry)container.getComponentInstanceOfType(ProvidersRegistry.class);
      requestHandler = (RequestHandler)container.getComponentInstanceOfType(RequestHandler.class);
      launcher = new ResourceLauncher(requestHandler);
      requestDispatcher = (RequestDispatcher)container.getComponentInstanceOfType(RequestDispatcher.class);
   }
}
