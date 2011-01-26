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
package org.everrest.core.impl.provider.json;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: BeanWithTransientField.java 34417 2009-07-23 14:42:56Z dkatayev
 *          $
 */
public class BeanWithTransientField
{

   private String field = "visible";

   //@JsonTransient
   transient private String transientField = "invisible";

   public String getField()
   {
      return field;
   }

   public void setField(String field)
   {
      this.field = field;
   }

   public String getTransientField()
   {
      return transientField;
   }

   public void setTransientField(String transientField)
   {
      this.transientField = transientField;
   }

}
