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

import java.util.Collection;

import javax.ws.rs.core.Application;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class EverrestProcessor
{
   private final RequestHandler requestHandler;

   public EverrestProcessor(ResourceBinder resources, ProviderBinder providers, RequestDispatcher dispatcher,
      DependencySupplier dependencies, EverrestConfiguration config, Collection<Application> apps)
   {
      ApplicationPublisher appInitializer = new ApplicationPublisher(resources, providers);
      for (Application app : apps)
      {
         appInitializer.publish(app);
      }
      requestHandler = new RequestHandlerImpl(resources, dispatcher, dependencies, config);
   }

   public void process(GenericContainerRequest request, GenericContainerResponse response, EnvironmentContext envCtx)
      throws Exception
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

}
