/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;

import org.everrest.core.ApplicationContext;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.ObjectFactory;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.async.AsynchronousJob;
import org.everrest.core.impl.header.AcceptMediaType;
import org.everrest.core.impl.header.MediaTypeHelper;
import org.everrest.core.impl.resource.AbstractResourceDescriptor;
import org.everrest.core.method.MethodInvoker;
import org.everrest.core.resource.ResourceDescriptor;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.resource.SubResourceLocatorDescriptor;
import org.everrest.core.resource.SubResourceMethodDescriptor;
import org.everrest.core.uri.UriPattern;
import org.everrest.core.util.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagateIfPossible;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.HttpHeaders.ALLOW;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.METHOD_NOT_ALLOWED;
import static javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNSUPPORTED_MEDIA_TYPE;
import static org.everrest.core.impl.header.HeaderHelper.convertToString;
import static org.everrest.core.impl.header.MediaTypeHelper.findFistCompatibleAcceptMediaType;

/**
 * Lookup resource which can serve request.
 *
 * @author andrew00x
 */
public class RequestDispatcher {
    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(RequestDispatcher.class);
    /** See {@link org.everrest.core.ResourceBinder}. */
    private final ResourceBinder resourceBinder;
    private final LoadingCache<Class<?>, ResourceDescriptor> locatorDescriptorCache;

    /**
     * Constructs new instance of RequestDispatcher.
     *
     * @param resourceBinder
     *         See {@link org.everrest.core.ResourceBinder}
     */
    public RequestDispatcher(ResourceBinder resourceBinder) {
        checkNotNull(resourceBinder);
        this.resourceBinder = resourceBinder;
        locatorDescriptorCache = CacheBuilder.newBuilder()
                                             .concurrencyLevel(8)
                                             .maximumSize(256)
                                             .expireAfterAccess(10, MINUTES)
                                             .build(new CacheLoader<Class<?>, ResourceDescriptor>() {
                                                 @Override
                                                 public ResourceDescriptor load(Class<?> aClass) {
                                                     return new AbstractResourceDescriptor(aClass);
                                                 }
                                             });
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
        ApplicationContext context = ApplicationContext.getCurrent();
        String requestPath = getRequestPathWithoutMatrixParameters(context);
        List<String> parameterValues = context.getParameterValues();

        ObjectFactory<ResourceDescriptor> resourceFactory = getRootResource(parameterValues, requestPath);
        if (resourceFactory == null) {
            LOG.debug("Root resource not found for {}", requestPath);
            response.setResponse(Response.status(NOT_FOUND)
                                         .entity(String.format("There is no any resources matched to request path %s", requestPath))
                                         .type(TEXT_PLAIN)
                                         .build());
            return;
        }

        // Take the tail of the request path, the tail will be requested path
        // for lower resources, e. g. ResourceClass -> Sub-resource method/locator
        String newRequestPath = getPathTail(parameterValues);
        context.addMatchedURI(requestPath.substring(0, requestPath.lastIndexOf(newRequestPath)));
        context.setParameterNames(resourceFactory.getObjectModel().getUriPattern().getParameterNames());

        Object resource = resourceFactory.getInstance(context);
        dispatch(request, response, context, resourceFactory.getObjectModel(), resource, newRequestPath);
    }

    public ResourceBinder getResources() {
        return resourceBinder;
    }

    private String getRequestPathWithoutMatrixParameters(ApplicationContext context) {
        List<PathSegment> requestPathSegments = context.getPathSegments(false);
        if (requestPathSegments.isEmpty()) {
            return "/";
        }
        StringBuilder requestPath = new StringBuilder();
        for (PathSegment pathSegment : requestPathSegments) {
            requestPath.append('/');
            requestPath.append(pathSegment.getPath());
        }

        return requestPath.toString();
    }

    /**
     * Get last element from path parameters. This element will be used as request path for child resources.
     *
     * @param parameterValues
     *         See {@link ApplicationContext#getParameterValues()}
     * @return last element from given list or empty string if last element is null
     */
    private String getPathTail(List<String> parameterValues) {
        int i = parameterValues.size() - 1;
        return parameterValues.get(i) == null ? "" : parameterValues.get(i);
    }

