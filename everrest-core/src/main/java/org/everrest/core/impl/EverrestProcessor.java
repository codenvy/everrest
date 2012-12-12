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
import org.everrest.core.util.Logger;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class EverrestProcessor implements Lifecycle
{
   private static final Logger LOG = Logger.getLogger(EverrestProcessor.class.getName());
   private final RequestHandler requestHandler;
   private final ResourceBinder resources;
   private final ProviderBinder providers;
   private final List<WeakReference<Object>> singletonsReferences = new ArrayList<WeakReference<Object>>();

   public EverrestProcessor(ResourceBinder resources, ProviderBinder providers, DependencySupplier dependencies,
                            EverrestConfiguration config, Application application)
   {
      this(new RequestHandlerImpl(
         new RequestDispatcher(resources), providers, dependencies, config),
         resources,
         providers,
         application);
   }

   // TODO : workaround to make possible use customized request handler. typically not need this for everrest itself but
   // may be useful for some integration. At the moment need this to get work exo integration through web sockets.
   // Need to find something smarter to configure RequestHandler.
   protected EverrestProcessor(RequestHandler handler, ResourceBinder resources, ProviderBinder providers,
                               Application application)
   {
      this.resources = resources;
      this.providers = providers;
      this.requestHandler = handler;
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

   /** @see org.everrest.core.Lifecycle#start() */
   @Override
   public void start()
   {
   }

   /** @see org.everrest.core.Lifecycle#stop() */
   @Override
   public void stop()
   {
      for (WeakReference<Object> ref : singletonsReferences)
      {
         Object o = ref.get();
         if (o != null)
         {
            try
            {
               new LifecycleComponent(o).destroy();
            }
            catch (InternalException e)
            {
               LOG.error("Unable to destroy component. ", e);
            }
         }
      }
      singletonsReferences.clear();
   }
}
