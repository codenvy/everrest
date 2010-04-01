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

import java.util.List;

import javax.ws.rs.core.MediaType;

/**
 * Describe resource method. Resource method is method of resource class which
 * has annotation {@link javax.ws.rs.HttpMethod}, e.g. {@link javax.ws.rs.GET}
 * and has not {@link javax.ws.rs.Path} annotation.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ResourceMethodDescriptor.java 285 2009-10-15 16:21:30Z aparfonov $
 */
public interface ResourceMethodDescriptor extends ResourceDescriptor, GenericMethodResource
{

   /**
    * Get HTTP method name.
    * 
    * @return HTTP method name
    */
   String getHttpMethod();

   /**
    * Get list of {@link MediaType} which current method consumes.
    * 
    * @return list of media types
    */
   List<MediaType> consumes();

   /**
    * Get list of {@link MediaType} which current method produces.
    * 
    * @return list of media types
    */
   List<MediaType> produces();

}
