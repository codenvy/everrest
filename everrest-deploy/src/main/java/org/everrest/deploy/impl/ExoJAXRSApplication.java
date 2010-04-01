/*
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.everrest.deploy.impl;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.everrest.core.resource.ResourceContainer;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: ExoJAXRSApplication.java 436 2009-10-28 06:47:29Z aparfonov $
 */
public class ExoJAXRSApplication extends Application implements Startable
{

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger(ExoJAXRSApplication.class);

   private ExoContainer container;

   private Set<Class<?>> cls = new HashSet<Class<?>>();

   private Set<Object> singletons = new HashSet<Object>();

   public ExoJAXRSApplication(InitParams initParams, ExoContainerContext containerContext)
   {
      if (initParams != null)
      {
         for (Object cl : initParams.getValuesParam("everrest.components").getValues())
         {
            try
            {
               cls.add(Class.forName((String)cl));
            }
            catch (ClassNotFoundException e)
            {
               LOG.error("Failed load class " + cl, e);
            }
         }
      }
      container = containerContext.getContainer();
   }

   /**
    * {@inheritDoc}
    */
   public Set<Class<?>> getClasses()
   {
      return cls;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Set<Object> getSingletons()
   {
      return singletons;
   }

   /**
    * {@inheritDoc}
    */
   public void start()
   {
      for (Object resource : container.getComponentInstancesOfType(ResourceContainer.class))
         singletons.add(resource);
   }

   /**
    * {@inheritDoc}
    */
   public void stop()
   {
   }

}
