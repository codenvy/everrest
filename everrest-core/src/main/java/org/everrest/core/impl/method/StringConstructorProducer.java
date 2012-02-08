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
package org.everrest.core.impl.method;

import java.lang.reflect.Constructor;

/**
 * Create object which has constructor with single String parameter.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: StringConstructorProducer.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
 */
public final class StringConstructorProducer extends BaseTypeProducer
{
   /** Constructor which must be used for creation object. */
   private Constructor<?> constructor;

   /**
    * @param constructor this constructor will be used for creation instance of
    * object
    */
   StringConstructorProducer(Constructor<?> constructor)
   {
      this.constructor = constructor;
   }

   /** {@inheritDoc} */
   @Override
   protected Object createValue(String value) throws Exception
   {
      if (value == null)
      {
         return null;
      }

      return constructor.newInstance(value);
   }
}
