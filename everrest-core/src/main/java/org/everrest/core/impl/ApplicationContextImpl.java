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
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.InitialProperties;
import org.everrest.core.Lifecycle;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousMethodInvoker;
import org.everrest.core.impl.method.DefaultMethodInvoker;
import org.everrest.core.impl.method.MethodInvokerDecoratorFactory;
import org.everrest.core.impl.method.OptionsRequestMethodInvoker;
import org.everrest.core.impl.uri.UriComponent;
import org.everrest.core.method.MethodInvoker;
import org.everrest.core.resource.GenericMethodResource;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.servlet.ServletContainerRequest;
import org.everrest.core.tools.SimplePrincipal;
import org.everrest.core.tools.SimpleSecurityContext;
import org.everrest.core.tools.WebApplicationDeclaredRoles;
import org.everrest.core.util.Logger;

import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class ApplicationContextImpl implements ApplicationContext, Lifecycle
{
   private static final Logger LOG = Logger.getLogger(ApplicationContextImpl.class);

   /** {@link ThreadLocal} ApplicationContext. */
   private static ThreadLocal<ApplicationContext> current = new ThreadLocal<ApplicationContext>();

   /** @return current ApplicationContext. */
   public static ApplicationContext getCurrent()
   {
      return current.get();
   }

   /**
    * Set ApplicationContext for current thread.
    *
    * @param context
    *    the ApplicationContext.
    */
   public static void setCurrent(ApplicationContext context)
   {
      current.set(context);
   }

   /** See {@link GenericContainerRequest}. */
   protected GenericContainerRequest request;

   /** See {@link ContainerResponse}. */
   protected GenericContainerResponse response;

   /** Providers. */
   protected ProviderBinder providers;

   protected DependencySupplier depInjector;

   /** Values of template parameters. */
   private List<String> parameterValues = new ArrayList<String>();

   /** List of matched resources. */
   private List<Object> matchedResources = new ArrayList<Object>();

   /** List of not decoded matched URIs. */
   private List<String> encodedMatchedURIs = new ArrayList<String>();

   /** List of decoded matched URIs. */
   private List<String> matchedURIs = new ArrayList<String>();

   /** Mutable runtime attributes. */
   private Map<String, Object> attributes;

   /** Properties. */
   private Map<String, String> properties;

   /** Absolute path, full requested URI without query string and fragment. */
   private URI absolutePath;

   /** Decoded relative path. */
   private String path;

   /** Not decoded relative path. */
   private String encodedPath;

   /** Not decoded path template parameters. */
   private MultivaluedMap<String, String> encodedPathParameters;

   /** Decoded path template parameters. */
   private MultivaluedMap<String, String> pathParameters;

   /** List of not decoded path segments. */
   private List<PathSegment> encodedPathSegments;

   /** Decoded path segments. */
   private List<PathSegment> pathSegments;

   /** Not decoded query parameters. */
   private MultivaluedMap<String, String> encodedQueryParameters;

   /** Decoded query parameters. */
   private MultivaluedMap<String, String> queryParameters;

   private final MethodInvokerDecoratorFactory methodInvokerDecoratorFactory;

   private SecurityContext asynchronousSecurityContext;

   /**
    * Constructs new instance of ApplicationContext.
    *
    * @param request
    *    See {@link GenericContainerRequest}
    * @param response
    *    See {@link GenericContainerResponse}
    * @param providers
    *    See {@link ProviderBinder}
    */
   public ApplicationContextImpl(GenericContainerRequest request, GenericContainerResponse response,
                                 ProviderBinder providers)
   {
      this(request, response, providers, null);
   }

   /**
    * Constructs new instance of ApplicationContext.
    *
    * @param request
    *    See {@link GenericContainerRequest}
    * @param response
    *    See {@link GenericContainerResponse}
    * @param providers
    *    See {@link ProviderBinder}
    * @param methodInvokerDecoratorFactory
    *    See {@link MethodInvokerDecoratorFactory}
    */
   public ApplicationContextImpl(GenericContainerRequest request, GenericContainerResponse response,
                                 ProviderBinder providers, MethodInvokerDecoratorFactory methodInvokerDecoratorFactory)
   {
      this.request = request;
      this.response = response;
      this.providers = providers;
      this.methodInvokerDecoratorFactory = methodInvokerDecoratorFactory;
   }

   /** {@inheritDoc} */
   public void addMatchedResource(Object resource)
   {
      matchedResources.add(0, resource);
   }

   /** {@inheritDoc} */
   @Override
   public void addMatchedURI(String uri)
   {
      encodedMatchedURIs.add(0, uri);
      matchedURIs.add(0, UriComponent.decode(uri, UriComponent.PATH_SEGMENT));
   }

   /** {@inheritDoc} */
   @Override
   public URI getAbsolutePath()
   {
      if (absolutePath != null)
      {
         return absolutePath;
      }

      return absolutePath = getRequestUriBuilder().replaceQuery(null).fragment(null).build();
   }

   /** {@inheritDoc} */
   @Override
   public UriBuilder getAbsolutePathBuilder()
   {
      return UriBuilder.fromUri(getAbsolutePath());
   }

   /** {@inheritDoc} */
   @Override
   public Map<String, Object> getAttributes()
   {
      return attributes == null ? attributes = new HashMap<String, Object>() : attributes;
   }

   /** {@inheritDoc} */
   @Override
   public URI getBaseUri()
   {
      return request.getBaseUri();
   }

   /** {@inheritDoc} */
   @Override
   public UriBuilder getBaseUriBuilder()
   {
      return UriBuilder.fromUri(getBaseUri());
   }

   /** {@inheritDoc} */
   @Override
   public GenericContainerRequest getContainerRequest()
   {
      return request;
   }

   /** {@inheritDoc} */
   @Override
   public GenericContainerResponse getContainerResponse()
   {
      return response;
   }

   /** {@inheritDoc} */
   @Override
   public DependencySupplier getDependencySupplier()
   {
      return depInjector;
   }

   /** {@inheritDoc} */
   @Override
   public HttpHeaders getHttpHeaders()
   {
      return request;
   }

   /** {@inheritDoc} */
   @Override
   public InitialProperties getInitialProperties()
   {
      return this;
   }

   /** {@inheritDoc} */
   @Override
   public List<Object> getMatchedResources()
   {
      return matchedResources;
   }

   /** {@inheritDoc} */
   @Override
   public List<String> getMatchedURIs()
   {
      return getMatchedURIs(true);
   }

   /** {@inheritDoc} */
   @Override
   public List<String> getMatchedURIs(boolean decode)
   {
      return decode ? matchedURIs : encodedMatchedURIs;
   }

   /** {@inheritDoc} */
   @Override
   public MethodInvoker getMethodInvoker(GenericMethodResource methodDescriptor)
   {
      String method = request.getMethod();
      if ("OPTIONS".equals(method) && methodDescriptor.getMethod() == null)
      {
         // GenericMethodResource.getMethod() always return null if method for
         // "OPTIONS" request was not described in source code of service. In
         // this case we provide mechanism for "fake" method invoking.
         return new OptionsRequestMethodInvoker();
      }
      MethodInvoker invoker = null;
      // Never use AsynchronousMethodInvoker for process SubResourceLocatorDescriptor.
      // Locators can't be processed in asynchronous mode since it is not end point of request.
      if (isAsynchronous() && methodDescriptor instanceof ResourceMethodDescriptor)
      {
         ContextResolver<AsynchronousJobPool> asyncJobsResolver =
            getProviders().getContextResolver(AsynchronousJobPool.class, null);
         if (asyncJobsResolver == null)
         {
            throw new IllegalStateException("Asynchronous jobs feature is not configured properly. ");
         }
         invoker = new AsynchronousMethodInvoker(asyncJobsResolver.getContext(null));
      }
      if (invoker == null)
      {
         invoker = new DefaultMethodInvoker();
      }
      if (methodInvokerDecoratorFactory != null)
      {
         return methodInvokerDecoratorFactory.makeDecorator(invoker);
      }
      return invoker;
   }

   /** {@inheritDoc} */
   @Override
   public List<String> getParameterValues()
   {
      return parameterValues;
   }

   /** {@inheritDoc} */
   @Override
   public String getPath()
   {
      return getPath(true);
   }

   /** {@inheritDoc} */
   @Override
   public String getPath(boolean decode)
   {
      if (encodedPath == null)
      {
         encodedPath = getAbsolutePath().getRawPath().substring(getBaseUri().getRawPath().length());
      }

      if (decode)
      {
         if (path != null)
         {
            return path;
         }

         return path = UriComponent.decode(encodedPath, UriComponent.PATH);

      }

      return encodedPath;
   }

   /** {@inheritDoc} */
   @Override
   public MultivaluedMap<String, String> getPathParameters()
   {
      return getPathParameters(true);
   }

   /** {@inheritDoc} */
   @Override
   public MultivaluedMap<String, String> getPathParameters(boolean decode)
   {
      if (encodedPathParameters == null)
      {
         throw new IllegalStateException("Path template variables not initialized yet.");
      }

      if (decode)
      {
         if (pathParameters == null)
         {
            pathParameters = new MultivaluedMapImpl();
         }

         if (pathParameters.size() != encodedPathParameters.size())
         {
            for (String key : encodedPathParameters.keySet())
            {
               if (!pathParameters.containsKey(key))
               {
                  pathParameters.putSingle(UriComponent.decode(key, UriComponent.PATH_SEGMENT),
                     UriComponent.decode(encodedPathParameters.getFirst(key), UriComponent.PATH));
               }
            }
         }
         return pathParameters;
      }

      return encodedPathParameters;
   }

   /** {@inheritDoc} */
   @Override
   public List<PathSegment> getPathSegments()
   {
      return getPathSegments(true);
   }

   /** {@inheritDoc} */
   @Override
   public List<PathSegment> getPathSegments(boolean decode)
   {
      if (decode)
      {
         return pathSegments != null ? pathSegments : (pathSegments = UriComponent.parsePathSegments(getPath(), true));
      }
      return encodedPathSegments != null ? encodedPathSegments : (encodedPathSegments =
         UriComponent.parsePathSegments(getPath(), false));
   }

   /** {@inheritDoc} */
   @Override
   public Map<String, String> getProperties()
   {
      return properties == null ? properties = new HashMap<String, String>() : properties;
   }

   /** {@inheritDoc} */
   @Override
   public String getProperty(String name)
   {
      return getProperties().get(name);
   }

   /** {@inheritDoc} */
   @Override
   public ProviderBinder getProviders()
   {
      return providers;
   }

   /**
    * @param providers
    *    ProviderBinder
    */
   @Override
   public void setProviders(ProviderBinder providers)
   {
      this.providers = providers;
   }

   /** {@inheritDoc} */
   @Override
   public MultivaluedMap<String, String> getQueryParameters()
   {
      return getQueryParameters(true);
   }

   /** {@inheritDoc} */
   @Override
   public MultivaluedMap<String, String> getQueryParameters(boolean decode)
   {
      if (decode)
      {
         return queryParameters != null ? queryParameters : (queryParameters =
            UriComponent.parseQueryString(getRequestUri().getRawQuery(), true));
      }
      return encodedQueryParameters != null ? encodedQueryParameters : (encodedQueryParameters =
         UriComponent.parseQueryString(getRequestUri().getRawQuery(), false));
   }

   /** {@inheritDoc} */
   @Override
   public Request getRequest()
   {
      return request;
   }

   /** {@inheritDoc} */
   @Override
   public URI getRequestUri()
   {
      return request.getRequestUri();
   }

   /** {@inheritDoc} */
   @Override
   public UriBuilder getRequestUriBuilder()
   {
      return UriBuilder.fromUri(getRequestUri());
   }

   /** {@inheritDoc} */
   @Override
   public SecurityContext getSecurityContext()
   {
      // We get security information from HttpServletRequest but we may be not able to do this is asynchronous mode.
      // In asynchronous mode resource method processed when HTTP request ended already and we cannot use it any more.
      // Do some workaround to keep security info even after request ends.
      if (isAsynchronous() && (request instanceof ServletContainerRequest))
      {
         if (asynchronousSecurityContext == null)
         {
            Principal requestPrincipal = request.getUserPrincipal();
            if (requestPrincipal == null)
            {
               asynchronousSecurityContext = new SimpleSecurityContext(request.isSecure());
            }
            else
            {
               // Info about roles declared for web application. We assume this is all roles that we can meet.
               WebApplicationDeclaredRoles declaredRoles =
                  (WebApplicationDeclaredRoles)EnvironmentContext.getCurrent().get(WebApplicationDeclaredRoles.class);
               if (declaredRoles == null)
               {
                  // Cannot provide any info about roles in this case.
                  asynchronousSecurityContext = new SimpleSecurityContext(new SimplePrincipal(requestPrincipal.getName()),
                     null, request.getAuthenticationScheme(), request.isSecure());
               }
               else
               {
                  Set<String> userRoles = new LinkedHashSet<String>();
                  for (String declaredRole : declaredRoles.getDeclaredRoles())
                  {
                     if (request.isUserInRole(declaredRole))
                     {
                        userRoles.add(declaredRole);
                     }
                  }
                  asynchronousSecurityContext = new SimpleSecurityContext(new SimplePrincipal(requestPrincipal.getName()),
                     userRoles, request.getAuthenticationScheme(), request.isSecure());
               }
            }
         }
         return asynchronousSecurityContext;
      }
      return request;
   }

   /** {@inheritDoc} */
   @Override
   public UriInfo getUriInfo()
   {
      return this;
   }

   /** {@inheritDoc} */
   @Override
   public void setDependencySupplier(DependencySupplier depInjector)
   {
      this.depInjector = depInjector;
   }

   /** {@inheritDoc} */
   @Override
   public void setParameterNames(List<String> parameterNames)
   {
      if (encodedPathParameters == null)
      {
         encodedPathParameters = new MultivaluedMapImpl();
      }

      for (int i = 0; i < parameterNames.size(); i++)
      {
         encodedPathParameters.add(parameterNames.get(i), parameterValues.get(i));
      }

   }

   /** {@inheritDoc} */
   @Override
   public void setProperty(String name, String value)
   {
      getProperties().put(name, value);
   }

   /** {@inheritDoc} */
   @Override
   public boolean isAsynchronous()
   {
      return Boolean.parseBoolean(getQueryParameters().getFirst("async"))
         || Boolean.parseBoolean(request.getRequestHeaders().getFirst("x-everrest-async"));
   }

   /** @see org.everrest.core.Lifecycle#start() */
   @Override
   public final void start()
   {
      // Nothing to do for start.
   }

   /** @see org.everrest.core.Lifecycle#stop() */
   @Override
   public final void stop()
   {
      @SuppressWarnings("unchecked")
      List<LifecycleComponent> perRequest =
         (List<LifecycleComponent>)getAttributes().get("org.everrest.lifecycle.PerRequest");
      if (perRequest != null && perRequest.size() > 0)
      {
         for (LifecycleComponent c : perRequest)
         {
            try
            {
               c.destroy();
            }
            catch (InternalException e)
            {
               LOG.error("Unable to destroy component. ", e);
            }
         }
         perRequest.clear();
      }
   }
}
