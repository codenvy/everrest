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

import org.apache.commons.fileupload.DefaultFileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.everrest.core.ApplicationContext;
import org.everrest.core.RequestHandler;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.FileCollector;
import org.everrest.core.provider.EntityProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 * Processing multipart data based on apache fileupload.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: MultipartFormDataEntityProvider.java 497 2009-11-08 13:19:25Z aparfonov $
 */
@Provider
@Consumes({"multipart/*"})
public class MultipartFormDataEntityProvider implements EntityProvider<Iterator<FileItem>>
{

   /**
    * @see HttpServletRequest
    */
   @Context
   private HttpServletRequest httpRequest;

   /**
    * {@inheritDoc}
    */
   public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      if (type == Iterator.class)
      {
         try
         {
            ParameterizedType t = (ParameterizedType)genericType;
            Type[] ta = t.getActualTypeArguments();
            if (ta.length == 1 && ta[0] == FileItem.class)
            {
               return true;
            }
            return false;
         }
         catch (ClassCastException e)
         {
            return false;
         }
      }

      return false;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public Iterator<FileItem> readFrom(Class<Iterator<FileItem>> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException
   {
      try
      {
         ApplicationContext context = ApplicationContextImpl.getCurrent();
         int bufferSize =
            context.getProperties().get(RequestHandler.WS_RS_BUFFER_SIZE) == null
               ? RequestHandler.WS_RS_BUFFER_SIZE_VALUE : Integer.parseInt((String)context.getProperties().get(
                  RequestHandler.WS_RS_BUFFER_SIZE));
         
         DefaultFileItemFactory factory = new DefaultFileItemFactory(bufferSize, FileCollector.getInstance().getStore());
         FileUpload upload = new FileUpload(factory);
         return upload.parseRequest(httpRequest).iterator();
      }
      catch (FileUploadException e)
      {
         throw new IOException("Can't process multipart data item " + e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public long getSize(Iterator<FileItem> t, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType)
   {
      return -1;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      // output is not supported
      return false;
   }

   /**
    * {@inheritDoc}
    */
   public void writeTo(Iterator<FileItem> t, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException
   {
      throw new UnsupportedOperationException();
   }

}
