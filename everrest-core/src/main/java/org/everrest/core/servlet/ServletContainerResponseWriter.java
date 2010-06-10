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
package org.everrest.core.servlet;

import org.everrest.core.ContainerResponseWriter;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.impl.header.HeaderHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * See {@link ContainerResponseWriter}.
 * 
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: ServletContainerResponseWriter.java 279 2009-10-14 15:58:06Z
 *          aparfonov $
 */
public class ServletContainerResponseWriter implements ContainerResponseWriter
{

   /**
    * See {@link HttpServletResponse}.
    */
   private HttpServletResponse servletResponse;

   /**
    * @param response HttpServletResponse
    */
   public ServletContainerResponseWriter(HttpServletResponse response)
   {
      this.servletResponse = response;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public void writeBody(GenericContainerResponse response, MessageBodyWriter entityWriter) throws IOException
   {
      Object entity = response.getEntity();
      if (entity != null)
      {
         OutputStream out = servletResponse.getOutputStream();
         entityWriter.writeTo(entity, entity.getClass(), response.getEntityType(), null, response.getContentType(),
            response.getHttpHeaders(), out);
         out.flush();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void writeHeaders(GenericContainerResponse response) throws IOException
   {
      if (servletResponse.isCommitted())
         return;

      servletResponse.setStatus(response.getStatus());

      if (response.getHttpHeaders() != null)
      {
         // content-type and content-length should be preset in headers
         for (Map.Entry<String, List<Object>> e : response.getHttpHeaders().entrySet())
         {
            String name = e.getKey();
            for (Object o : e.getValue())
            {
               String value = null;
               if (o != null && (value = HeaderHelper.getHeaderAsString(o)) != null)
                  servletResponse.addHeader(name, value);
            }
         }
      }
   }
}