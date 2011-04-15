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

import org.everrest.core.impl.header.MediaTypeHelper;

import java.util.Comparator;

import javax.ws.rs.core.MediaType;

/**
 * Keeps sorted values.
 * 
 * @param <T> actual value type
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class MediaTypeMap<T> extends java.util.TreeMap<MediaType, T>
{

   /**
    * Serial Version UID.
    */
   private static final long serialVersionUID = -4713556573521776577L;

   /**
    * Create new instance of MedaTypeMap with {@link Comparator}.
    */
   public MediaTypeMap()
   {
      super(COMPARATOR);
   }

   /**
    * See {@link Comparator}.
    */
   static final Comparator<MediaType> COMPARATOR = new Comparator<MediaType>()
   {

      /**
       * Compare two {@link MediaType}.
       * 
       * @param o1 first MediaType to be compared
       * @param o2 second MediaType to be compared
       * @return result of comparison
       * @see Comparator#compare(Object, Object)
       * @see MediaTypeHelper
       * @see MediaType
       */
      public int compare(MediaType o1, MediaType o2)
      {
         int r = MediaTypeHelper.MEDIA_TYPE_COMPARATOR.compare(o1, o2);
         // If media type has the same 'weight' (i.e. 'application/xml' and
         // 'text/xml' has the same 'weight'), then order does not matter but
         // should e compared lexicographically, otherwise new entry with the
         // same 'weight' will be not added in map.
         if (r == 0)
            // TODO weak solution
            r = _toString(o1).compareToIgnoreCase(_toString(o2));
         return r;
      }

      private String _toString(MediaType mime)
      {
         return mime.getType() + "/" + mime.getSubtype();
      }

   };

}
