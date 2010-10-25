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

/**
 * Implementation of this interface should be able provide object instance
 * dependent of component lifecycle.
 * 
 * @param <T> ObjectModel extensions
 * @see ObjectModel
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public interface ObjectFactory<T extends ObjectModel>
{

   /**
    * Create object instance. ApplicationContext can be used for getting
    * required parameters for object constructors or fields.
    * 
    * @param context ApplicationContext
    * @return object instance
    */
   Object getInstance(ApplicationContext context);

   /**
    * @return any extension of {@link ObjectModel}. That must allows create
    *         object instance and initialize object's fields for per-request
    *         resources
    */
   T getObjectModel();

}
