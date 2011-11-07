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
import org.everrest.core.Lifecycle;
import org.everrest.core.RequestHandler;
import org.everrest.core.ResourceBinder;
import org.everrest.core.UnhandledException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class EverrestProcessor implements Lifecycle
{
   private final RequestHandler requestHandler;
   private final ResourceBinder resources;
   private final ProviderBinder providers;
   private final List<WeakReference<Object>> singletonsReferences = new ArrayList<WeakReference<Object>>();

   public EverrestProcessor(ResourceBinder resources, ProviderBinder providers, DependencySupplier dependencies,
      EverrestConfiguration config, Application application)
   {
      this.resources = resources;
      this.providers = providers;
      this.requestHandler = new RequestHandlerImpl(new RequestDispatcher(resources), providers, dependencies, config);
      if (application != null)
      {
         addApplication(application);
      }
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
      if (application == null)
      {
         throw new NullPointerException("application");
      }
      new ApplicationPublisher(resources, providers).publish(application);
      Set<Object> singletons = application.getSingletons();
      if (singletons != null && singletons.size() > 0)
      {
         for (Object o : singletons)
         {
            singletonsReferences.add(new WeakReference<Object>(o));
         }
      }
   }

   /**
    * @see org.everrest.core.Lifecycle#start()
    */
   @Override
   public void start()
   {
   }

   /**
    * @see org.everrest.core.Lifecycle#stop()
    */
   @Override
   public void stop()
   {
      RuntimeException exception = null;
      for (WeakReference<Object> ref : singletonsReferences)
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
      singletonsReferences.clear();
      if (exception != null)
      {
         throw exception;
      }
   }
}
