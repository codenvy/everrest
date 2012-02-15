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
package org.everrest.core.impl.provider;

import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.provider.EntityProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
@Provider
public class ReaderEntityProvider implements EntityProvider<Reader>
{
   /** {@inheritDoc} */
   public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return type == Reader.class;
   }

   /** {@inheritDoc} */
   public Reader readFrom(Class<Reader> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                          MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException
   {
      String cs = mediaType != null ? mediaType.getParameters().get("charset") : null;
      Charset charset = cs != null ? Charset.forName(cs) : IOHelper.DEFAULT_CHARSET;

      ApplicationContext context = ApplicationContextImpl.getCurrent();
      if (context.isAsynchronous())
      {
         // If request is asynchronous spool content of stream to file or memory.
         Integer bufferSize = (Integer)context.getAttributes().get(EverrestConfiguration.EVERREST_MAX_BUFFER_SIZE);
         return new InputStreamReader(IOHelper.bufferStream(entityStream, bufferSize), charset);
      }

      return new InputStreamReader(entityStream, charset);
   }

   /** {@inheritDoc} */
   public long getSize(Reader t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return -1;
   }

   /** {@inheritDoc} */
   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return Reader.class.isAssignableFrom(type);
   }

   /** {@inheritDoc} */
   public void writeTo(Reader t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                       MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException
   {
      Writer out = new OutputStreamWriter(entityStream);
      try
      {
         IOHelper.write(t, out);
      }
      finally
      {
         out.flush();
         t.close();
      }
   }

}
