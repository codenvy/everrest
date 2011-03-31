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
import org.everrest.core.ComponentLifecycleScope;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.ObjectFactory;
import org.everrest.core.ResourceBinder;
import org.everrest.core.SingletonObjectFactory;
import org.everrest.core.impl.header.HeaderHelper;
import org.everrest.core.impl.header.MediaTypeHelper;
import org.everrest.core.impl.resource.AbstractResourceDescriptorImpl;
import org.everrest.core.method.MethodInvoker;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.resource.ResourceMethodMap;
import org.everrest.core.resource.SubResourceLocatorDescriptor;
import org.everrest.core.resource.SubResourceLocatorMap;
import org.everrest.core.resource.SubResourceMethodDescriptor;
import org.everrest.core.resource.SubResourceMethodMap;
import org.everrest.core.uri.UriPattern;
import org.everrest.core.util.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Lookup resource which can serve request.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class RequestDispatcher
{

   /** Logger. */
   private static final Logger LOG = Logger.getLogger(RequestDispatcher.class);

   /** See {@link ResourceBinder}. */
   protected final ResourceBinder resourceBinder;

   /**
    * Constructs new instance of RequestDispatcher.
    *
    * @param resourceBinder See {@link ResourceBinder}
    */
   public RequestDispatcher(ResourceBinder resourceBinder)
   {
      this.resourceBinder = resourceBinder;
   }

   /**
    * Dispatch {@link ContainerRequest} to resource which can serve request.
    *
    * @param request See {@link GenericContainerRequest}
    * @param response See {@link GenericContainerResponse}
    */
   public void dispatch(GenericContainerRequest request, GenericContainerResponse response)
   {
      ApplicationContext context = ApplicationContextImpl.getCurrent();
      String requestPath = context.getPath(false);
      List<String> parameterValues = context.getParameterValues();

      ObjectFactory<AbstractResourceDescriptor> resourceFactory = getRootResourse(parameterValues, requestPath);

      // Take the tail of the request path, the tail will be requested path
      // for lower resources, e. g. ResourceClass -> Sub-resource method/locator
      String newRequestPath = getPathTail(parameterValues);

      // save the resource class URI in hierarchy
      context.addMatchedURI(requestPath.substring(0, requestPath.lastIndexOf(newRequestPath)));

      context.setParameterNames(resourceFactory.getObjectModel().getUriPattern().getParameterNames());

      // may thrown WebApplicationException
      Object resource = resourceFactory.getInstance(context);

      dispatch(request, response, context, resourceFactory, resource, newRequestPath);
   }

   /**
    * Get last element from path parameters. This element will be used as
    * request path for child resources.
    *
    * @param parameterValues See
    *        {@link ApplicationContextImpl#getParameterValues()}
    * @return last element from given list or empty string if last element is
    *         null
    */
   private static String getPathTail(List<String> parameterValues)
   {
      int i = parameterValues.size() - 1;
      return parameterValues.get(i) != null ? parameterValues.get(i) : "";
   }

   /**
    * Process resource methods, sub-resource methods and sub-resource locators
    * to find the best one for serve request.
    *
    * @param request See {@link GenericContainerRequest}
    * @param response See {@link GenericContainerResponse}
    * @param context See {@link ApplicationContextImpl}
    * @param resourceFactory the root resource factory or resource factory which
    *        was created by previous sub-resource locator
    * @param resource instance of resource class
    * @param requestPath request path, it is relative path to the base URI or
    *        other resource which was called before (one of sub-resource
    *        locators)
    */
   private void dispatch(GenericContainerRequest request, GenericContainerResponse response,
      ApplicationContext context, ObjectFactory<AbstractResourceDescriptor> resourceFactory, Object resource,
      String requestPath)
   {

      List<String> parameterValues = context.getParameterValues();
      int len = parameterValues.size();

      // resource method or sub-resource method or sub-resource locator

      ResourceMethodMap<ResourceMethodDescriptor> rmm = resourceFactory.getObjectModel().getResourceMethods();
      SubResourceMethodMap srmm = resourceFactory.getObjectModel().getSubResourceMethods();
      SubResourceLocatorMap srlm = resourceFactory.getObjectModel().getSubResourceLocators();
      if ((parameterValues.get(len - 1) == null || "/".equals(parameterValues.get(len - 1))) && rmm.size() > 0)
      {
         // resource method, then process HTTP method and consume/produce media
         // types

         List<ResourceMethodDescriptor> methods = new ArrayList<ResourceMethodDescriptor>();
         boolean match = processResourceMethod(rmm, request, response, methods);
         if (!match)
         {
            if (LOG.isDebugEnabled())
               LOG.debug("Not found resource method for method " + request.getMethod());

            return; // Error Response is preset
         }

         invokeResourceMethod(methods.get(0), resource, context, request, response);

      }
      else
      { // sub-resource method/locator
         List<SubResourceMethodDescriptor> methods = new ArrayList<SubResourceMethodDescriptor>();
         // check sub-resource methods
         boolean match = processSubResourceMethod(srmm, requestPath, request, response, parameterValues, methods);
         // check sub-resource locators
         List<SubResourceLocatorDescriptor> locators = new ArrayList<SubResourceLocatorDescriptor>();
         boolean hasAcceptableLocator = processSubResourceLocator(srlm, requestPath, parameterValues, locators);

         // Sub-resource method or sub-resource locator should be found,
         // otherwise error response with corresponding status.
         // If sub-resource locator not found status must be Not Found (404).
         // If sub-resource method not found then can be few statuses to
         // return, in this case don't care about locators, just return status
         // for sub-resource method. If no one method found then status will
         // Not Found (404) anyway.
         if (!match && !hasAcceptableLocator)
         {
            if (LOG.isDebugEnabled())
               LOG.debug("Not found sub-resource methods nor sub-resource locators for path " + requestPath
                  + " and method " + request.getMethod());

            return; // Error Response is preset
         }

         // Sub-resource method, sub-resource locator or both acceptable.
         // If both, sub-resource method and sub-resource then do next:
         // Check number of characters and number of variables in URI pattern, if
         // the same then sub-resource method has higher priority, otherwise
         // sub-resource with 'higher' URI pattern selected.
         if ((!hasAcceptableLocator && match)
            || (hasAcceptableLocator && match && compareSubResources(methods.get(0), locators.get(0)) < 0))
         {
            // sub-resource method
            invokeSubResourceMethod(requestPath, methods.get(0), resource, context, request, response);
         }
         else if ((hasAcceptableLocator && !match)
            || (hasAcceptableLocator && match && compareSubResources(methods.get(0), locators.get(0)) > 0))
         {
            // sub-resource locator
            invokeSuResourceLocator(requestPath, locators.get(0), resource, context, request, response);
         }
      }

   }

   /**
    * Invoke resource methods.
    *
    * @param rmd See {@link ResourceMethodDescriptor}
    * @param resource instance of resource class
    * @param context See {@link ApplicationContextImpl}
    * @param request See {@link GenericContainerRequest}
    * @param response See {@link GenericContainerResponse}
    * @see ResourceMethodDescriptor
    */
   private void invokeResourceMethod(ResourceMethodDescriptor rmd, Object resource, ApplicationContext context,
      GenericContainerRequest request, GenericContainerResponse response)
   {
      // save resource in hierarchy
      context.addMatchedResource(resource);

      Class<?> returnType = rmd.getResponseType();
      MethodInvoker invoker = context.getMethodInvoker(rmd);
      Object o = invoker.invokeMethod(resource, rmd, context);
      processResponse(o, returnType, request, response, rmd.produces());
   }

   /**
    * Invoke sub-resource methods.
    *
    * @param requestPath request path
    * @param srmd See {@link SubResourceMethodDescriptor}
    * @param resource instance of resource class
    * @param context See {@link ApplicationContextImpl}
    * @param request See {@link GenericContainerRequest}
    * @param response See {@link GenericContainerResponse}
    * @see SubResourceMethodDescriptor
    */
   private void invokeSubResourceMethod(String requestPath, SubResourceMethodDescriptor srmd, Object resource,
      ApplicationContext context, GenericContainerRequest request, GenericContainerResponse response)
   {
      // save resource in hierarchy
      context.addMatchedResource(resource);
      // save the sub-resource method URI in hierarchy
      context.addMatchedURI(requestPath);
      // save parameters values, actually parameters was save before, now just
      // map parameter's names to values
      context.setParameterNames(srmd.getUriPattern().getParameterNames());

      Class<?> returnType = srmd.getResponseType();
      MethodInvoker invoker = context.getMethodInvoker(srmd);
      Object o = invoker.invokeMethod(resource, srmd, context);
      processResponse(o, returnType, request, response, srmd.produces());
   }

   /**
    * Invoke sub-resource locators.
    *
    * @param requestPath request path
    * @param srld See {@link SubResourceLocatorDescriptor}
    * @param resource instance of resource class
    * @param context See {@link ApplicationContextImpl}
    * @param request See {@link GenericContainerRequest}
    * @param response See {@link GenericContainerResponse}
    * @see SubResourceLocatorDescriptor
    */
   private void invokeSuResourceLocator(String requestPath, SubResourceLocatorDescriptor srld, Object resource,
      ApplicationContext context, GenericContainerRequest request, GenericContainerResponse response)
   {
      context.addMatchedResource(resource);
      // take the tail of the request path, the tail will be new request path
      // for lower resources
      String newRequestPath = getPathTail(context.getParameterValues());
      // save the resource class URI in hierarchy
      context.addMatchedURI(requestPath.substring(0, requestPath.lastIndexOf(newRequestPath)));
      // save parameters values, actually parameters was save before, now just
      // map parameter's names to values
      context.setParameterNames(srld.getUriPattern().getParameterNames());

      // NOTE Locators can't accept entity
      MethodInvoker invoker = context.getMethodInvoker(srld);

      resource = invoker.invokeMethod(resource, srld, context);

      AbstractResourceDescriptor descriptor =
         new AbstractResourceDescriptorImpl(resource.getClass(), ComponentLifecycleScope.SINGLETON);
      SingletonObjectFactory<AbstractResourceDescriptor> locResource =
         new SingletonObjectFactory<AbstractResourceDescriptor>(descriptor, resource);

      // dispatch again newly created resource
      dispatch(request, response, context, locResource, resource, newRequestPath);
   }

   /**
    * Compare two sub-resources. One of it is
    * {@link SubResourceMethodDescriptor} and other one id
    * {@link SubResourceLocatorDescriptor}. First compare UriPattern, see
    * {@link UriPattern#URIPATTERN_COMPARATOR}. NOTE URI comparator compare
    * UriPattrens for descending sorting. So it it return negative integer then
    * it minds SubResourceMethodDescriptor has higher priority by UriPattern
    * comparison. If comparator return positive integer then
    * SubResourceLocatorDescriptor has higher priority. And finally if zero was
    * returned then UriPattern is equals, in this case
    * SubResourceMethodDescriptor must be selected.
    *
    * @param srmd See {@link SubResourceMethodDescriptor}
    * @param srld See {@link SubResourceLocatorDescriptor}
    * @return result of comparison sub-resources
    */
   private int compareSubResources(SubResourceMethodDescriptor srmd, SubResourceLocatorDescriptor srld)
   {
      int r = UriPattern.URIPATTERN_COMPARATOR.compare(srmd.getUriPattern(), srld.getUriPattern());
      // NOTE If patterns are the same sub-resource method has priority
      if (r == 0)
         return -1;
      return r;
   }

   /**
    * Process result of invoked method, and set {@link Response} parameters
    * dependent of returned object.
    *
    * @param o result of invoked method
    * @param returnType type of returned object
    * @param request See {@link GenericContainerRequest}
    * @param response See {@link GenericContainerResponse}
    * @param produces list of method produces media types
    * @see ResourceMethodDescriptor
    * @see SubResourceMethodDescriptor
    * @see SubResourceLocatorDescriptor
    */
   private static void processResponse(Object o, Class<?> returnType, GenericContainerRequest request,
      GenericContainerResponse response, List<MediaType> produces)
   {
      if (returnType == void.class || o == null)
      {
         response.setResponse(Response.noContent().build());
      }
      else
      {
         // get most acceptable media type for response
         MediaType contentType = request.getAcceptableMediaType(produces);
         if (Response.class.isAssignableFrom(returnType))
         {
            Response r = (Response)o;
            // If content-type is not set then add it
            if (r.getMetadata().getFirst(HttpHeaders.CONTENT_TYPE) == null && r.getEntity() != null)
            {
               r.getMetadata().putSingle(HttpHeaders.CONTENT_TYPE, contentType);
            }
            response.setResponse(r);
         }
         else if (GenericEntity.class.isAssignableFrom(returnType))
         {
            response.setResponse(Response.ok(o, contentType).build());
         }
         else
         {
            response.setResponse(Response.ok(o, contentType).build());
         }
      }
   }

   /**
    * Process resource methods.
    *
    * @param <T> ResourceMethodDescriptor extension
    * @param rmm See {@link ResourceMethodMap}
    * @param request See {@link GenericContainerRequest}
    * @param response See {@link GenericContainerResponse}
    * @param methods list for method resources
    * @return true if at least one resource method found false otherwise
    */
   private static <T extends ResourceMethodDescriptor> boolean processResourceMethod(ResourceMethodMap<T> rmm,
      GenericContainerRequest request, GenericContainerResponse response, List<T> methods)
   {
      String method = request.getMethod();
      List<T> rmds = rmm.getList(method);
      if (rmds == null || rmds.size() == 0)
      {
         response.setResponse(Response.status(405).header("Allow", HeaderHelper.convertToString(rmm.getAllow()))
            .entity(method + " method is not allowed for resource " + ApplicationContextImpl.getCurrent().getPath())
            .type(MediaType.TEXT_PLAIN).build());
         return false;
      }
      MediaType contentType = request.getMediaType();
      if (contentType == null)
      {
         methods.addAll(rmds);
      }
      else
      {
         for (T rmd : rmds)
         {
            if (MediaTypeHelper.isConsume(rmd.consumes(), contentType))
               methods.add(rmd);
         }
      }

      if (methods.isEmpty())
      {
         response.setResponse(Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
            .entity("Media type " + contentType + " is not supported.").type(MediaType.TEXT_PLAIN).build());
         return false;
      }

      List<MediaType> acceptable = request.getAcceptableMediaTypes();
      float previousQValue = 0.0F;
      int n = 0, p = 0;
      for (ListIterator<T> i = methods.listIterator(); i.hasNext();)
      {
         n = i.nextIndex();
         ResourceMethodDescriptor rmd = i.next();
         float qValue = MediaTypeHelper.processQuality(acceptable, rmd.produces());
         if (qValue > previousQValue)
         {
            previousQValue = qValue;
            p = n; // position of the best resource at the moment
         }
         else
         {
            i.remove(); // qValue is less then previous one
         }
      }

      if (!methods.isEmpty())
      {
         // remove all with lower q value
         if (methods.size() > 1)
         {
            n = 0;
            for (Iterator<T> i = methods.listIterator(); i.hasNext(); i.remove(), n++)
            {
               i.next();
               if (n == p)
                  break; // get index p in list then stop removing
            }
         }

         return true;
      }

      response.setResponse(Response.status(Response.Status.NOT_ACCEPTABLE).entity("Not Acceptable")
         .type(MediaType.TEXT_PLAIN).build());
      return false;
   }

   /**
    * Process sub-resource methods.
    *
    * @param srmm See {@link SubResourceLocatorMap}
    * @param requestedPath part of requested path
    * @param request See {@link GenericContainerRequest}
    * @param response See {@link GenericContainerResponse}
    * @param capturingValues the list for keeping template values. See
    *        {@link javax.ws.rs.core.UriInfo#getPathParameters()}
    * @param methods list for method resources
    * @return true if at least one sub-resource method found false otherwise
    */
   private static boolean processSubResourceMethod(SubResourceMethodMap srmm, String requestedPath,
      GenericContainerRequest request, GenericContainerResponse response, List<String> capturingValues,
      List<SubResourceMethodDescriptor> methods)
   {
      ResourceMethodMap<SubResourceMethodDescriptor> rmm = null;
      for (Entry<UriPattern, ResourceMethodMap<SubResourceMethodDescriptor>> e : srmm.entrySet())
      {
         if (e.getKey().match(requestedPath, capturingValues))
         {
            int len = capturingValues.size();
            if (capturingValues.get(len - 1) != null && !"/".equals(capturingValues.get(len - 1)))
               continue;

            rmm = e.getValue();
            break;
         }
      }

      if (rmm == null)
      {
         response.setResponse(Response.status(Status.NOT_FOUND)
            .entity("There is no any resources matched to request path " + requestedPath).type(MediaType.TEXT_PLAIN)
            .build());
         return false;
      }

      List<SubResourceMethodDescriptor> l = new ArrayList<SubResourceMethodDescriptor>();
      boolean match = processResourceMethod(rmm, request, response, l);

      if (match)
      {
         // for cast, Iterator contains SubResourceMethodDescriptor
         @SuppressWarnings("rawtypes")
         Iterator i = l.iterator();
         while (i.hasNext())
            methods.add((SubResourceMethodDescriptor)i.next());
      }

      return match;
   }

   /**
    * Process sub-resource locators.
    *
    * @param srlm See {@link SubResourceLocatorMap}
    * @param requestedPath part of requested path
    * @param capturingValues the list for keeping template values
    * @param locators list for sub-resource locators
    * @return true if at least one SubResourceLocatorDescriptor found false
    *         otherwise
    */
   private static boolean processSubResourceLocator(SubResourceLocatorMap srlm, String requestedPath,
      List<String> capturingValues, List<SubResourceLocatorDescriptor> locators)
   {
      for (Map.Entry<UriPattern, SubResourceLocatorDescriptor> e : srlm.entrySet())
      {
         if (e.getKey().match(requestedPath, capturingValues))
            locators.add(e.getValue());
      }

      return !locators.isEmpty();
   }

   /**
    * Get root resource.
    *
    * @param parameterValues is taken from context
    * @param requestPath is taken from context
    * @return root resource
    * @throws WebApplicationException if there is no matched root resources.
    *         Exception with prepared error response with 'Not Found' status
    */
   protected ObjectFactory<AbstractResourceDescriptor> getRootResourse(List<String> parameterValues, String requestPath)
   {
      ObjectFactory<AbstractResourceDescriptor> resourceFactory =
         resourceBinder.getMatchedResource(requestPath, parameterValues);
      if (resourceFactory == null)
      {
         if (LOG.isDebugEnabled())
         {
            LOG.debug("Root resource not found for " + requestPath);
         }
         // Stop here, there is no matched root resource
         throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
            .entity("There is no any resources matched to request path " + requestPath).type(MediaType.TEXT_PLAIN)
            .build());
      }
      return resourceFactory;
   }
}
