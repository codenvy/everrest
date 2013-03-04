/*
 * Copyright (C) 2013 eXo Platform SAS.
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

/** Call "initialize" and "destroy" methods of object. */
public interface LifecycleMethodStrategy
{
   /**
    * Call "initialize" method on the specified object. It is up to the implementation how to find "initialize"
    * method. It is possible to have more than one initialize method but any particular order of methods invocation
    * is not guaranteed.
    *
    * @param o the object
    * @throws org.everrest.core.impl.InternalException if initialize method throws any exception
    */
   void invokeInitializeMethods(Object o);

   /**
    * Call "destroy" method on the specified object. It is up to the implementation how to find "destroy" method. It
    * is possible to have more than one destroy method but any particular order of methods invocation is not
    * guaranteed.
    *
    * @param o the object
    * @throws org.everrest.core.impl.InternalException if destroy method throws any exception
    */
   void invokeDestroyMethods(Object o);
}