    /**
     * Process resource methods, sub-resource methods and sub-resource locators to find the best one for serve request.
     *
     * @param request
     *         See {@link org.everrest.core.GenericContainerRequest}
     * @param response
     *         See {@link org.everrest.core.GenericContainerResponse}
     * @param context
     *         See {@link ApplicationContext}
     * @param resourceDescriptor
     *         the root resource descriptor or resource descriptor which was created by previous sub-resource locator
     * @param resource
     *         instance of resource class
     * @param requestPath
     *         request path, it is relative path to the base URI or other resource which was called before
     *         (one of sub-resource locators)
     */
    private void dispatch(GenericContainerRequest request,
                          GenericContainerResponse response,
                          ApplicationContext context,
                          ResourceDescriptor resourceDescriptor,
                          Object resource,
                          String requestPath) {
        List<String> parameterValues = context.getParameterValues();
        String lastParameterValue = Iterables.getLast(parameterValues);
        boolean resourceMethodRequested = lastParameterValue == null || "/".equals(lastParameterValue);
        Map<String, List<ResourceMethodDescriptor>> resourceMethods = resourceDescriptor.getResourceMethods();
        if (resourceMethodRequested && !resourceMethods.isEmpty()) {
            List<ResourceMethodDescriptor> matchedResourceMethods = new ArrayList<>();
            boolean match = processResourceMethod(resourceMethods, request, response, matchedResourceMethods);
            if (match) {
                ResourceMethodDescriptor mostMatchedResourceMethod = matchedResourceMethods.get(0);
                if (Tracer.isTracingEnabled()) {
                    Tracer.trace("Matched resource method for method \"%s\", media type \"%s\" = (%s)",
                                 request.getMethod(), request.getMediaType(), mostMatchedResourceMethod.getMethod());
                }

                invokeResourceMethod(mostMatchedResourceMethod, resource, context, request, response);
            } else {
                LOG.debug("Not found resource method for method {}", request.getMethod());
            }
        } else {
            Map<UriPattern, Map<String, List<SubResourceMethodDescriptor>>> subResourceMethods = resourceDescriptor.getSubResourceMethods();
            Map<UriPattern, SubResourceLocatorDescriptor> subResourceLocators = resourceDescriptor.getSubResourceLocators();

            List<SubResourceMethodDescriptor> matchedSubResourceMethods = new ArrayList<>();
            boolean match = processSubResourceMethod(subResourceMethods, requestPath, request, response, parameterValues, matchedSubResourceMethods);

            List<SubResourceLocatorDescriptor> matchedSubResourceLocators = new ArrayList<>();
            match |= processSubResourceLocator(subResourceLocators, requestPath, parameterValues, matchedSubResourceLocators);

            if (match) {
                response.setResponse(null);

                boolean foundMatchedSubResourceMethods = !matchedSubResourceMethods.isEmpty();
                boolean foundMatchedSubResourceLocators = !matchedSubResourceLocators.isEmpty();
                if (foundMatchedSubResourceMethods && Tracer.isTracingEnabled()) {
                    Tracer.trace("Matched sub-resource method for method \"%s\", path \"%s\", media type \"%s\" = (%s)",
                                 request.getMethod(), requestPath, request.getMediaType(), matchedSubResourceMethods.get(0).getMethod());
                }
                if (foundMatchedSubResourceLocators && Tracer.isTracingEnabled()) {
                    Tracer.trace("Matched sub-resource locator for path \"%s\", media type \"%s\" = (%s)",
                                 requestPath, request.getMediaType(), matchedSubResourceLocators.get(0).getMethod());
                }

                if (foundMatchedSubResourceMethods
                    && (!foundMatchedSubResourceLocators ||  compareSubResources(matchedSubResourceMethods.get(0), matchedSubResourceLocators.get(0)) < 0)) {

                    if (Tracer.isTracingEnabled()) {
                        Tracer.trace("Sub-resource method (%s) selected", matchedSubResourceMethods.get(0).getMethod());
                    }

                    invokeSubResourceMethod(requestPath, matchedSubResourceMethods.get(0), resource, context, request, response);
                } else {
                    if (Tracer.isTracingEnabled()) {
                        Tracer.trace("Sub-resource locator (%s) selected", matchedSubResourceLocators.get(0).getMethod());
                    }

                    invokeSubResourceLocator(requestPath, matchedSubResourceLocators.get(0), resource, context, request, response);
                }
            } else {
                LOG.debug("Not found sub-resource methods nor sub-resource locators for path {} and method {}", requestPath, request.getMethod());
            }
        }
    }

