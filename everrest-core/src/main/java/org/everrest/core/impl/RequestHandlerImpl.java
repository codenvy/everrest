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
import org.everrest.core.ObjectFactory;
import org.everrest.core.RequestFilter;
import org.everrest.core.RequestHandler;
import org.everrest.core.ResourceBinder;
import org.everrest.core.ResponseFilter;
import org.everrest.core.UnhandledException;
import org.everrest.core.impl.method.filter.SecurityConstraint;
import org.everrest.core.impl.uri.UriComponent;
import org.everrest.core.util.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class RequestHandlerImpl implements RequestHandler
{

   /** Logger. */
   private static final Logger LOG = Logger.getLogger(RequestHandlerImpl.class);

   /**
    * Application properties. Properties from this map will be copied to
    * ApplicationContext and may be accessible via method
    * {@link ApplicationContextImpl#getProperties()}.
    */
   private static final Map<String, String> properties = new HashMap<String, String>();

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

   /** ResourceBinder. */
   private final ResourceBinder resources;

   /** ProviderBinder. */
   private final ProviderBinder applicationProviders;

   private final DependencySupplier depInjector;

   private final EverrestConfiguration config;

   public RequestHandlerImpl(ResourceBinder resources, ProviderBinder providers, RequestDispatcher dispatcher,
      DependencySupplier depInjector, EverrestConfiguration config)
   {
      this.resources = resources;
      this.applicationProviders = providers;
      this.config = config == null ? new EverrestConfiguration() : config;
      this.dispatcher = dispatcher;
      this.depInjector = depInjector;
      // Add security check only in customized ProviderBinder.
      if (this.applicationProviders != null && this.config.isCheckSecurity())
      {
         this.applicationProviders.addMethodInvokerFilter(new SecurityConstraint());
      }
   }

   public RequestHandlerImpl(ResourceBinder resources, ProviderBinder providers, DependencySupplier depInjector,
      EverrestConfiguration config)
   {
      this(resources, providers, new RequestDispatcher(resources), depInjector, config);
   }

   public RequestHandlerImpl(ResourceBinder resources, DependencySupplier depInjector)
   {
      this(resources, null, new RequestDispatcher(resources), depInjector, new EverrestConfiguration());
   }

   /**
    * {@inheritDoc}
    */
   public ResourceBinder getResourceBinder()
   {
      return resources;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public void handleRequest(GenericContainerRequest request, GenericContainerResponse response)
      throws UnhandledException, IOException
   {
      try
      {
         if (config.isNormalizeUri())
         {
            request.setUris(UriComponent.normalize(request.getRequestUri()), request.getBaseUri());
         }
         if (config.isHttpMethodOverride())
         {
            String method = request.getRequestHeaders().getFirst(ExtHttpHeaders.X_HTTP_METHOD_OVERRIDE);
            if (method != null)
            {
               request.setMethod(method);
            }
         }

         ProviderBinder providers = applicationProviders;
         if (providers == null)
         {
            providers = ProviderBinder.getInstance();
         }

         ApplicationContext context = new ApplicationContextImpl(request, response, providers);
         context.getProperties().putAll(properties);
         context.setDependencySupplier(depInjector);
         ApplicationContextImpl.setCurrent(context);

         for (ObjectFactory<FilterDescriptor> factory : providers.getRequestFilters(context.getPath()))
         {
            RequestFilter f = (RequestFilter)factory.getInstance(context);
            f.doFilter(request);
         }

         try
         {
            dispatcher.dispatch(request, response);
            if (response.getHttpHeaders().getFirst(ExtHttpHeaders.JAXRS_BODY_PROVIDED) == null)
            {
               String jaxrsHeader = getJaxrsHeader(response.getStatus());
               if (jaxrsHeader != null)
               {
                  response.getHttpHeaders().putSingle(ExtHttpHeaders.JAXRS_BODY_PROVIDED, jaxrsHeader);
               }
            }
         }
         catch (Exception e)
         {
            if (e instanceof WebApplicationException)
            {
               Response errorResponse = ((WebApplicationException)e).getResponse();
               ExceptionMapper excmap = providers.getExceptionMapper(WebApplicationException.class);

               int errorStatus = errorResponse.getStatus();
               // should be some of 4xx status
               if (errorStatus < 500)
               {
                  // Warn about error in debug mode only.
                  if (LOG.isDebugEnabled() && e.getCause() != null)
                  {
                     LOG.warn("WebApplication exception occurs.", e.getCause());
                  }
               }
               else
               {
                  if (e.getCause() != null)
                  {
                     LOG.warn("WebApplication exception occurs.", e.getCause());
                  }
               }
               // -----
               if (errorResponse.getEntity() == null)
               {
                  if (excmap != null)
                  {
                     errorResponse = excmap.toResponse(e);
                  }
                  else
                  {
                     if (e.getMessage() != null)
                     {
                        errorResponse = createErrorResponse(errorStatus, e.getMessage());
                     }
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
               Class causeClazz = cause.getClass();
               ExceptionMapper excmap = providers.getExceptionMapper(causeClazz);
               while (causeClazz != null && excmap == null)
               {
                  excmap = providers.getExceptionMapper(causeClazz);
                  if (excmap == null)
                  {
                     causeClazz = causeClazz.getSuperclass();
                  }
               }
               if (excmap != null)
               {
                  if (LOG.isDebugEnabled())
                  {
                     // Hide error message if exception mapper exists.
                     LOG.warn("Internal error occurs.", cause);
                  }
                  response.setResponse(excmap.toResponse(e.getCause()));
               }
               else
               {
                  LOG.error("Internal error occurs.", cause);
                  throw new UnhandledException(e.getCause());
               }
            }
            else
            {
               throw new UnhandledException(e);
            }
         }
         for (ObjectFactory<FilterDescriptor> factory : providers.getResponseFilters(context.getPath()))
         {
            ResponseFilter f = (ResponseFilter)factory.getInstance(context);
            f.doFilter(response);
         }
         response.writeResponse();
      }
      finally
      {
         // reset application context
         ApplicationContextImpl.setCurrent(null);
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
