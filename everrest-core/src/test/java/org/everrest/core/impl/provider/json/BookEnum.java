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

package org.everrest.core.impl.provider.json;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public enum BookEnum {
   JUNIT_IN_ACTION(new Book("Vincent Masson", "JUnit in Action", 19.37, 93011099534534L, 386, true, false)), //
   BEGINNING_C(new Book("Christian Gross", "Beginning C# 2008 from novice to professional", 23.56, 9781590598696L, 511,
      false, false));
   private final Book book;

   private BookEnum(Book book)
   {
      this.book = book;
   }

   @Override
   public String toString()
   {
      return book.toString();
   }
}
