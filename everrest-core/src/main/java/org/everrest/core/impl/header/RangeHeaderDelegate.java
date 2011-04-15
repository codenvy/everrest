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

package org.everrest.core.impl.header;

import org.everrest.core.header.AbstractHeaderDelegate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class RangeHeaderDelegate extends AbstractHeaderDelegate<Ranges>
{

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<Ranges> support()
   {
      return Ranges.class;
   }

   /**
    * {@inheritDoc}
    */
   public Ranges fromString(String value) throws IllegalArgumentException
   {
      if (value == null)
      {
         throw new IllegalArgumentException("null");
      }
      if (!value.startsWith("bytes"))
      {
         throw new IllegalArgumentException("Invalid byte range.");
      }

      value = value.substring(value.indexOf("=") + 1);

      String[] tokens = value.split(",");
      List<Ranges.Range> r = new ArrayList<Ranges.Range>();
      for (String token : tokens)
      {
         long start = 0;
         long end = -1L;
         token = token.trim();
         int dash = token.indexOf("-");
         if (dash == -1)
         {
            throw new IllegalArgumentException("Invalid byte range.");
         }
         else if (dash == 0)
         {
            start = Long.parseLong(token);
         }
         else if (dash > 0)
         {
            start = Long.parseLong(token.substring(0, dash).trim());
            if (dash < token.length() - 1)
            {
               end = Long.parseLong(token.substring(dash + 1, token.length()).trim());
            }
         }
         r.add(new Ranges.Range(start, end));
      }
      return new Ranges(r);
   }

   /**
    * {@inheritDoc}
    */
   public String toString(Ranges value)
   {
      throw new UnsupportedOperationException();
   }

}
