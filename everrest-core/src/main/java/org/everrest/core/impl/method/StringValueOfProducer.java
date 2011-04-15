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

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class StringValueOfProducer extends BaseTypeProducer
{

   /**
    * This method will be used for creation object.
    */
   private Method valueOfMethod;

   /**
    * @param valueOfMethod static method with single String parameter
    */
   StringValueOfProducer(Method valueOfMethod)
   {
      this.valueOfMethod = valueOfMethod;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected Object createValue(String value) throws Exception
   {
      if (value == null)
         return null;

      return valueOfMethod.invoke(null, value);
   }

}
