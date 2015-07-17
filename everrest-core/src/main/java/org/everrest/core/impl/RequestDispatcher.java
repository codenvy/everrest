/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl;

import org.everrest.core.ApplicationContext;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.Lifecycle;
import org.everrest.core.ObjectFactory;
import org.everrest.core.ResourceBinder;
import org.everrest.core.SingletonObjectFactory;
import org.everrest.core.impl.async.AsynchronousJob;
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
import org.everrest.core.util.Tracer;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

/**
 * Lookup resource which can serve request.
 *
 * @author andrew00x
 */
public class RequestDispatcher {
    /** Logger. */
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RequestDispatcher.class);
    /** See {@link org.everrest.core.ResourceBinder}. */
    protected final ResourceBinder resourceBinder;

    private final HelperCache<Class<?>, AbstractResourceDescriptor> locatorDescriptorCache = new HelperCache<>(60 * 1000, 50);

    /**
     * Constructs new instance of RequestDispatcher.
     *
     * @param resourceBinder
     *         See {@link org.everrest.core.ResourceBinder}
     */
    public RequestDispatcher(ResourceBinder resourceBinder) {
        this.resourceBinder = resourceBinder;
    }

    /**
     * Dispatch {@link org.everrest.core.impl.ContainerRequest} to resource which can serve request.
     *
     * @param request
     *         See {@link org.everrest.core.GenericContainerRequest}
     * @param response
     *         See {@link org.everrest.core.GenericContainerResponse}
     */
    public void dispatch(GenericContainerRequest request, GenericContainerResponse response) {
        ApplicationContext context = ApplicationContextImpl.getCurrent();
        String requestPath = context.getPath(false);
        List<String> parameterValues = context.getParameterValues();

        ObjectFactory<AbstractResourceDescriptor> resourceFactory = getRootResource(parameterValues, requestPath);

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
     * Get last element from path parameters. This element will be used as request path for child resources.
     *
     * @param parameterValues
     *         See {@link org.everrest.core.impl.ApplicationContextImpl#getParameterValues()}
     * @return last element from given list or empty string if last element is null
     */
    protected static String getPathTail(List<String> parameterValues) {
        int i = parameterValues.size() - 1;
        return parameterValues.get(i) != null ? parameterValues.get(i) : "";
    }

    /**
     * Process resource methods, sub-resource methods and sub-resource locators to find the best one for serve request.
     *
     * @param request
     *         See {@link org.everrest.core.GenericContainerRequest}
     * @param response
     *         See {@link org.everrest.core.GenericContainerResponse}
     * @param context
     *         See {@link org.everrest.core.impl.ApplicationContextImpl}
     * @param resourceFactory
     *         the root resource factory or resource factory which was created by previous sub-resource
     *         locator
     * @param resource
     *         instance of resource class
     * @param requestPath
     *         request path, it is relative path to the base URI or other resource which was called before
     *         (one of sub-resource locators)
     */
    protected void dispatch(GenericContainerRequest request,
                            GenericContainerResponse response,
                            ApplicationContext context,
                            ObjectFactory<AbstractResourceDescriptor> resourceFactory,
                            Object resource,
                            String requestPath) {
        List<String> parameterValues = context.getParameterValues();
        int len = parameterValues.size();

        // Resource method or sub-resource method or sub-resource locator ?

        ResourceMethodMap<ResourceMethodDescriptor> rmm = resourceFactory.getObjectModel().getResourceMethods();
        if ((parameterValues.get(len - 1) == null || "/".equals(parameterValues.get(len - 1))) && rmm.size() > 0) {
            // Resource method, then process HTTP method and consume/produce media types.

            List<ResourceMethodDescriptor> methods = new ArrayList<>();
            boolean match = processResourceMethod(rmm, request, response, methods);
            if (match) {
                if (Tracer.isTracingEnabled()) {
                    Tracer.trace("Matched resource method for method \"" + request.getMethod()
                                 + "\", media type \"" + request.getMediaType()
                                 + "\" = (" + methods.get(0).getMethod() + ")");
                }

                invokeResourceMethod(methods.get(0), resource, context, request, response);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Not found resource method for method " + request.getMethod());
                }

                // Error Response is preset.
            }
        } else {
            // Sub-resource method or locator ?
            SubResourceMethodMap srmm = resourceFactory.getObjectModel().getSubResourceMethods();
            SubResourceLocatorMap srlm = resourceFactory.getObjectModel().getSubResourceLocators();

            // Check sub-resource methods.
            List<SubResourceMethodDescriptor> methods = new ArrayList<>();
            boolean match = processSubResourceMethod(srmm, requestPath, request, response, parameterValues, methods);

            if (match && Tracer.isTracingEnabled()) {
                Tracer.trace("Matched sub-resource method for method \"" + request.getMethod()
                             + "\", path \"" + requestPath
                             + "\", media type \"" + request.getMediaType()
                             + "\" = (" + methods.get(0).getMethod() + ")");
            }

            // Check sub-resource locators.
            List<SubResourceLocatorDescriptor> locators = new ArrayList<>();
            boolean acceptableLocators = processSubResourceLocator(srlm, requestPath, parameterValues, locators);

            if (acceptableLocators && Tracer.isTracingEnabled()) {
                Tracer.trace("Matched sub-resource locator for path \"" + requestPath
                             + "\", media type \"" + request.getMediaType()
                             + "\" = (" + locators.get(0).getMethod() + ")");
            }

            // Sub-resource method or sub-resource locator should be found,
            // otherwise error response with corresponding status already set.
            if (match || acceptableLocators) {
                // Reset any previous responses.
                response.setResponse(null);

                // Sub-resource method, sub-resource locator or both acceptable.
                // If both, sub-resource method and sub-resource then do next:
                // Check number of characters and number of variables in URI pattern, if
                // the same then sub-resource method has higher priority, otherwise
                // sub-resource with 'higher' URI pattern selected.

                if (match && (!acceptableLocators || compareSubResources(methods.get(0), locators.get(0)) < 0)) {
                    // Sub-resource method

                    if (Tracer.isTracingEnabled()) {
                        Tracer.trace("Sub-resource method (" + methods.get(0).getMethod() + ") selected. ");
                    }

                    invokeSubResourceMethod(requestPath, methods.get(0), resource, context, request, response);
                } else {
                    // Sub-resource locator.

                    if (Tracer.isTracingEnabled()) {
                        Tracer.trace("Sub-resource locator (" + locators.get(0).getMethod() + ") selected. ");
                    }

                    invokeSubResourceLocator(requestPath, locators.get(0), resource, context, request, response);
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Not found sub-resource methods nor sub-resource locators for path " + requestPath
                              + " and method " + request.getMethod());
                }

                // Error Response is preset.
            }
        }
    }

    /**
     * Invoke resource methods.
     *
     * @param rmd
     *         See {@link org.everrest.core.resource.ResourceMethodDescriptor}
     * @param resource
     *         instance of resource class
     * @param context
     *         See {@link org.everrest.core.impl.ApplicationContextImpl}
     * @param request
     *         See {@link org.everrest.core.GenericContainerRequest}
     * @param response
     *         See {@link org.everrest.core.GenericContainerResponse}
     * @see org.everrest.core.resource.ResourceMethodDescriptor
     */
    private void invokeResourceMethod(ResourceMethodDescriptor rmd,
                                      Object resource,
                                      ApplicationContext context,
                                      GenericContainerRequest request,
                                      GenericContainerResponse response) {
        // Save resource in hierarchy.
        context.addMatchedResource(resource);
        doInvokeResource(rmd, resource, context, request, response);
    }

    /**
     * Invoke sub-resource methods.
     *
     * @param requestPath
     *         request path
     * @param srmd
     *         See {@link org.everrest.core.resource.SubResourceMethodDescriptor}
     * @param resource
     *         instance of resource class
     * @param context
     *         See {@link org.everrest.core.impl.ApplicationContextImpl}
     * @param request
     *         See {@link org.everrest.core.GenericContainerRequest}
     * @param response
     *         See {@link org.everrest.core.GenericContainerResponse}
     * @see org.everrest.core.resource.SubResourceMethodDescriptor
     */
    private void invokeSubResourceMethod(String requestPath,
                                         SubResourceMethodDescriptor srmd,
                                         Object resource,
                                         ApplicationContext context,
                                         GenericContainerRequest request,
                                         GenericContainerResponse response) {
        // save resource in hierarchy
        context.addMatchedResource(resource);
        // save the sub-resource method URI in hierarchy
        context.addMatchedURI(requestPath);
        // save parameters values, actually parameters was save before, now just map parameter's names to values
        context.setParameterNames(srmd.getUriPattern().getParameterNames());
        doInvokeResource(srmd, resource, context, request, response);
    }

    private void doInvokeResource(ResourceMethodDescriptor method,
                                  Object resource,
                                  ApplicationContext context,
                                  GenericContainerRequest request,
                                  GenericContainerResponse response) {
        MethodInvoker invoker = context.getMethodInvoker(method);
        Object o = invoker.invokeMethod(resource, method, context);
        processResponse(o, request, response, method.produces(), context);
    }

    /**
     * Invoke sub-resource locators.
     *
     * @param requestPath
     *         request path
     * @param srld
     *         See {@link org.everrest.core.resource.SubResourceLocatorDescriptor}
     * @param resource
     *         instance of resource class
     * @param context
     *         See {@link org.everrest.core.impl.ApplicationContextImpl}
     * @param request
     *         See {@link org.everrest.core.GenericContainerRequest}
     * @param response
     *         See {@link org.everrest.core.GenericContainerResponse}
     * @see org.everrest.core.resource.SubResourceLocatorDescriptor
     */
    private void invokeSubResourceLocator(String requestPath,
                                          SubResourceLocatorDescriptor srld,
                                          Object resource,
                                          ApplicationContext context,
                                          GenericContainerRequest request,
                                          GenericContainerResponse response) {
        context.addMatchedResource(resource);
        // take the tail of the request path, the tail will be new request path
        // for lower resources
        String newRequestPath = getPathTail(context.getParameterValues());
        // save the resource class URI in hierarchy
        context.addMatchedURI(requestPath.substring(0, requestPath.lastIndexOf(newRequestPath)));
        // save parameters values, actually parameters was save before, now just
        // map parameter's names to values
        context.setParameterNames(srld.getUriPattern().getParameterNames());

        // NOTE Locator can't accept entity
        MethodInvoker invoker = context.getMethodInvoker(srld);

        resource = invoker.invokeMethod(resource, srld, context);

        AbstractResourceDescriptor descriptor;
        synchronized (locatorDescriptorCache) {
            descriptor = locatorDescriptorCache.get(resource.getClass());
            if (descriptor == null) {
                descriptor = new AbstractResourceDescriptorImpl(resource);
                locatorDescriptorCache.put(resource.getClass(), descriptor);
            }
        }
        SingletonObjectFactory<AbstractResourceDescriptor> locResource = new SingletonObjectFactory<>(descriptor, resource);

        if (context instanceof Lifecycle) {
            @SuppressWarnings("unchecked")
            List<LifecycleComponent> perRequest =
                    (List<LifecycleComponent>)context.getAttributes().get("org.everrest.lifecycle.PerRequest");
            if (perRequest == null) {
                perRequest = new LinkedList<>();
                context.getAttributes().put("org.everrest.lifecycle.PerRequest", perRequest);
            }
            // We do nothing for initialize resource since it is created by other resource
            // but we lets to process 'destroy' method.
            perRequest.add(new LifecycleComponent(resource));
        }

        if (Tracer.isTracingEnabled()) {
            Tracer.trace("Sub-resource for request path \"" + newRequestPath + "\" = (" + resource + ")");
        }

        // dispatch again newly created resource
        dispatch(request, response, context, locResource, resource, newRequestPath);
    }

    /**
     * Compare two sub-resources. One of it is {@link org.everrest.core.resource.SubResourceMethodDescriptor} and other
     * one id
     * {@link org.everrest.core.resource.SubResourceLocatorDescriptor}. First compare UriPattern, see {@link
     * org.everrest.core.uri.UriPattern#URIPATTERN_COMPARATOR}.
     * NOTE
     * URI comparator compare UriPatterns for descending sorting. So it it return negative integer then it minds
     * SubResourceMethodDescriptor has higher priority by UriPattern comparison. If comparator return positive integer
     * then SubResourceLocatorDescriptor has higher priority. And finally if zero was returned then UriPattern is
     * equals, in this case SubResourceMethodDescriptor must be selected.
     *
     * @param srmd
     *         See {@link org.everrest.core.resource.SubResourceMethodDescriptor}
     * @param srld
     *         See {@link org.everrest.core.resource.SubResourceLocatorDescriptor}
     * @return result of comparison sub-resources
     */
    private int compareSubResources(SubResourceMethodDescriptor srmd, SubResourceLocatorDescriptor srld) {
        int r = UriPattern.URIPATTERN_COMPARATOR.compare(srmd.getUriPattern(), srld.getUriPattern());
        // NOTE If patterns are the same sub-resource method has priority
        return r == 0 ? -1 : r;
    }

    /**
     * Process result of invoked method, and set {@link javax.ws.rs.core.Response} parameters dependent of returned
     * object.
     *
     * @param o
     *         result of invoked method
     * @param request
     *         See {@link org.everrest.core.GenericContainerRequest}
     * @param response
     *         See {@link org.everrest.core.GenericContainerResponse}
     * @param produces
     *         list of method produces media types
     * @param context
     * @see org.everrest.core.resource.ResourceMethodDescriptor
     * @see org.everrest.core.resource.SubResourceMethodDescriptor
     * @see org.everrest.core.resource.SubResourceLocatorDescriptor
     */
    private void processResponse(Object o,
                                 GenericContainerRequest request,
                                 GenericContainerResponse response,
                                 List<MediaType> produces,
                                 ApplicationContext context) {
        if (response.getResponse() != null) {
            // Response may be set for asynchronous jobs.
            return;
        }
        if (o == null || o.getClass() == void.class || o.getClass() == Void.class) {
            response.setResponse(Response.noContent().build());
        } else if (o instanceof AsynchronousJob) {
            final String internalJobUri = ((AsynchronousJob)o).getJobURI();
            final String externalJobUri = context.getBaseUriBuilder().path(internalJobUri).build().toString();
            response.setResponse(Response.status(Response.Status.ACCEPTED)
                                         .header(HttpHeaders.LOCATION, externalJobUri)
                                         .entity(externalJobUri)
                                         .type(MediaType.TEXT_PLAIN).build());
        } else {
            // get most acceptable media type for response
            MediaType contentType = request.getAcceptableMediaType(produces);
            if (Response.class.isAssignableFrom(o.getClass())) {
                Response r = (Response)o;
                // If content-type is not set then add it
                if (r.getMetadata().getFirst(HttpHeaders.CONTENT_TYPE) == null && r.getEntity() != null) {
                    r.getMetadata().putSingle(HttpHeaders.CONTENT_TYPE, contentType);
                }
                response.setResponse(r);
            } else if (GenericEntity.class.isAssignableFrom(o.getClass())) {
                response.setResponse(Response.ok(o, contentType).build());
            } else {
                response.setResponse(Response.ok(o, contentType).build());
            }
        }
    }

    /**
     * Process resource methods.
     *
     * @param <T>
     *         ResourceMethodDescriptor extension
     * @param rmm
     *         See {@link org.everrest.core.resource.ResourceMethodMap}
     * @param request
     *         See {@link org.everrest.core.GenericContainerRequest}
     * @param response
     *         See {@link org.everrest.core.GenericContainerResponse}
     * @param methods
     *         list for method resources
     * @return true if at least one resource method found false otherwise
     */
    private <T extends ResourceMethodDescriptor> boolean processResourceMethod(ResourceMethodMap<T> rmm,
                                                                               GenericContainerRequest request,
                                                                               GenericContainerResponse response,
                                                                               List<T> methods) {
        String method = request.getMethod();
        List<T> rmds = rmm.getList(method);
        if (rmds == null || rmds.size() == 0) {
            response.setResponse(Response.status(405).header("Allow", HeaderHelper.convertToString(rmm.getAllow()))
                                         .entity(method + " method is not allowed for resource " +
                                                 ApplicationContextImpl.getCurrent().getPath())
                                         .type(MediaType.TEXT_PLAIN).build());
            return false;
        }
        MediaType contentType = request.getMediaType();
        if (contentType == null) {
            methods.addAll(rmds);
        } else {
            for (T rmd : rmds) {
                if (MediaTypeHelper.isConsume(rmd.consumes(), contentType)) {
                    methods.add(rmd);
                }
            }
        }

        if (methods.isEmpty()) {
            response.setResponse(Response.status(Status.UNSUPPORTED_MEDIA_TYPE)
                                         .entity("Media type " + contentType + " is not supported.").type(MediaType.TEXT_PLAIN).build());
            return false;
        }

        List<MediaType> acceptable = request.getAcceptableMediaTypes();
        float previousQValue = 0.0F;
        int n, p = 0;
        for (ListIterator<T> i = methods.listIterator(); i.hasNext(); ) {
            n = i.nextIndex();
            ResourceMethodDescriptor rmd = i.next();
            float qValue = MediaTypeHelper.processQuality(acceptable, rmd.produces());
            if (qValue > previousQValue) {
                previousQValue = qValue;
                p = n; // position of the best resource at the moment
            } else {
                i.remove(); // qValue is less then previous one
            }
        }

        if (!methods.isEmpty()) {
            // remove all with lower q value
            if (methods.size() > 1) {
                n = 0;
                for (Iterator<T> i = methods.listIterator(); i.hasNext(); i.remove(), n++) {
                    i.next();
                    if (n == p) {
                        break; // get index p in list then stop removing
                    }
                }
            }

            return true;
        }

        response.setResponse(Response.status(Status.NOT_ACCEPTABLE).entity("Not Acceptable")
                                     .type(MediaType.TEXT_PLAIN).build());
        return false;
    }

    /**
     * Process sub-resource methods.
     *
     * @param srmm
     *         See {@link org.everrest.core.resource.SubResourceLocatorMap}
     * @param requestedPath
     *         part of requested path
     * @param request
     *         See {@link org.everrest.core.GenericContainerRequest}
     * @param response
     *         See {@link org.everrest.core.GenericContainerResponse}
     * @param capturingValues
     *         the list for keeping template values. See
     *         {@link javax.ws.rs.core.UriInfo#getPathParameters()}
     * @param methods
     *         list for method resources
     * @return true if at least one sub-resource method found false otherwise
     */
    private boolean processSubResourceMethod(SubResourceMethodMap srmm,
                                             String requestedPath,
                                             GenericContainerRequest request,
                                             GenericContainerResponse response,
                                             List<String> capturingValues,
                                             List<SubResourceMethodDescriptor> methods) {
        ResourceMethodMap<SubResourceMethodDescriptor> rmm = null;
        for (Entry<UriPattern, ResourceMethodMap<SubResourceMethodDescriptor>> e : srmm.entrySet()) {
            if (e.getKey().match(requestedPath, capturingValues)) {
                int len = capturingValues.size();
                if (capturingValues.get(len - 1) != null && !"/".equals(capturingValues.get(len - 1))) {
                    continue;
                }

                rmm = e.getValue();
                break;
            }
        }

        if (rmm == null) {
            response.setResponse(Response
                                         .status(Status.NOT_FOUND)
                                         .entity("There is no any resources matched to request path " + requestedPath)
                                         .type(MediaType.TEXT_PLAIN)
                                         .build());
            return false;
        }

        List<SubResourceMethodDescriptor> l = new ArrayList<>();
        boolean match = processResourceMethod(rmm, request, response, l);

        if (match) {
            for (SubResourceMethodDescriptor aL : l) {
                methods.add(aL);
            }
        }

        return match;
    }

    /**
     * Process sub-resource locators.
     *
     * @param srlm
     *         See {@link org.everrest.core.resource.SubResourceLocatorMap}
     * @param requestedPath
     *         part of requested path
     * @param capturingValues
     *         the list for keeping template values
     * @param locators
     *         list for sub-resource locators
     * @return true if at least one SubResourceLocatorDescriptor found false otherwise
     */
    private boolean processSubResourceLocator(SubResourceLocatorMap srlm,
                                              String requestedPath,
                                              List<String> capturingValues,
                                              List<SubResourceLocatorDescriptor> locators) {
        for (Entry<UriPattern, SubResourceLocatorDescriptor> e : srlm.entrySet()) {
            if (e.getKey().match(requestedPath, capturingValues)) {
                locators.add(e.getValue());
            }
        }

        return !locators.isEmpty();
    }

    /**
     * Get root resource.
     *
     * @param parameterValues
     *         is taken from context
     * @param requestPath
     *         is taken from context
     * @return root resource
     * @throws javax.ws.rs.WebApplicationException
     *         if there is no matched root resources. Exception with prepared error response
     *         with
     *         'Not Found' status
     */
    protected ObjectFactory<AbstractResourceDescriptor> getRootResource(List<String> parameterValues, String requestPath) {
        ObjectFactory<AbstractResourceDescriptor> resourceFactory =
                resourceBinder.getMatchedResource(requestPath, parameterValues);
        if (resourceFactory == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Root resource not found for " + requestPath);
            }

            // Stop here, there is no matched root resource
            throw new WebApplicationException(Response.status(Status.NOT_FOUND)
                                                      .entity("There is no any resources matched to request path " + requestPath)
                                                      .type(MediaType.TEXT_PLAIN)
                                                      .build());
        }

        if (Tracer.isTracingEnabled()) {
            AbstractResourceDescriptor model = resourceFactory.getObjectModel();
            Tracer.trace("Matched root resource for request path \"" + requestPath
                         + "\" = (@Path \"" + model.getPathValue().getPath() + "\", " + model.getObjectClass() + ")"
                        );
        }

        return resourceFactory;
    }
}
