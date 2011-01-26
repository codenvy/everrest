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

import org.everrest.core.impl.FileCollector;
import org.everrest.core.provider.EntityProvider;

import java.io.File;
import java.io.FileInputStream;
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
public class FileEntityProvider implements EntityProvider<File>
{

   /**
    * {@inheritDoc}
    */
   public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return type == File.class;
   }

   /**
    * {@inheritDoc}
    */
   public File readFrom(Class<File> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException
   {
      File f = FileCollector.getInstance().createFile();
      OutputStream out = new FileOutputStream(f);
      try
      {
         IOHelper.write(entityStream, out);
      }
      finally
      {
         out.close();
      }
      return f;
   }

   /**
    * {@inheritDoc}
    */
   public long getSize(File t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return t.length();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return File.class.isAssignableFrom(type); // more flexible then '=='
   }

   /**
    * {@inheritDoc}
    */
   public void writeTo(File t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException
   {
      InputStream in = new FileInputStream(t);
      try
      {
         IOHelper.write(in, entityStream);
      }
      finally
      {
         in.close();
      }
   }

}
