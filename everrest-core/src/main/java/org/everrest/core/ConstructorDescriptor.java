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
package org.everrest.core;

import org.everrest.core.resource.ResourceDescriptor;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Abstraction of constructor descriptor. Used for create object instance when
 * type is used in per-request lifecycle.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public interface ConstructorDescriptor extends ResourceDescriptor
{

   /**
    * @param context ApplicationContext
    * @return newly created instance of the constructor's
    * @see ApplicationContext
    */
   Object createInstance(ApplicationContext context);

   /**
    * Get source constructor.
    * 
    * @return constructor
    * @see Constructor
    */
   Constructor<?> getConstructor();

   /**
    * @return constructor's parameters
    */
   List<ConstructorParameter> getParameters();

}
