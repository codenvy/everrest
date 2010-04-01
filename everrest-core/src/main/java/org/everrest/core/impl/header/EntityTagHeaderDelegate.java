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
package org.everrest.core.impl.header;

import org.everrest.core.header.AbstractHeaderDelegate;

import javax.ws.rs.core.EntityTag;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: EntityTagHeaderDelegate.java 285 2009-10-15 16:21:30Z aparfonov $
 */
public class EntityTagHeaderDelegate extends AbstractHeaderDelegate<EntityTag>
{

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<EntityTag> support()
   {
      return EntityTag.class;
   }

   /**
    * {@inheritDoc}
    */
   public EntityTag fromString(String header)
   {
      if (header == null)
         throw new IllegalArgumentException();

      boolean isWeak = header.startsWith("W/") ? true : false;

      String value;
      // cut 'W/' prefix if exists
      if (isWeak)
         value = header.substring(2);
      else
         value = header;
      // remove quotes
      value = value.substring(1, value.length() - 1);
      value = HeaderHelper.filterEscape(value);

      return new EntityTag(value, isWeak);
   }

   /**
    * {@inheritDoc}
    */
   public String toString(EntityTag entityTag)
   {
      StringBuffer sb = new StringBuffer();
      if (entityTag.isWeak())
         sb.append('W').append('/');

      sb.append('"');
      HeaderHelper.appendEscapeQuote(sb, entityTag.getValue());
      sb.append('"');

      return sb.toString();
   }

}
