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

/**
 * Contract of this component is process all requests, initialization and
 * control main components of JAX-RS implementation.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public interface RequestHandler
{

   /**
    * Max buffer size attribute name. Entities that has size greater then
    * specified will be stored in temporary directory on file system during
    * entity processing.
    */
   public static final String WS_RS_BUFFER_SIZE = "ws.rs.buffersize";

   /**
    * Max buffer size attribute value. See {@link #WS_RS_BUFFER_SIZE}.
    */
   public static final int WS_RS_BUFFER_SIZE_VALUE = 204800;

   /**
    * Handle the HTTP request by dispatching request to appropriate resource. If
    * no one appropriate resource found then error response will be produced.
    *
    * @param request HTTP request
    * @param response HTTP response
    * @throws java.io.IOException if any i/o exceptions occurs
    * @throws UnhandledException if any other errors occurs
    */
   void handleRequest(GenericContainerRequest request, GenericContainerResponse response) throws UnhandledException, IOException;

}