    /**
     * Invoke resource methods.
     *
     * @param resourceMethod
     *         See {@link org.everrest.core.resource.ResourceMethodDescriptor}
     * @param resource
     *         instance of resource class
     * @param context
     *         See {@link ApplicationContext}
     * @param request
     *         See {@link org.everrest.core.GenericContainerRequest}
     * @param response
     *         See {@link org.everrest.core.GenericContainerResponse}
     * @see org.everrest.core.resource.ResourceMethodDescriptor
     */
    private void invokeResourceMethod(ResourceMethodDescriptor resourceMethod,
                                      Object resource,
                                      ApplicationContext context,
                                      GenericContainerRequest request,
                                      GenericContainerResponse response) {
        context.addMatchedResource(resource);
        doInvokeResource(resourceMethod, resource, context, request, response);
    }

    /**
     * Invoke sub-resource methods.
     *
     * @param requestPath
     *         request path
     * @param subResourceMethod
     *         See {@link org.everrest.core.resource.SubResourceMethodDescriptor}
     * @param resource
     *         instance of resource class
     * @param context
     *         See {@link ApplicationContext}
     * @param request
     *         See {@link org.everrest.core.GenericContainerRequest}
     * @param response
     *         See {@link org.everrest.core.GenericContainerResponse}
     * @see org.everrest.core.resource.SubResourceMethodDescriptor
     */
    private void invokeSubResourceMethod(String requestPath,
                                         SubResourceMethodDescriptor subResourceMethod,
                                         Object resource,
                                         ApplicationContext context,
                                         GenericContainerRequest request,
                                         GenericContainerResponse response) {
        context.addMatchedResource(resource);
        context.addMatchedURI(requestPath);
        context.setParameterNames(subResourceMethod.getUriPattern().getParameterNames());
        doInvokeResource(subResourceMethod, resource, context, request, response);
    }

    private void doInvokeResource(ResourceMethodDescriptor method,
                                  Object resource,
                                  ApplicationContext context,
                                  GenericContainerRequest request,
                                  GenericContainerResponse response) {
        MethodInvoker invoker = context.getMethodInvoker(method);
        Object result = invoker.invokeMethod(resource, method, context);
        processResponse(result, request, response, method.produces(), context);
    }

