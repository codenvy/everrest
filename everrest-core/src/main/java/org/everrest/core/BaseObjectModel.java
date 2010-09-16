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
package org.everrest.core;

import org.everrest.core.impl.MultivaluedMapImpl;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

public abstract class BaseObjectModel implements ObjectModel
{

   protected final Class<?> clazz;

   /** Optional data. */
   protected MultivaluedMapImpl properties;

   public BaseObjectModel(Class<?> clazz)
   {
      this.clazz = clazz;
   }

   /**
    * {@inheritDoc}
    */
   public Class<?> getObjectClass()
   {
      return clazz;
   }

   /**
    * {@inheritDoc}
    */
   public MultivaluedMap<String, String> getProperties()
   {
      if (properties == null)
      {
         properties = new MultivaluedMapImpl();
      }
      return properties;
   }

   /**
    * {@inheritDoc}
    */
   public List<String> getProperty(String key)
   {
      if (properties != null)
      {
         return properties.get(key);
      }
      return null;
   }

}
