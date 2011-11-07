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
import org.everrest.core.impl.ApplicationPublisher;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.FileCollector;
import org.everrest.core.impl.FileCollectorDestroyer;
import org.everrest.core.impl.InternalException;
import org.everrest.core.impl.LifecycleComponent;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.core.impl.method.filter.SecurityConstraint;
import org.everrest.core.util.Logger;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.picocontainer.Startable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;

/**
 * Load all available instances of {@link Application} (include {@link StartableApplication}) from ExoContainer and
 * deploy them. EverrestInitializer should be used when components of EverRest framework registered in ExoContainer.
 * Process of ExoContainer bootstrap is opaque for EverrestInitializer.
 * <p>
 * It is NOT expected to use EverrestInitializer and sub-classes of
 * {@link org.everrest.exoplatform.servlet.EverrestExoContextListener} in same web application. Use
 * {@link org.everrest.exoplatform.servlet.EverrestExoServlet} instead of
 * {@link org.everrest.core.servlet.EverrestServlet}.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class EverrestInitializer implements Startable
{
   private static final Logger LOG = Logger.getLogger(EverrestInitializer.class);

   private static final AtomicLong appNameCounter = new AtomicLong(1);

   private final ExoContainer container;
   private final ResourceBinder resources;
   private final ProvidersRegistry providersRegistry;
   private final EverrestConfiguration config;
   private List<WeakReference<Object>> singletonsReferences;

   public EverrestInitializer(ExoContainerContext containerContext, ResourceBinder resources,
      ProvidersRegistry providersRegistry, StartableApplication eXo /* Be sure eXo components are initialized. */,
      InitParams initParams)
   {
      this.resources = resources;
      this.providersRegistry = providersRegistry;
      this.container = containerContext.getContainer();
      this.config = EverrestConfigurationHelper.createEverrestConfiguration(initParams);
   }

   public EverrestInitializer(ExoContainerContext containerContext, ResourceBinder resources,
      ProvidersRegistry providersRegistry, StartableApplication eXo /* Be sure eXo components are initialized. */)
   {
      this(containerContext, resources, providersRegistry, eXo, null);
   }

   /**
    * @see org.picocontainer.Startable#start()
    */
   @SuppressWarnings("rawtypes")
   @Override
   public void start()
   {
      Application everrest = new Application()
      {
         private final Set<Class<?>> classes = new HashSet<Class<?>>(1);
         private final Set<Object> singletons = new HashSet<Object>(2);

         @Override
         public Set<Class<?>> getClasses()
         {
            return classes;
         }

         @Override
         public Set<Object> getSingletons()
         {
            return singletons;
         }
      };

      if (config.isAsynchronousSupported())
      {
         everrest.getSingletons().add(new ExoAsynchronousJobPool(config));
         everrest.getClasses().add(AsynchronousJobService.class);
      }
      if (config.isCheckSecurity())
      {
         everrest.getSingletons().add(new SecurityConstraint());
      }

      // Do not prevent GC remove objects if they are removed somehow from ResourceBinder or ProviderBinder.
      // NOTE We provider life cycle control ONLY for internal components and do nothing for components
      // obtained from container. Container must take care about its components. 
      Set<Object> singletons = everrest.getSingletons();
      singletonsReferences = new ArrayList<WeakReference<Object>>(singletons.size());
      for (Object o : singletons)
      {
         singletonsReferences.add(new WeakReference<Object>(o));
      }
      // Publish components of EverRest framework.
      new ApplicationPublisher(resources, ProviderBinder.getInstance()).publish(everrest);

      // Process applications.
      List allApps = container.getComponentInstancesOfType(Application.class);
      if (allApps != null && !allApps.isEmpty())
      {
         for (Object o : allApps)
         {
            addApplication((Application)o);
         }
      }
   }

   /**
    * @see org.picocontainer.Startable#stop()
    */
   @Override
   public void stop()
   {
      makeFileCollectorDestroyer().stopFileCollector();
      if (singletonsReferences != null && singletonsReferences.size() > 0)
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
               catch (WebApplicationException e)
               {
                  LOG.error(e.getMessage(), e);
               }
               catch (InternalException e)
               {
                  LOG.error(e.getMessage(), e);
               }
            }
         }
         singletonsReferences.clear();
      }
   }

   protected FileCollectorDestroyer makeFileCollectorDestroyer()
   {
      // Always stop FileCollector without checking FileCollector class loader.
      return new FileCollectorDestroyer()
      {
         @Override
         public void stopFileCollector()
         {
            FileCollector fc = FileCollector.getInstance();
            fc.stop();
         }
      };
   }

   public void addApplication(Application application)
   {
      String applicationName = "application" + appNameCounter.getAndIncrement();
      ApplicationProviderBinder applicationProviders = new ApplicationProviderBinder();
      new ExoApplicationPublisher(resources, applicationProviders).publish(new ApplicationConfiguration(
         applicationName, application));
      providersRegistry.addProviders(applicationName, applicationProviders);
      if (LOG.isDebugEnabled())
      {
         LOG.debug("JAX-RS Application " + applicationName + ", class: " + application.getClass().getName()
            + " registered. ");
      }
   }
}
