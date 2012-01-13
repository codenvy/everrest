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

import java.io.IOException;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public interface GenericContainerResponse
{

   /**
    * Set response. New response can override old one.
    * 
    * @param response See {@link Response}
    */
   void setResponse(Response response);

   /**
    * Get preset {@link Response}. This method can be useful for modification
    * {@link GenericContainerResponse}. See
    * {@link ResponseFilter#doFilter(GenericContainerResponse)}.
    * 
    * @return preset {@link Response} or null if it was not initialized yet.
    */
   Response getResponse();

   /**
    * Write response to output stream.
    * 
    * @throws IOException if any i/o errors occurs
    */
   void writeResponse() throws IOException;

   /**
    * @return HTTP status
    */
   int getStatus();

   /**
    * @return HTTP headers
    */
   MultivaluedMap<String, Object> getHttpHeaders();

   /**
    * @return entity body
    */
   Object getEntity();

   /**
    * @return entity type
    */
   Type getEntityType();

   /**
    * @return body content type
    */
   MediaType getContentType();

}
