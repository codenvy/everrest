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
package org.everrest.core.util;

import org.everrest.core.ExtMultivaluedMap;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

public class MediaTypeMultivaluedMap<V> extends MediaTypeMap<List<V>> implements ExtMultivaluedMap<MediaType, V>
{
   private static final long serialVersionUID = 7082102018450744774L;

   /**
    * Get {@link List} with specified key. If it does not exist new one be
    * created.
    *
    * @param mediaType MediaType
    * @return List of ProviderFactory if no value mapped to the specified key
    *         then empty list will be returned instead null
    */
   public List<V> getList(MediaType mediaType)
   {
      List<V> l = get(mediaType);
      if (l == null)
      {
         l = new ArrayList<V>();
         put(mediaType, l);
      }
      return l;
   }

   /** {@inheritDoc} */
   public void add(MediaType mediaType, V value)
   {
      if (value == null)
      {
         return;
      }
      List<V> list = getList(mediaType);
      list.add(value);
   }

   /** {@inheritDoc} */
   public V getFirst(MediaType mime)
   {
      List<V> list = get(mime);
      return list != null && list.size() > 0 ? list.get(0) : null;
   }

   /** {@inheritDoc} */
   public void putSingle(MediaType mediaType, V value)
   {
      if (value == null)
      {
         remove(mediaType);
         return;
      }
      List<V> list = getList(mediaType);
      list.clear();
      list.add(value);
   }
}
