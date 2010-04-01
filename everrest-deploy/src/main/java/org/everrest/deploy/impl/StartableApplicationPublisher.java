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

import java.lang.annotation.Annotation;
import java.util.List;

import javax.ws.rs.core.Application;

import org.everrest.core.DependencySupplier;
import org.everrest.core.FieldInjector;
import org.everrest.core.Inject;
import org.everrest.core.Parameter;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationPublisher;
import org.everrest.core.impl.ProviderBinder;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: StartableApplicationPublisher.java 436 2009-10-28 06:47:29Z aparfonov $
 */
public class StartableApplicationPublisher extends ApplicationPublisher implements DependencySupplier, Startable
{

   private ExoContainer container;

   public StartableApplicationPublisher(ResourceBinder resources, ExoContainerContext containerContext)
   {
      super(resources, ProviderBinder.getInstance());
      container = containerContext.getContainer();
   }

   /**
    * {@inheritDoc}
    */
   public Object getComponent(Parameter parameter)
   {
      // Container can different.
      ExoContainer container = ExoContainerContext.getCurrentContainer();

      if (parameter instanceof FieldInjector)
      {
         for (Annotation a : parameter.getAnnotations())
         {
            // Do not process fields without annotation Inject
            if (a.annotationType() == Inject.class)
            {
               return container.getComponentInstanceOfType(parameter.getParameterClass());
            }
         }
      }
      return container.getComponentInstanceOfType(parameter.getParameterClass());
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public void start()
   {
      List<Application> applications = container.getComponentInstancesOfType(Application.class);
      for (Application application : applications)
         publish(application);
   }

   /**
    * {@inheritDoc}
    */
   public void stop()
   {
   }

}