    /**
     * Invoke sub-resource locators.
     *
     * @param requestPath
     *         request path
     * @param subResourceLocator
     *         See {@link org.everrest.core.resource.SubResourceLocatorDescriptor}
     * @param resource
     *         instance of resource class
     * @param context
     *         See {@link ApplicationContext}
     * @param request
     *         See {@link org.everrest.core.GenericContainerRequest}
     * @param response
     *         See {@link org.everrest.core.GenericContainerResponse}
     * @see org.everrest.core.resource.SubResourceLocatorDescriptor
     */
    private void invokeSubResourceLocator(String requestPath,
                                          SubResourceLocatorDescriptor subResourceLocator,
                                          Object resource,
                                          ApplicationContext context,
                                          GenericContainerRequest request,
                                          GenericContainerResponse response) {
        context.addMatchedResource(resource);
        String newRequestPath = getPathTail(context.getParameterValues());
        context.addMatchedURI(requestPath.substring(0, requestPath.lastIndexOf(newRequestPath)));
        context.setParameterNames(subResourceLocator.getUriPattern().getParameterNames());
        MethodInvoker invoker = context.getMethodInvoker(subResourceLocator);
        Object newResource = invoker.invokeMethod(resource, subResourceLocator, context);
        ResourceDescriptor descriptor;
        try {
            descriptor = locatorDescriptorCache.get(newResource.getClass());
        } catch (ExecutionException e) {
            propagateIfPossible(e.getCause());
            throw new RuntimeException(e.getCause());
        }

        @SuppressWarnings("unchecked")
        List<LifecycleComponent> perRequestComponents = (List<LifecycleComponent>)context.getAttributes().get("org.everrest.lifecycle.PerRequest");
        if (perRequestComponents == null) {
            context.getAttributes().put("org.everrest.lifecycle.PerRequest", perRequestComponents = new ArrayList<>());
        }
        // We do nothing for initialize resource since it is created by other resource but we lets to process 'destroy' method.
        perRequestComponents.add(new LifecycleComponent(newResource));

        if (Tracer.isTracingEnabled()) {
            Tracer.trace("Sub-resource for request path \"%s\" = (%s)", newRequestPath, newResource);
        }

        dispatch(request, response, context, descriptor, newResource, newRequestPath);
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
     * @param subResourceMethod
     *         See {@link org.everrest.core.resource.SubResourceMethodDescriptor}
     * @param subResourceLocator
     *         See {@link org.everrest.core.resource.SubResourceLocatorDescriptor}
     * @return result of comparison sub-resources
     */
    private int compareSubResources(SubResourceMethodDescriptor subResourceMethod, SubResourceLocatorDescriptor subResourceLocator) {
        int result = UriPattern.URIPATTERN_COMPARATOR.compare(subResourceMethod.getUriPattern(), subResourceLocator.getUriPattern());
        // NOTE If patterns are the same sub-resource method has priority
        return result == 0 ? -1 : result;
    }

    /**
     * Process result of invoked method, and set {@link javax.ws.rs.core.Response} parameters dependent of returned
     * object.
     *
     * @param methodInvocationResult
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
    private void processResponse(Object methodInvocationResult,
                                 GenericContainerRequest request,
                                 GenericContainerResponse response,
                                 List<MediaType> produces,
                                 ApplicationContext context) {
        if (response.getResponse() != null) {
            // Response may be set for asynchronous jobs.
            return;
        }
        if (methodInvocationResult == null || methodInvocationResult.getClass() == void.class || methodInvocationResult.getClass() == Void.class) {
            response.setResponse(Response.noContent().build());
        } else if (methodInvocationResult instanceof AsynchronousJob) {
            final String internalJobUri = ((AsynchronousJob)methodInvocationResult).getJobURI();
            final String externalJobUri = context.getBaseUriBuilder().path(internalJobUri).build().toString();
            response.setResponse(Response.status(ACCEPTED)
                                         .header(LOCATION, externalJobUri)
                                         .entity(externalJobUri)
                                         .type(TEXT_PLAIN).build());
        } else {
            MediaType contentType = request.getAcceptableMediaType(produces);
            if (Response.class.isAssignableFrom(methodInvocationResult.getClass())) {
                Response resultResponse = (Response)methodInvocationResult;
                if (resultResponse.getMetadata().getFirst(CONTENT_TYPE) == null && resultResponse.getEntity() != null) {
                    resultResponse.getMetadata().putSingle(CONTENT_TYPE, contentType);
                }
                response.setResponse(resultResponse);
            } else {
                response.setResponse(Response.ok(methodInvocationResult, contentType).build());
            }
        }
    }

    /**
     * Process resource methods.
     *
     * @param <T>
     *         ResourceMethodDescriptor extension
     * @param resourceMethods
     *         resource methods
     * @param request
     *         See {@link org.everrest.core.GenericContainerRequest}
     * @param response
     *         See {@link org.everrest.core.GenericContainerResponse}
     * @param matchedMethods
     *         list for matched method resources
     * @return true if at least one resource method found false otherwise
     */
    private <T extends ResourceMethodDescriptor> boolean processResourceMethod(Map<String, List<T>> resourceMethods,
                                                                               GenericContainerRequest request,
                                                                               GenericContainerResponse response,
                                                                               List<T> matchedMethods) {
        final String httpMethod = request.getMethod();
        List<T> resourceMethodsByHttpMethod = resourceMethods.get(httpMethod);
        if (resourceMethodsByHttpMethod == null || resourceMethodsByHttpMethod.size() == 0) {
            response.setResponse(Response.status(METHOD_NOT_ALLOWED)
                                         .header(ALLOW, convertToString(getAllow(resourceMethods)))
                                         .entity(String.format("%s method is not allowed", httpMethod))
                                         .type(TEXT_PLAIN)
                                         .build());
            return false;
        }

        List<T> resourceMethodCandidates = new ArrayList<>();
        MediaType contentType = request.getMediaType();
        if (contentType == null) {
            resourceMethodCandidates.addAll(resourceMethodsByHttpMethod);
        } else {
            resourceMethodCandidates.addAll(resourceMethodsByHttpMethod.stream()
                                                                       .filter(resourceMethod -> MediaTypeHelper.isConsume(resourceMethod.consumes(), contentType))
                                                                       .collect(toList()));
        }
        if (resourceMethodCandidates.isEmpty()) {
            response.setResponse(Response.status(UNSUPPORTED_MEDIA_TYPE)
                                         .entity(String.format("Media type %s is not supported", contentType))
                                         .type(TEXT_PLAIN)
                                         .build());
            return false;
        }

        List<AcceptMediaType> acceptMediaTypes = request.getAcceptMediaTypeList();
        resourceMethodCandidates = resourceMethodCandidates.stream()
                                                           .filter(notAcceptableFilter(acceptMediaTypes))
                                                           .sorted(byAcceptMediaTypeComparator(acceptMediaTypes))
                                                           .collect(toList());
        if (resourceMethodCandidates.isEmpty()) {
            response.setResponse(Response.status(NOT_ACCEPTABLE).entity("Not Acceptable").type(TEXT_PLAIN).build());
            return false;
        }

        matchedMethods.addAll(resourceMethodCandidates);
        return true;
    }

    private <T extends ResourceMethodDescriptor> Comparator<T> byAcceptMediaTypeComparator(List<AcceptMediaType> acceptMediaTypes) {
        return (resourceMethodOne, resourceMethodTwo) ->
                Float.compare(findFistCompatibleAcceptMediaType(acceptMediaTypes, resourceMethodTwo.produces()).getQvalue(),
                              findFistCompatibleAcceptMediaType(acceptMediaTypes, resourceMethodOne.produces()).getQvalue());
    }

    private <T extends ResourceMethodDescriptor> Predicate<T> notAcceptableFilter(List<AcceptMediaType> acceptMediaTypes) {
        return resourceMethod -> findFistCompatibleAcceptMediaType(acceptMediaTypes, resourceMethod.produces()) != null;
    }

    private <T extends ResourceMethodDescriptor> Collection<String> getAllow(Map<String, List<T>> resourceMethods) {
        List<String> allowed = new ArrayList<>();
        for (Map.Entry<String, List<T>> entry : resourceMethods.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }
            allowed.add(entry.getKey());
        }
        return allowed;
    }


