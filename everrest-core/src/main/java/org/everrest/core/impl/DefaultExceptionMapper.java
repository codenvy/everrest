/**
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.everrest.core.impl;

import org.everrest.core.ExtHttpHeaders;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Transform {@link java.lang.Exception} to JAX-RS response.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: DefaultExceptionMapper.java 2262 2010-04-09 15:21:21Z aparfonov $
 */
public class DefaultExceptionMapper implements ExceptionMapper<Exception>
{

   /**
    * {@inheritDoc}
    */
   public Response toResponse(Exception exception)
   {
      String message = exception.getMessage();
      return Response.status(500).entity(message == null ? exception.getClass().getName() : message).type(
         MediaType.TEXT_PLAIN).header(ExtHttpHeaders.JAXRS_BODY_PROVIDED, "Error-Message").build();
   }

}
