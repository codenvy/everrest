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
package org.everrest.json;

import org.everrest.json.value.JsonValue;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: JsonGenerator.java 34417 2009-07-23 14:42:56Z dkatayev $
 */
public interface JsonGenerator
{

   /**
    * Get HashMap representation for Java Bean object.
    * 
    * @param bean the source object.
    * @return the HashMap which represented give JavaBean.
    * @throws JsonException if Object can't be transformed in JSON
    *         representation.
    */
   JsonValue createJsonObject(Object bean) throws JsonException;

}
