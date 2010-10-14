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

import org.everrest.core.FilterDescriptor;
import org.everrest.core.ObjectFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class ApplicationProviderBinder extends ProviderBinder
{

   public ApplicationProviderBinder()
   {
      super();
   }

   /**
    * {@inheritDoc}
    */
   protected void init()
   {
      // Do not add default providers.
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List<MediaType> getAcceptableWriterMediaTypes(Class<?> type, Type genericType, Annotation[] annotations)
   {
      List<MediaType> l = doGetAcceptableWriterMediaTypes(type, genericType, annotations);
      l.addAll(getParent().getAcceptableWriterMediaTypes(type, genericType, annotations));
      return l;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> ContextResolver<T> getContextResolver(Class<T> contextType, MediaType mediaType)
   {
      ContextResolver<T> resolver = doGetContextResolver(contextType, mediaType);
      if (resolver == null)
      {
         resolver = getParent().getContextResolver(contextType, mediaType);
      }
      return resolver;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> type)
   {
      ExceptionMapper<T> excMapper = doGetExceptionMapper(type);
      if (excMapper == null)
      {
         excMapper = getParent().getExceptionMapper(type);
      }
      return excMapper;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type, Type genericType, Annotation[] annotations,
      MediaType mediaType)
   {
      MessageBodyReader<T> reader = doGetMessageBodyReader(type, genericType, annotations, mediaType);
      if (reader == null)
      {
         reader = getParent().getMessageBodyReader(type, genericType, annotations, mediaType);
      }
      return reader;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type, Type genericType, Annotation[] annotations,
      MediaType mediaType)
   {
      MessageBodyWriter<T> writer = doGetMessageBodyWriter(type, genericType, annotations, mediaType);
      if (writer == null)
      {
         writer = getParent().getMessageBodyWriter(type, genericType, annotations, mediaType);
      }
      return writer;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List<ObjectFactory<FilterDescriptor>> getMethodInvokerFilters(String path)
   {
      List<ObjectFactory<FilterDescriptor>> l = doGetMatchedFilters(path, invokerFilters);
      l.addAll(getParent().getMethodInvokerFilters(path));
      return l;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List<ObjectFactory<FilterDescriptor>> getRequestFilters(String path)
   {
      List<ObjectFactory<FilterDescriptor>> l = doGetMatchedFilters(path, requestFilters);
      l.addAll(getParent().getRequestFilters(path));
      return l;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List<ObjectFactory<FilterDescriptor>> getResponseFilters(String path)
   {
      List<ObjectFactory<FilterDescriptor>> l = doGetMatchedFilters(path, responseFilters);
      l.addAll(getParent().getResponseFilters(path));
      return l;
   }

   private ProviderBinder getParent()
   {
      return ProviderBinder.getInstance();
   }

}
