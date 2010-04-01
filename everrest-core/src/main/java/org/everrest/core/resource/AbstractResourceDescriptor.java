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
package org.everrest.core.resource;

import org.everrest.core.ObjectModel;
import org.everrest.core.impl.resource.PathValue;
import org.everrest.core.uri.UriPattern;

/**
 * Describe Resource Class or Root Resource Class. Resource Class is any Java
 * class that uses JAX-RS annotations to implement corresponding Web resource.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: AbstractResourceDescriptor.java 285 2009-10-15 16:21:30Z aparfonov $
 */
public interface AbstractResourceDescriptor extends ResourceDescriptor, ObjectModel
{

   /**
    * @return See {@link PathValue}
    */
   PathValue getPathValue();

   /**
    * @see ResourceMethodDescriptor
    * @return resource methods
    */
   ResourceMethodMap<ResourceMethodDescriptor> getResourceMethods();

   /**
    * @see SubResourceLocatorDescriptor
    * @return sub-resource locators
    */
   SubResourceLocatorMap getSubResourceLocators();

   /**
    * @see SubResourceMethodDescriptor
    * @return sub-resource methods
    */
   SubResourceMethodMap getSubResourceMethods();

   /**
    * @return See {@link UriPattern}
    */
   UriPattern getUriPattern();

   /**
    * @return true if resource is root resource false otherwise. Root resource is
    *         class which has own {@link javax.ws.rs.Path} annotation
    */
   boolean isRootResource();

}
