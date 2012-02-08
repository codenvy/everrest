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
package org.everrest.core.impl;

import org.everrest.core.ApplicationContext;
import org.everrest.core.DependencySupplier;
import org.everrest.core.ExtHttpHeaders;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.Lifecycle;
import org.everrest.core.ObjectFactory;
import org.everrest.core.RequestFilter;
import org.everrest.core.RequestHandler;
import org.everrest.core.ResourceBinder;
import org.everrest.core.ResponseFilter;
import org.everrest.core.UnhandledException;
import org.everrest.core.impl.method.MethodInvokerDecoratorFactory;
import org.everrest.core.impl.uri.UriComponent;
import org.everrest.core.util.Logger;
import org.everrest.core.util.Tracer;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class RequestHandlerImpl implements RequestHandler
{
   /** Logger. */
   private static final Logger LOG = Logger.getLogger(RequestHandlerImpl.class);

   /**
    * Application properties. Properties from this map will be copied to ApplicationContext and may be accessible via
    * method {@link ApplicationContextImpl#getProperties()}.
    */
   private static final Map<String, String> properties = new ConcurrentHashMap<String, String>();

   public static String getProperty(String name)
   {
      return properties.get(name);
   }

   public static void setProperty(String name, String value)
   {
      if (value == null)
      {
         properties.remove(name);
      }
      else
      {
         properties.put(name, value);
      }
   }

   /** See {@link RequestDispatcher}. */
   private final RequestDispatcher dispatcher;

   private final DependencySupplier dependencySupplier;

   private final ProviderBinder providers;

   private final boolean normalizeUriFeature;

   private final boolean httpMethodOverrideFeature;

   private final int maxBufferSize;

   private MethodInvokerDecoratorFactory methodInvokerDecoratorFactory;

   /**
    * @param dispatcher RequestDispatcher
    * @param providers ProviderBinder. May be <code>null</code> then default set of providers used
    * @param dependencySupplier DependencySupplier
    * @param config EverrestConfiguration. May be <code>null</code> then default configuration used
    */
   public RequestHandlerImpl(RequestDispatcher dispatcher, ProviderBinder providers,
                             DependencySupplier dependencySupplier, EverrestConfiguration config)
   {
      this.dispatcher = dispatcher;
      this.dependencySupplier = dependencySupplier;
      this.providers = providers == null ? ProviderBinder.getInstance() : providers;
      if (config == null)
      {
         config = new EverrestConfiguration();
      }
      httpMethodOverrideFeature = config.isHttpMethodOverride();
      normalizeUriFeature = config.isNormalizeUri();
      maxBufferSize = config.getMaxBufferSize();
      ServiceLoader<MethodInvokerDecoratorFactory> s = ServiceLoader.load(MethodInvokerDecoratorFactory.class);
      Iterator<MethodInvokerDecoratorFactory> iterator = s.iterator();
      if (iterator.hasNext())
      {
         methodInvokerDecoratorFactory = iterator.next();
      }
   }

   public RequestHandlerImpl(RequestDispatcher dispatcher, DependencySupplier dependencySupplier,
                             EverrestConfiguration config)
   {
      this(dispatcher, null, dependencySupplier, config);
   }

   /** @deprecated do not use it any more. It is kept for back compatibility only. Will be removed in future. */
   public RequestHandlerImpl(ResourceBinder resources, DependencySupplier dependencySupplier)
   {
      this(new RequestDispatcher(resources), null, dependencySupplier, new EverrestConfiguration());
   }

   /** @deprecated do not use it any more. It is kept for back compatibility only. Will be removed in future. */
   public RequestHandlerImpl(ResourceBinder resources, ProviderBinder providers, DependencySupplier dependencySupplier,
                             EverrestConfiguration config)
   {
      this(new RequestDispatcher(resources), providers, dependencySupplier, config);
   }

   /** {@inheritDoc} */
   @SuppressWarnings({"unchecked", "rawtypes"})
   public void handleRequest(GenericContainerRequest request, GenericContainerResponse response)
      throws UnhandledException, IOException
   {
      ApplicationContext context = null;
      try
      {
         context = new ApplicationContextImpl(request, response, providers, methodInvokerDecoratorFactory);
         context.getProperties().putAll(properties);
         context.setDependencySupplier(dependencySupplier);
         context.getAttributes().put(EverrestConfiguration.EVERREST_MAX_BUFFER_SIZE, maxBufferSize);
         ((Lifecycle)context).start();
         ApplicationContextImpl.setCurrent(context);

         if (normalizeUriFeature)
         {
            request.setUris(UriComponent.normalize(request.getRequestUri()), request.getBaseUri());
         }

         if (httpMethodOverrideFeature)
         {
            String method = request.getRequestHeaders().getFirst(ExtHttpHeaders.X_HTTP_METHOD_OVERRIDE);
            if (method != null)
            {
               if (Tracer.isTracingEnabled())
               {
                  Tracer.trace("Override HTTP method from \"X-HTTP-Method-Override\" header "
                     + request.getMethod() + " => " + method);
               }

               request.setMethod(method);
            }
         }

         try
         {
            for (ObjectFactory<FilterDescriptor> factory : context.getProviders().getRequestFilters(context.getPath()))
            {
               ((RequestFilter)factory.getInstance(context)).doFilter(request);
            }

            dispatcher.dispatch(request, response);

            if (response.getHttpHeaders().getFirst(ExtHttpHeaders.JAXRS_BODY_PROVIDED) == null)
            {
               String jaxrsHeader = getJaxrsHeader(response.getStatus());
               if (jaxrsHeader != null)
               {
                  response.getHttpHeaders().putSingle(ExtHttpHeaders.JAXRS_BODY_PROVIDED, jaxrsHeader);
               }
            }

            for (ObjectFactory<FilterDescriptor> factory : context.getProviders().getResponseFilters(context.getPath()))
            {
               ((ResponseFilter)factory.getInstance(context)).doFilter(response);
            }
         }
         catch (Exception e)
         {
            if (e instanceof WebApplicationException)
            {
               Response errorResponse = ((WebApplicationException)e).getResponse();

               int errorStatus = errorResponse.getStatus();
               Throwable cause = e.getCause();
               // should be some of 4xx status
               if (errorStatus < 500)
               {
                  // Warn about error in debug mode only.
                  if (LOG.isDebugEnabled() && cause != null)
                  {
                     LOG.debug("WebApplicationException occurs.", cause);
                  }
               }
               else
               {
                  if (cause != null)
                  {
                     LOG.error("WebApplicationException occurs.", cause);
                  }
               }

               if (Tracer.isTracingEnabled())
               {
                  Tracer.trace("WebApplicationException occurs, cause = (" + cause + ")");
               }

               ExceptionMapper exceptionMapper = context.getProviders().getExceptionMapper(WebApplicationException.class);
               if (errorResponse.getEntity() == null)
               {
                  if (exceptionMapper != null)
                  {
                     if (Tracer.isTracingEnabled())
                     {
                        Tracer.trace("Found ExceptionMapper for WebApplicationException = (" + exceptionMapper + ")");
                     }

                     errorResponse = exceptionMapper.toResponse(e);
                  }
                  else if (e.getMessage() != null)
                  {
                     errorResponse = createErrorResponse(errorStatus, e.getMessage());
                  }
               }
               else
               {
                  if (errorResponse.getMetadata().getFirst(ExtHttpHeaders.JAXRS_BODY_PROVIDED) == null)
                  {
                     String jaxrsHeader = getJaxrsHeader(errorStatus);
                     if (jaxrsHeader != null)
                     {
                        errorResponse.getMetadata().putSingle(ExtHttpHeaders.JAXRS_BODY_PROVIDED, jaxrsHeader);
                     }
                  }
               }

               response.setResponse(errorResponse);
            }
            else if (e instanceof InternalException)
            {
               Throwable cause = e.getCause();

               if (Tracer.isTracingEnabled())
               {
                  Tracer.trace("InternalException occurs, cause = (" + cause + ")");
               }

               Class causeClazz = cause.getClass();
               ExceptionMapper exceptionMapper = context.getProviders().getExceptionMapper(causeClazz);
               while (causeClazz != null && exceptionMapper == null)
               {
                  exceptionMapper = context.getProviders().getExceptionMapper(causeClazz);
                  if (exceptionMapper == null)
                  {
                     causeClazz = causeClazz.getSuperclass();
                  }
               }

               if (exceptionMapper != null)
               {
                  // Hide error message if exception mapper exists.
                  if (LOG.isDebugEnabled())
                  {
                     LOG.debug("InternalException occurs.", cause);
                  }

                  if (Tracer.isTracingEnabled())
                  {
                     Tracer.trace("Found ExceptionMapper for " + cause.getClass() + " = (" + exceptionMapper + ")");
                  }

                  response.setResponse(exceptionMapper.toResponse(cause));
               }
               else
               {
                  LOG.error("InternalException occurs.", cause);
                  throw new UnhandledException(e.getCause());
               }
            }
            else
            {
               throw new UnhandledException(e);
            }
         }

         response.writeResponse();
      }
      finally
      {
         try
         {
            if (context != null)
            {
               ((Lifecycle)context).stop();
            }
         }
         finally
         {
            ApplicationContextImpl.setCurrent(null);
         }
      }
   }

   /**
    * Create error response with specified status and body message.
    *
    * @param status response status
    * @param message response message
    * @return response
    */
   private Response createErrorResponse(int status, String message)
   {

      ResponseBuilder responseBuilder = Response.status(status);
      responseBuilder.entity(message).type(MediaType.TEXT_PLAIN);
      String jaxrsHeader = getJaxrsHeader(status);
      if (jaxrsHeader != null)
      {
         responseBuilder.header(ExtHttpHeaders.JAXRS_BODY_PROVIDED, jaxrsHeader);
      }
      return responseBuilder.build();
   }

   private String getJaxrsHeader(int status)
   {
      if (status >= 400)
      {
         return "Error-Message";
      }
      // Add required behavior here.
      return null;
   }

}
