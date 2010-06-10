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
package org.everrest.core.impl;

import org.everrest.core.ResourceBinder;

import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: ApplicationPublisher.java -1 $
 */
public class ApplicationPublisher
{

   protected RestComponentResolver resolver;

   public ApplicationPublisher(ResourceBinder resources, ProviderBinder providers)
   {
      resolver = new RestComponentResolver(resources, providers);
   }

   @SuppressWarnings("unchecked")
   public void publish(Application application)
   {
      Set<Object> singletons = application.getSingletons();
      if (singletons != null)
      {
         for (Object instance : singletons)
            resolver.addSingleton(instance);
      }
      Set<Class<?>> perRequests = application.getClasses();
      if (perRequests != null)
      {
         for (Class clazz : perRequests)
            resolver.addPerRequest(clazz);
      }
   }

}
