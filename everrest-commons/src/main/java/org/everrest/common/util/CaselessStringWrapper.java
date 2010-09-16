/*
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
package org.everrest.common.util;

/**
 * Caseless wrapper for strings.
 */
public class CaselessStringWrapper
{

   private final String string;

   private final String caselessString;

   public CaselessStringWrapper(String string)
   {
      this.string = string;
      if (string != null)
      {
         this.caselessString = string.toLowerCase();
      }
      else
      {
         this.caselessString = null;
      }
   }

   public String toString()
   {
      return string == null ? "null" : string;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;
      if (obj.getClass() != getClass())
         return false;
      CaselessStringWrapper other = (CaselessStringWrapper)obj;
      return (caselessString == null && other.caselessString == null)
         || (caselessString != null && caselessString.equals(other.caselessString));
   }

   @Override
   public int hashCode()
   {
      return caselessString == null ? 0 : caselessString.hashCode();
   }

}
