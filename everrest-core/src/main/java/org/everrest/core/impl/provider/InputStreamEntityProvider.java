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
import org.everrest.core.RequestHandler;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.FileCollector;
import org.everrest.core.provider.EntityProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
@Provider
public class InputStreamEntityProvider implements EntityProvider<InputStream>
{
   /**
    * {@inheritDoc}
    */
   public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return type == InputStream.class;
   }

   /**
    * {@inheritDoc}
    */
   public InputStream readFrom(Class<InputStream> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException
   {
      ApplicationContext context = ApplicationContextImpl.getCurrent();
      boolean async = Boolean.parseBoolean(context.getQueryParameters().getFirst("async"));
      if (async)
      {
         // If request is asynchronous spool content of stream to file or memory.
         int bufferSize =
            context.getProperties().get(RequestHandler.WS_RS_BUFFER_SIZE) == null
               ? RequestHandler.WS_RS_BUFFER_SIZE_VALUE : Integer.parseInt(context.getProperties().get(
                  RequestHandler.WS_RS_BUFFER_SIZE));

         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         byte[] b = new byte[8192];
         int r;
         boolean overflow = false;
         while ((!overflow) && (r = entityStream.read(b)) != -1)
         {
            bos.write(b, 0, r);
            overflow = bos.size() > bufferSize;
         }

         if (overflow)
         {
            File f = FileCollector.getInstance().createFile();

            FileOutputStream fos = new FileOutputStream(f);
            bos.writeTo(fos);
            while ((r = entityStream.read(b)) != -1)
            {
               fos.write(b, 0, r);
            }
            fos.close();
            return new DeleteOnCloseFIS(f);
         }
         else
         {
            return new ByteArrayInputStream(bos.toByteArray());
         }
      }
      return entityStream;
   }
   
   private static final class DeleteOnCloseFIS extends FileInputStream
   {
      private final File file;

      public DeleteOnCloseFIS(File file) throws FileNotFoundException
      {
         super(file);
         this.file = file;
      }

      @Override
      public void close() throws IOException
      {
         try
         {
            super.close();
         }
         finally
         {
            if (file.exists())
            {
               file.delete();
            }
         }
      }
   }


   /**
    * {@inheritDoc}
    */
   public long getSize(InputStream t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return -1;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return InputStream.class.isAssignableFrom(type);
   }

   /**
    * {@inheritDoc}
    */
   public void writeTo(InputStream t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException
   {
      try
      {
         IOHelper.write(t, entityStream);
      }
      finally
      {
         t.close();
      }
   }
}
