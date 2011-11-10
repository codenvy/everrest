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
package org.everrest.core.tools;

import org.everrest.core.ContainerResponseWriter;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.impl.OutputHeadersMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * Mock object that can be used for any tests.
 * 
 * @see ContainerResponseWriter
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class ByteArrayContainerResponseWriter implements ContainerResponseWriter
{
   /**
    * Message body.
    */
   private byte[] body;

   /**
    * HTTP headers.
    */
   private MultivaluedMap<String, Object> headers;

   private boolean commited;

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   public void writeBody(GenericContainerResponse response, MessageBodyWriter entityWriter) throws IOException
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Object entity = response.getEntity();
      if (entity != null)
      {
         entityWriter.writeTo(entity, entity.getClass(), response.getEntityType(), null, response.getContentType(),
            response.getHttpHeaders(), out);
         body = out.toByteArray();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void writeHeaders(GenericContainerResponse response) throws IOException
   {
      if (commited)
         throw new IllegalStateException("Response has been commited. Unable write headers. ");
      headers = new OutputHeadersMap(response.getHttpHeaders());
      commited = true;
   }

   /**
    * @return message body
    */
   public byte[] getBody()
   {
      return body;
   }

   /**
    * @return HTTP headers
    */
   public MultivaluedMap<String, Object> getHeaders()
   {
      return headers;
   }

   /**
    * Clear message body and HTTP headers map.
    */
   public void reset()
   {
      body = null;
      headers = null;
      commited = false;
   }

}
