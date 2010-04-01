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
package org.everrest.core.resource;

import org.everrest.core.ExtMultivaluedMap;
import org.everrest.core.impl.header.MediaTypeHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.MediaType;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ResourceMethodMap.java 285 2009-10-15 16:21:30Z aparfonov $
 */
public class ResourceMethodMap<T extends ResourceMethodDescriptor> extends HashMap<String, List<T>> implements
   ExtMultivaluedMap<String, T>
{

   /**
    * Serial version UID. 
    */
   private static final long serialVersionUID = 8930689464134153848L;

   /**
    * Compare list of media types. Each list should be already sorted by
    * {@link MediaTypeHelper#MEDIA_TYPE_COMPARATOR}. So it is enough to compare
    * only last media types in the list. Last media types is the least precise.
    */
   private static final Comparator<ResourceMethodDescriptor> RESOURCE_METHOD_COMPARATOR =
      new Comparator<ResourceMethodDescriptor>()
      {

         public int compare(ResourceMethodDescriptor o1, ResourceMethodDescriptor o2)
         {
            int r = MediaTypeHelper.MEDIA_TYPE_COMPARATOR.compare(getLast(o1.consumes()), getLast(o2.consumes()));
            if (r == 0)
               r = MediaTypeHelper.MEDIA_TYPE_COMPARATOR.compare(getLast(o1.produces()), getLast(o2.produces()));
            // More precise goes first.
            // e.g.
            // [a/b, y/z] and [a/b] then second one goes first 
            if (r == 0)
               r = o1.consumes().size() - o2.consumes().size();
            if (r == 0)
               r = o1.produces().size() - o2.produces().size();
            return r;
         }

         private MediaType getLast(List<MediaType> l)
         {
            return l.get(l.size() - 1);
         }

      };

   /**
    * {@inheritDoc}
    */
   public List<T> getList(String httpMethod)
   {
      List<T> l = get(httpMethod);
      if (l == null)
      {
         l = new ArrayList<T>();
         put(httpMethod, l);
      }
      return l;
   }

   /**
    * {@inheritDoc}
    */
   public void add(String httpMethod, T resourceMethod)
   {
      if (resourceMethod == null)
         return;
      List<T> l = getList(httpMethod);
      l.add(resourceMethod);
   }

   /**
    * {@inheritDoc}
    */
   public T getFirst(String httpMethod)
   {
      List<T> l = getList(httpMethod);
      return l != null && l.size() > 0 ? l.get(0) : null;
   }

   /**
    * {@inheritDoc}
    */
   public void putSingle(String httpMethod, T resourceMethod)
   {
      if (resourceMethod == null)
         return;
      List<T> l = getList(httpMethod);
      l.clear();
      l.add(resourceMethod);
   }

   /**
    * Sort each collections in map.
    */
   public void sort()
   {
      for (List<T> l : values())
         Collections.sort(l, RESOURCE_METHOD_COMPARATOR);
   }

   /**
    * Get HTTP method names to use it in 'Allow' header.
    * 
    * @return collection of method names
    */
   public Collection<String> getAllow()
   {
      return keySet();
   }

}