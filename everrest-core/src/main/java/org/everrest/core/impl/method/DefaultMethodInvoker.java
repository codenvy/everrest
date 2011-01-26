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
package org.everrest.core.impl.method;

import org.everrest.core.ApplicationContext;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.ObjectFactory;
import org.everrest.core.impl.InternalException;
import org.everrest.core.method.MethodInvoker;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.method.MethodParameter;
import org.everrest.core.resource.GenericMethodResource;
import org.everrest.core.util.Logger;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;

/**
 * Invoker for Resource Method, Sub-Resource Method and SubResource Locator.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class DefaultMethodInvoker implements MethodInvoker
{

   /** Logger. */
   private static final Logger LOG = Logger.getLogger(DefaultMethodInvoker.class);

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public final Object invokeMethod(Object resource, GenericMethodResource methodResource, ApplicationContext context)
   {
      beforeInvokeMethod(methodResource, methodResource, context);

      Object[] p = new Object[methodResource.getMethodParameters().size()];
      int i = 0;
      for (MethodParameter mp : methodResource.getMethodParameters())
      {
         Annotation a = mp.getAnnotation();
         if (a != null)
         {
            ParameterResolver<?> pr = ParameterResolverFactory.createParameterResolver(a);
            try
            {
               p[i++] = pr.resolve(mp, context);
            }
            catch (Exception e)
            {
               String msg = "Not able resolve method parameter " + mp;
               Class<?> ac = a.annotationType();
               if (ac == MatrixParam.class || ac == QueryParam.class || ac == PathParam.class)
               {
                  throw new WebApplicationException(e, Response.status(Response.Status.NOT_FOUND).entity(msg).type(
                     MediaType.TEXT_PLAIN).build());
               }
               throw new WebApplicationException(e, Response.status(Response.Status.BAD_REQUEST).entity(msg).type(
                  MediaType.TEXT_PLAIN).build());
            }
         }
         else
         {
            InputStream entityStream = context.getContainerRequest().getEntityStream();
            if (entityStream == null)
            {
               p[i++] = null;
            }
            else
            {
               MediaType contentType = context.getContainerRequest().getMediaType();

               MessageBodyReader entityReader =
                  context.getProviders().getMessageBodyReader(mp.getParameterClass(), mp.getGenericType(),
                     mp.getAnnotations(), contentType);
               if (entityReader == null)
               {
                  List<String> contentLength =
                     context.getContainerRequest().getRequestHeader(HttpHeaders.CONTENT_LENGTH);
                  int length = 0;
                  if (contentLength != null && contentLength.size() > 0)
                  {
                     try
                     {
                        length = Integer.parseInt(contentLength.get(0));
                     }
                     catch (NumberFormatException e)
                     {
                     }
                  }
                  if (contentType == null && length == 0)
                  {
                     // If both Content-Length and Content-Type is not set
                     // consider there is no content. In this case we not able
                     // to determine reader required for content but
                     // 'Unsupported Media Type' (415) status looks strange if
                     // there is no content at all.
                     p[i++] = null;
                  }
                  else
                  {
                     String msg =
                        "Media type " + contentType + " is not supported. There is no corresponded entity reader.";
                     if (LOG.isDebugEnabled())
                     {
                        LOG.warn(msg);
                     }
                     throw new WebApplicationException(Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).entity(
                        msg).type(MediaType.TEXT_PLAIN).build());
                  }
               }
               else
               {
                  try
                  {
                     MultivaluedMap<String, String> headers = context.getContainerRequest().getRequestHeaders();
                     p[i++] =
                        entityReader.readFrom(mp.getParameterClass(), mp.getGenericType(), mp.getAnnotations(),
                           contentType, headers, entityStream);
                  }
                  catch (Exception e)
                  {
                     if (LOG.isDebugEnabled())
                     {
                        e.printStackTrace();
                     }
                     if (e instanceof WebApplicationException)
                     {
                        throw (WebApplicationException)e;
                     }
                     throw new InternalException(e);
                  }
               }
            }
         }
      }
      return invokeMethod(resource, methodResource, p);
   }

   protected void beforeInvokeMethod(Object resource, GenericMethodResource methodResource, ApplicationContext context)
   {
      for (ObjectFactory<FilterDescriptor> factory : context.getProviders().getMethodInvokerFilters(context.getPath()))
      {
         MethodInvokerFilter f = (MethodInvokerFilter)factory.getInstance(context);
         f.accept(methodResource);
      }
   }

   protected Object invokeMethod(Object resource, GenericMethodResource methodResource, Object[] p)
   {
      try
      {
         return methodResource.getMethod().invoke(resource, p);
      }
      catch (IllegalArgumentException argExc)
      {
         // should not be thrown
         throw new InternalException(argExc);
      }
      catch (IllegalAccessException accessExc)
      {
         // should not be thrown
         throw new InternalException(accessExc);
      }
      catch (InvocationTargetException invExc)
      {
         if (LOG.isDebugEnabled())
         {
            invExc.printStackTrace();
         }
         // get cause of exception that method produces
         Throwable cause = invExc.getCause();
         // if WebApplicationException than it may contain response
         if (WebApplicationException.class == cause.getClass())
         {
            throw (WebApplicationException)cause;
         }

         throw new InternalException(cause);
      }
   }
}
