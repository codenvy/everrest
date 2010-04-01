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

import org.everrest.core.resource.AbstractResourceDescriptor;

import java.util.List;

/**
 * Manages root resources.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ResourceBinder.java -1   $
 */
public interface ResourceBinder
{
   /**
    * @param resourceClass class of candidate to be root resource
    * @return true if resource was bound and false if resource was not bound
    *         cause it is not root resource
    */
   boolean bind(Class<?> resourceClass);

   /**
    * Register supplied Object as root resource if it has valid JAX-RS
    * annotations and no one resource with the same UriPattern already
    * registered.
    * 
    * @param resource candidate to be root resource
    * @return true if resource was bound and false if resource was not bound
    *         cause it is not root resource
    */
   boolean bind(Object resource);

   /**
    * @return all registered root resources
    */
   List<ObjectFactory<AbstractResourceDescriptor>> getResources();

   /**
    * @return number of bound resources
    */
   int getSize();

   /**
    * Remove root resource of supplied class from root resource collection.
    * 
    * @param clazz root resource class
    * @return true if resource was unbound false otherwise
    */
   boolean unbind(Class<?> clazz);

   boolean unbind(String uriTemplate);

}