    /**
     * Process sub-resource methods.
     *
     * @param subResourceMethods
     *         sub-resource methods
     * @param requestedPath
     *         part of requested path
     * @param request
     *         See {@link org.everrest.core.GenericContainerRequest}
     * @param response
     *         See {@link org.everrest.core.GenericContainerResponse}
     * @param capturedValues
     *         the list for keeping template values. See
     *         {@link javax.ws.rs.core.UriInfo#getPathParameters()}
     * @param matchedMethods
     *         list for method resources
     * @return true if at least one sub-resource method found false otherwise
     */
    private boolean processSubResourceMethod(Map<UriPattern, Map<String, List<SubResourceMethodDescriptor>>> subResourceMethods,
                                             String requestedPath,
                                             GenericContainerRequest request,
                                             GenericContainerResponse response,
                                             List<String> capturedValues,
                                             List<SubResourceMethodDescriptor> matchedMethods) {
        Map<String, List<SubResourceMethodDescriptor>> resourceMethods = null;
        for (Entry<UriPattern, Map<String, List<SubResourceMethodDescriptor>>> entry : subResourceMethods.entrySet()) {
            if (entry.getKey().match(requestedPath, capturedValues)) {
                String lastCapturedValue = Iterables.getLast(capturedValues);
                if (lastCapturedValue == null || "/".equals(lastCapturedValue)) {
                    resourceMethods = entry.getValue();
                    break;
                }
            }
        }

        if (resourceMethods == null) {
            response.setResponse(Response.status(NOT_FOUND)
                                         .entity(String.format("There is no any resources matched to request path %s", requestedPath))
                                         .type(TEXT_PLAIN)
                                         .build());
            return false;
        }

        return processResourceMethod(resourceMethods, request, response, matchedMethods);
    }

    /**
     * Process sub-resource locators.
     *
     * @param subResourceLocators
     *         sub-resource locators
     * @param requestedPath
     *         part of requested path
     * @param capturingValues
     *         the list for keeping template values
     * @param locators
     *         list for sub-resource locators
     * @return true if at least one SubResourceLocatorDescriptor found false otherwise
     */
    private boolean processSubResourceLocator(Map<UriPattern, SubResourceLocatorDescriptor> subResourceLocators,
                                              String requestedPath,
                                              List<String> capturingValues,
                                              List<SubResourceLocatorDescriptor> locators) {
        locators.addAll(subResourceLocators.entrySet().stream()
                                           .filter(e -> e.getKey().match(requestedPath, capturingValues))
                                           .map(e -> e.getValue())
                                           .collect(toList()));
        return !locators.isEmpty();
    }

    /**
     * Get root resource.
     *
     * @param parameterValues
     *         is taken from context
     * @param requestPath
     *         is taken from context
     * @return root resource or {@code null}
     */
    private ObjectFactory<ResourceDescriptor> getRootResource(List<String> parameterValues, String requestPath) {
        ObjectFactory<ResourceDescriptor> resourceFactory = resourceBinder.getMatchedResource(requestPath, parameterValues);
        if (resourceFactory != null) {
            if (Tracer.isTracingEnabled()) {
                ResourceDescriptor resourceDescriptor = resourceFactory.getObjectModel();
                Tracer.trace("Matched root resource for request path \"%s\" = (@Path \"%s\", %s)",
                             requestPath, resourceDescriptor.getPathValue().getPath(), resourceDescriptor.getObjectClass());
            }
        }
        return resourceFactory;
    }
}
