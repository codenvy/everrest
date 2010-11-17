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

package org.everrest.core.impl;

import org.everrest.core.DependencySupplier;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.RequestHandler;
import org.everrest.core.ResourceBinder;
import org.everrest.core.UnhandledException;

import java.io.IOException;

import javax.ws.rs.core.Application;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class EverrestProcessor
{

   private final RequestHandler requestHandler;

   private final ResourceBinder resources;

   private final ProviderBinder providers;

   public EverrestProcessor(ResourceBinder resources, ProviderBinder providers, RequestDispatcher dispatcher,
      DependencySupplier dependencies, EverrestConfiguration config, Application application)
   {
      this.resources = resources;
      this.providers = providers;
      this.requestHandler = new RequestHandlerImpl(resources, providers, dispatcher, dependencies, config);
      addApplication(application);
   }

   public void process(GenericContainerRequest request, GenericContainerResponse response, EnvironmentContext envCtx)
      throws UnhandledException, IOException
   {
      try
      {
         EnvironmentContext.setCurrent(envCtx);
         requestHandler.handleRequest(request, response);
      }
      finally
      {
         EnvironmentContext.setCurrent(null);
      }
   }

   public void addApplication(Application application)
   {
      if (application != null)
      {
         ApplicationPublisher appPublisher = new ApplicationPublisher(resources, providers);
         appPublisher.publish(application);
      }
   }

}
