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

import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.util.Logger;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.picocontainer.Startable;

import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.Application;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class EverrestInitializer implements Startable
{
   private static final Logger log = Logger.getLogger(EverrestInitializer.class);

   private static int appNameCounter = 0;

   private final ExoContainer container;
   private final ResourceBinder resources;
   private final ProvidersRegistry providersRegistry;

   public EverrestInitializer(ExoContainerContext containerContext, ResourceBinder resources,
      ProvidersRegistry providersRegistry, StartableApplication eXo /* Be sure eXo components are initialized. */)
   {
      this.resources = resources;
      this.providersRegistry = providersRegistry;
      this.container = containerContext.getContainer();
   }

   /**
    * @see org.picocontainer.Startable#start()
    */
   @SuppressWarnings("rawtypes")
   @Override
   public void start()
   {
      List allApps = container.getComponentInstancesOfType(Application.class);
      if (allApps != null && !allApps.isEmpty())
      {
         for (Iterator iter = allApps.iterator(); iter.hasNext();)
            addApplication((Application)iter.next());
      }
   }

   /**
    * @see org.picocontainer.Startable#stop()
    */
   @Override
   public void stop()
   {
   }

   public void addApplication(Application application)
   {
      String applicationName = generateApplicationName(application);
      ApplicationProviderBinder applicationProviders = new ApplicationProviderBinder();
      new ExoApplicationPublisher(resources, applicationProviders).publish(new ApplicationConfiguration(
         applicationName, application));
      providersRegistry.addProviders(applicationName, applicationProviders);
      if (log.isDebugEnabled())
         log.debug("JAX-RS Application " + applicationName + ", class: " + application.getClass().getName()
            + " registered. ");
   }

   protected synchronized String generateApplicationName(Application application)
   {
      appNameCounter++;
      return "application" + appNameCounter;
   }
}
