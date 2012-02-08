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

import org.everrest.core.ApplicationContext;
import org.everrest.core.InitialProperties;
import org.everrest.core.Property;

/**
 * Obtain value of property (see {@link InitialProperties}) with name supplied
 * in {@link Property#value()} .
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class PropertyResolver extends ParameterResolver<Property>
{
   /** See {@link Property} */
   private final Property property;

   /** @param property Property */
   PropertyResolver(Property property)
   {
      this.property = property;
   }

   /** {@inheritDoc} */
   public Object resolve(org.everrest.core.Parameter parameter, ApplicationContext context) throws Exception
   {
      if (parameter.getParameterClass() != String.class)
      {
         throw new IllegalArgumentException(
            "Only parameters and fields with string type may be annotated by @Property.");
      }
      String param = this.property.value();

      Object value = context.getInitialProperties().getProperties().get(param);
      if (value == null)
      {
         return parameter.getDefaultValue();
      }

      return value;
   }
}
