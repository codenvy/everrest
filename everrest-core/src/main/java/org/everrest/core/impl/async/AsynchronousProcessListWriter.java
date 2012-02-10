/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.everrest.core.impl.async;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@Provider
@Produces(MediaType.TEXT_PLAIN)
public class AsynchronousProcessListWriter implements MessageBodyWriter<Iterable<AsynchronousProcess>>
{
   private static final String OUTPUT_FORMAT = "%-30s%-10s%-10s%s%n";

   @Override
   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      if (Iterable.class.isAssignableFrom(type) && (genericType instanceof ParameterizedType))
      {
         Type[] types = ((ParameterizedType)genericType).getActualTypeArguments();
         return types.length == 1 && types[0] == AsynchronousProcess.class;
      }
      return false;
   }

   @Override
   public long getSize(Iterable<AsynchronousProcess> asynchronousProcesses,
                       Class<?> type,
                       Type genericType,
                       Annotation[] annotations,
                       MediaType mediaType)
   {
      return -1;
   }

   @Override
   public void writeTo(Iterable<AsynchronousProcess> asynchronousProcesses,
                       Class<?> type, Type genericType,
                       Annotation[] annotations,
                       MediaType mediaType,
                       MultivaluedMap<String, Object> httpHeaders,
                       OutputStream entityStream) throws IOException, WebApplicationException
   {
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(entityStream));
      try
      {
         writer.format(OUTPUT_FORMAT, "USER", "ID", "STAT", "PATH");
         for (AsynchronousProcess process : asynchronousProcesses)
         {
            writer.format(OUTPUT_FORMAT, process.getOwner(), process.getId(), process.getStatus(), process.getPath());
         }
      }
      finally
      {
         writer.flush();
      }
   }
}
