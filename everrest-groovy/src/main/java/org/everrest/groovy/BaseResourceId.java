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

package org.everrest.groovy;

/**
 * Base implementation of ResourceId.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: BaseResourceId.java 2663 2010-06-18 13:50:27Z aparfonov $
 */
public class BaseResourceId implements ResourceId
{

   protected final String id;

   public BaseResourceId(String id)
   {
      this.id = id;
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      return id;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      return id.equals(((BaseResourceId)obj).id);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      return id.hashCode();
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      return getClass().getSimpleName() + '(' + id + ')';
   }
}
