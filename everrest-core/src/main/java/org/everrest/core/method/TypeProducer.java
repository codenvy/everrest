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
package org.everrest.core.method;

import javax.ws.rs.core.MultivaluedMap;

/**
 * Can create object by using String value. For each type of object should be
 * created new instance of TypeProducer.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: TypeProducer.java 285 2009-10-15 16:21:30Z aparfonov $
 */
public interface TypeProducer
{

   /**
    * @param param parameter name, parameter name should be getting from
    *          parameter annotation
    * @param values all value which can be used for construct object, it can be
    *          header parameters, path parameters, query parameters, etc
    * @param defaultValue default value which can be used if in value can't be
    *          found required value with name <i>param</i>
    * @return newly created object
    * @throws Exception if any errors occurs
    */
   Object createValue(String param, MultivaluedMap<String, String> values, String defaultValue) throws Exception;

}
