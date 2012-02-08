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

package org.everrest.core.impl.provider.ext;

import org.everrest.core.impl.provider.FileEntityProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * This provider useful in environment where need disable access to file system.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
@Provider
public class NoFileEntityProvider extends FileEntityProvider
{
   /** {@inheritDoc} */
   @Override
   public long getSize(File t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(
         "File is not supported as method's parameter.").type(MediaType.TEXT_PLAIN).build());
   }

   /** {@inheritDoc} */
   @Override
   public File readFrom(Class<File> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, String> httpHeaders,
                        InputStream entityStream) throws IOException
   {
      throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(
         "File is not supported as method's parameter.").type(MediaType.TEXT_PLAIN).build());
   }

   /** {@inheritDoc} */
   @Override
   public void writeTo(File t,
                       Class<?> type,
                       Type genericType,
                       Annotation[] annotations,
                       MediaType mediaType,
                       MultivaluedMap<String, Object> httpHeaders,
                       OutputStream entityStream) throws IOException
   {
      throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(
         "File is not supported as method's parameter.").type(MediaType.TEXT_PLAIN).build());
   }
}
