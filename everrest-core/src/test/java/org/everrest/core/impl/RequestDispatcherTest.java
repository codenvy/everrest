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

import org.everrest.core.ApplicationContext;
import org.everrest.core.ObjectFactory;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.async.AsynchronousJob;
import org.everrest.core.impl.header.AcceptMediaType;
import org.everrest.core.impl.uri.PathSegmentImpl;
import org.everrest.core.impl.uri.UriBuilderImpl;
import org.everrest.core.method.MethodInvoker;
import org.everrest.core.resource.GenericResourceMethod;
import org.everrest.core.resource.ResourceDescriptor;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.resource.SubResourceLocatorDescriptor;
import org.everrest.core.resource.SubResourceMethodDescriptor;
import org.everrest.core.uri.UriPattern;
import org.everrest.core.util.ResourceMethodComparator;
import org.everrest.core.util.UriPatternComparator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.HttpHeaders.ALLOW;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_XML_TYPE;
import static javax.ws.rs.core.MediaType.WILDCARD_TYPE;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.METHOD_NOT_ALLOWED;
import static javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNSUPPORTED_MEDIA_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class RequestDispatcherTest {
    @Rule public ExpectedException thrown = ExpectedException.none();

    private ResourceBinder     resources;
    private ContainerRequest   request;
    private ContainerResponse  response;
    private ApplicationContext applicationContext;
    private MethodInvoker      methodInvoker;
    private List<String>       pathParameterValues;

    private RequestDispatcher requestDispatcher;

    @Before
    public void setUp() throws Exception {
        mockContainerRequest("POST");

        response = mock(ContainerResponse.class);

        resources = mock(ResourceBinder.class);

        methodInvoker = mock(MethodInvoker.class);

        mockApplicationContext();

        requestDispatcher = new RequestDispatcher(resources);
    }

    private void mockContainerRequest(String httpMethod) {
        request = mock(ContainerRequest.class);
        when(request.getMethod()).thenReturn(httpMethod);
        when(request.getAcceptMediaTypeList()).thenReturn(newArrayList(new AcceptMediaType()));
    }

    private void mockApplicationContext() {
        applicationContext = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
        when(applicationContext.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
        when(applicationContext.getMethodInvoker(isA(GenericResourceMethod.class))).thenReturn(methodInvoker);
        pathParameterValues = newArrayList();
        when(applicationContext.getParameterValues()).thenReturn(pathParameterValues);
        when(applicationContext.getAttributes().get("org.everrest.lifecycle.PerRequest")).thenReturn(newArrayList());
        when(applicationContext.getBaseUriBuilder()).thenReturn(UriBuilderImpl.fromPath("http://localhost:8080/servlet"));
        ApplicationContext.setCurrent(applicationContext);
    }

    @Test
    public void returnsResponseWithStatus_NOT_FOUND_WhenRootResourceNotFound() throws Exception {
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b;x=y"));
        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response).setResponse(argumentCaptor.capture());
        assertEquals(NOT_FOUND, argumentCaptor.getValue().getStatusInfo());
    }

    @Test
    public void callsResourceMethod() throws Exception {
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b;x=y"));

        Resource resource = new Resource();
        ResourceMethodDescriptor resourceMethod =
                mockResourceMethod(Resource.class.getMethod("echo", String.class), "POST", newArrayList(WILDCARD_TYPE), newArrayList(WILDCARD_TYPE));
        ObjectFactory resourceFactory = mockResourceFactory(resource, newArrayList(resourceMethod), newArrayList(), newArrayList());

        matchRequestPath();
        when(resources.getMatchedResource(eq("/a/b"), anyList())).thenReturn(resourceFactory);

        when(methodInvoker.invokeMethod(same(resource), same(resourceMethod), same(applicationContext))).thenReturn("foo");

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response).setResponse(argumentCaptor.capture());
        assertEquals(OK, argumentCaptor.getValue().getStatusInfo());
        assertEquals("foo", argumentCaptor.getValue().getEntity());
    }

    @Test
    public void returnsResponseWithStatus_NO_CONTENT_WhenResourceMethodReturnsNull() throws Exception {
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b;x=y"));

        Resource resource = new Resource();
        ResourceMethodDescriptor resourceMethod =
                mockResourceMethod(Resource.class.getMethod("echo", String.class), "POST", newArrayList(WILDCARD_TYPE), newArrayList(WILDCARD_TYPE));
        ObjectFactory resourceFactory = mockResourceFactory(resource, newArrayList(resourceMethod), newArrayList(), newArrayList());

        matchRequestPath();
        when(resources.getMatchedResource(eq("/a/b"), anyList())).thenReturn(resourceFactory);

        when(methodInvoker.invokeMethod(same(resource), same(resourceMethod), same(applicationContext))).thenReturn(null);

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response).setResponse(argumentCaptor.capture());
        assertEquals(NO_CONTENT, argumentCaptor.getValue().getStatusInfo());
    }

    @Test
    public void returnsResponseWithStatus_ACCEPTED_WhenResourceMethodReturnsAsynchronousJob() throws Exception {
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b;x=y"));

        Resource resource = new Resource();
        ResourceMethodDescriptor resourceMethod =
                mockResourceMethod(Resource.class.getMethod("echo", String.class), "POST", newArrayList(WILDCARD_TYPE), newArrayList(WILDCARD_TYPE));
        ObjectFactory resourceFactory = mockResourceFactory(resource, newArrayList(resourceMethod), newArrayList(), newArrayList());

        matchRequestPath();
        when(resources.getMatchedResource(eq("/a/b"), anyList())).thenReturn(resourceFactory);

        AsynchronousJob asynchronousJob = mock(AsynchronousJob.class);
        when(asynchronousJob.getJobURI()).thenReturn("async/1");
        when(methodInvoker.invokeMethod(same(resource), same(resourceMethod), same(applicationContext))).thenReturn(asynchronousJob);

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response).setResponse(argumentCaptor.capture());
        assertEquals(ACCEPTED, argumentCaptor.getValue().getStatusInfo());
        assertEquals("http://localhost:8080/servlet/async/1", argumentCaptor.getValue().getHeaderString(LOCATION));
    }

    @Test
    public void returnsResponseProducedByResourceMethod() throws Exception {
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b;x=y"));

        Resource resource = new Resource();
        ResourceMethodDescriptor resourceMethod =
                mockResourceMethod(Resource.class.getMethod("echo", String.class), "POST", newArrayList(WILDCARD_TYPE), newArrayList(WILDCARD_TYPE));
        ObjectFactory resourceFactory = mockResourceFactory(resource, newArrayList(resourceMethod), newArrayList(), newArrayList());

        matchRequestPath();
        when(resources.getMatchedResource(eq("/a/b"), anyList())).thenReturn(resourceFactory);

        Response methodResponse = mock(Response.class);
        when(methodResponse.getMetadata()).thenReturn(new MultivaluedHashMap<>());
        when(methodInvoker.invokeMethod(same(resource), same(resourceMethod), same(applicationContext))).thenReturn(methodResponse);

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response).setResponse(argumentCaptor.capture());
        assertSame(methodResponse, argumentCaptor.getValue());
    }

    @Test
    public void callsResourceMethodRespectsConsumedMediaTypes() throws Exception {
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b;x=y"));
        when(request.getMediaType()).thenReturn(APPLICATION_XML_TYPE);

        Resource resource = new Resource();
        ResourceMethodDescriptor resourceMethodOne = mockResourceMethod(Resource.class.getMethod("echo", String.class),
                                                                        "POST",
                                                                        newArrayList(TEXT_XML_TYPE, APPLICATION_XML_TYPE),
                                                                        newArrayList(WILDCARD_TYPE));
        ResourceMethodDescriptor resourceMethodTwo = mockResourceMethod(Resource.class.getMethod("echo", String.class),
                                                                        "POST",
                                                                        newArrayList(APPLICATION_JSON_TYPE),
                                                                        newArrayList(WILDCARD_TYPE));
        ObjectFactory resourceFactory = mockResourceFactory(resource, newArrayList(resourceMethodOne, resourceMethodTwo), newArrayList(), newArrayList());

        matchRequestPath();
        when(resources.getMatchedResource(eq("/a/b"), anyList())).thenReturn(resourceFactory);

        when(methodInvoker.invokeMethod(same(resource), same(resourceMethodOne), same(applicationContext))).thenReturn("foo");
        when(methodInvoker.invokeMethod(same(resource), same(resourceMethodTwo), same(applicationContext))).thenReturn("bar");

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response).setResponse(argumentCaptor.capture());
        assertEquals(OK, argumentCaptor.getValue().getStatusInfo());
        assertEquals("foo", argumentCaptor.getValue().getEntity());
    }

    @Test
    public void returnsResponseWithStatus_NOT_FOUND_WhenResourceMethodNotFound() throws Exception {
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b;x=y"));

        Resource resource = new Resource();
        ObjectFactory resourceFactory = mockResourceFactory(resource, newArrayList(), newArrayList(), newArrayList());

        matchRequestPath();
        when(resources.getMatchedResource(eq("/a/b"), anyList())).thenReturn(resourceFactory);

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response).setResponse(argumentCaptor.capture());
        assertEquals(NOT_FOUND, argumentCaptor.getValue().getStatusInfo());
    }

    @Test
    public void returnsResponseWithStatus_METHOD_NOT_ALLOWED_WhenNotFoundResourceMethodWithRequestedHttpMethod() throws Exception {
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b;x=y"));

        Resource resource = new Resource();
        ResourceMethodDescriptor resourceMethod =
                mockResourceMethod(Resource.class.getMethod("echo", String.class), "GET", newArrayList(WILDCARD_TYPE), newArrayList(WILDCARD_TYPE));
        ObjectFactory resourceFactory = mockResourceFactory(resource, newArrayList(resourceMethod), newArrayList(), newArrayList());

        matchRequestPath();
        when(resources.getMatchedResource(eq("/a/b"), anyList())).thenReturn(resourceFactory);

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response).setResponse(argumentCaptor.capture());
        assertEquals(METHOD_NOT_ALLOWED, argumentCaptor.getValue().getStatusInfo());
        String allowHeader = argumentCaptor.getValue().getHeaderString(ALLOW);
        assertEquals("GET", allowHeader);
    }

    @Test
    public void returnsResponseWithStatus_UNSUPPORTED_MEDIA_TYPE_WhenResourceMethodDoesNotSupportContentTypeOfRequest() throws Exception {
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b;x=y"));
        when(request.getMediaType()).thenReturn(APPLICATION_JSON_TYPE);

        Resource resource = new Resource();
        ResourceMethodDescriptor resourceMethod =
                mockResourceMethod(Resource.class.getMethod("echo", String.class), "POST", newArrayList(TEXT_PLAIN_TYPE), newArrayList(WILDCARD_TYPE));
        ObjectFactory resourceFactory = mockResourceFactory(resource, newArrayList(resourceMethod), newArrayList(), newArrayList());

        matchRequestPath();
        when(resources.getMatchedResource(eq("/a/b"), anyList())).thenReturn(resourceFactory);

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response).setResponse(argumentCaptor.capture());
        assertEquals(UNSUPPORTED_MEDIA_TYPE, argumentCaptor.getValue().getStatusInfo());
    }

    @Test
    public void callsResourceMethodRespectsProducedMediaTypes() throws Exception {
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b;x=y"));
        when(request.getAcceptMediaTypeList()).thenReturn(newArrayList(new AcceptMediaType(TEXT_XML_TYPE, 0.7f),
                                                                       new AcceptMediaType(TEXT_PLAIN_TYPE)));

        Resource resource = new Resource();
        ResourceMethodDescriptor resourceMethodOne = mockResourceMethod(Resource.class.getMethod("echo", String.class),
                                                                        "POST",
                                                                        newArrayList(WILDCARD_TYPE),
                                                                        newArrayList(TEXT_XML_TYPE, APPLICATION_XML_TYPE));
        ResourceMethodDescriptor resourceMethodTwo = mockResourceMethod(Resource.class.getMethod("echo", String.class),
                                                                        "POST",
                                                                        newArrayList(WILDCARD_TYPE),
                                                                        newArrayList(TEXT_PLAIN_TYPE));
        ObjectFactory resourceFactory = mockResourceFactory(resource, newArrayList(resourceMethodOne, resourceMethodTwo), newArrayList(), newArrayList());

        matchRequestPath();
        when(resources.getMatchedResource(eq("/a/b"), anyList())).thenReturn(resourceFactory);

        when(methodInvoker.invokeMethod(same(resource), same(resourceMethodOne), same(applicationContext))).thenReturn("<bar/>");
        when(methodInvoker.invokeMethod(same(resource), same(resourceMethodTwo), same(applicationContext))).thenReturn("foo");

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response).setResponse(argumentCaptor.capture());
        assertEquals(OK, argumentCaptor.getValue().getStatusInfo());
        assertEquals("foo", argumentCaptor.getValue().getEntity());
    }

    @Test
    public void returnsResponseWithStatus_NOT_ACCEPTABLE_WhenResourceMethodDoesNotProduceContentAcceptableByCaller() throws Exception {
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b;x=y"));
        when(request.getAcceptMediaTypeList()).thenReturn(newArrayList(new AcceptMediaType(TEXT_PLAIN_TYPE)));

        Resource resource = new Resource();
        ResourceMethodDescriptor resourceMethod = mockResourceMethod(Resource.class.getMethod("echo", String.class),
                                                                     "POST",
                                                                     newArrayList(WILDCARD_TYPE),
                                                                     newArrayList(TEXT_XML_TYPE, APPLICATION_XML_TYPE));
        ObjectFactory resourceFactory = mockResourceFactory(resource, newArrayList(resourceMethod), newArrayList(), newArrayList());

        matchRequestPath();
        when(resources.getMatchedResource(eq("/a/b"), anyList())).thenReturn(resourceFactory);

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response).setResponse(argumentCaptor.capture());
        assertEquals(NOT_ACCEPTABLE, argumentCaptor.getValue().getStatusInfo());
    }

    @Test
    public void callsSubResourceMethod() throws Exception {
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b", "c", "d;x=y"));

        Resource resource = new Resource();
        SubResourceMethodDescriptor subResourceMethod =
                mockSubResourceMethod(Resource.class.getMethod("echo", String.class), "c/d", "POST", newArrayList(WILDCARD_TYPE), newArrayList(WILDCARD_TYPE));
        ObjectFactory resourceFactory = mockResourceFactory(resource, newArrayList(), newArrayList(subResourceMethod), newArrayList());

        matchRequestPath("c/d");

        when(resources.getMatchedResource(eq("/a/b/c/d"), anyList())).thenReturn(resourceFactory);
        when(methodInvoker.invokeMethod(same(resource), same(subResourceMethod), same(applicationContext))).thenReturn("foo");

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response, atLeastOnce()).setResponse(argumentCaptor.capture());
        assertEquals(OK, argumentCaptor.getValue().getStatusInfo());
        assertEquals("foo", argumentCaptor.getValue().getEntity());
    }

    @Test
    public void callsSubResourceMethodRespectsProducedMediaTypes() throws Exception {
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b", "c", "d;x=y"));
        when(request.getAcceptMediaTypeList()).thenReturn(newArrayList(new AcceptMediaType(TEXT_XML_TYPE, 0.7f),
                                                                       new AcceptMediaType(TEXT_PLAIN_TYPE)));

        Resource resource = new Resource();
        SubResourceMethodDescriptor subResourceMethodOne =
                mockSubResourceMethod(Resource.class.getMethod("echo", String.class), "c/d", "POST", newArrayList(WILDCARD_TYPE), newArrayList(TEXT_PLAIN_TYPE));
        SubResourceMethodDescriptor subResourceMethodTwo =
                mockSubResourceMethod(Resource.class.getMethod("echo", String.class), "c/d", "POST", newArrayList(WILDCARD_TYPE), newArrayList(TEXT_XML_TYPE, APPLICATION_XML_TYPE));
        ObjectFactory resourceFactory = mockResourceFactory(resource, newArrayList(), newArrayList(subResourceMethodOne, subResourceMethodTwo), newArrayList());

        matchRequestPath("c/d");
        when(resources.getMatchedResource(eq("/a/b/c/d"), anyList())).thenReturn(resourceFactory);

        when(methodInvoker.invokeMethod(same(resource), same(subResourceMethodOne), same(applicationContext))).thenReturn("foo");
        when(methodInvoker.invokeMethod(same(resource), same(subResourceMethodTwo), same(applicationContext))).thenReturn("<bar/>");

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response, atLeastOnce()).setResponse(argumentCaptor.capture());
        assertEquals(OK, argumentCaptor.getValue().getStatusInfo());
        assertEquals("foo", argumentCaptor.getValue().getEntity());
    }

    @Test
    public void returnsResponseWithStatus_METHOD_NOT_ALLOWED_WhenNotFoundSubResourceMethodWithRequestedHttpMethod() throws Exception {
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b", "c", "d;x=y"));

        Resource resource = new Resource();
        SubResourceMethodDescriptor subResourceMethod =
                mockSubResourceMethod(Resource.class.getMethod("echo", String.class), "c/d", "GET", newArrayList(WILDCARD_TYPE), newArrayList(WILDCARD_TYPE));
        ObjectFactory resourceFactory = mockResourceFactory(resource, newArrayList(), newArrayList(subResourceMethod), newArrayList());

        matchRequestPath("c/d");
        when(resources.getMatchedResource(eq("/a/b/c/d"), anyList())).thenReturn(resourceFactory);

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response).setResponse(argumentCaptor.capture());
        assertEquals(METHOD_NOT_ALLOWED, argumentCaptor.getValue().getStatusInfo());
        String allowHeader = argumentCaptor.getValue().getHeaderString(ALLOW);
        assertEquals("GET", allowHeader);
    }

    @Test
    public void returnsResponseWithStatus_UNSUPPORTED_MEDIA_TYPE_WhenSubResourceMethodDoesNotSupportContentTypeOfRequest() throws Exception {
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b", "c", "d;x=y"));
        when(request.getMediaType()).thenReturn(APPLICATION_JSON_TYPE);

        Resource resource = new Resource();
        SubResourceMethodDescriptor subResourceMethod =
                mockSubResourceMethod(Resource.class.getMethod("echo", String.class), "c/d", "POST", newArrayList(TEXT_PLAIN_TYPE), newArrayList(WILDCARD_TYPE));
        ObjectFactory resourceFactory = mockResourceFactory(resource, newArrayList(), newArrayList(subResourceMethod), newArrayList());

        matchRequestPath("c/d");
        when(resources.getMatchedResource(eq("/a/b/c/d"), anyList())).thenReturn(resourceFactory);

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response, atLeastOnce()).setResponse(argumentCaptor.capture());
        assertEquals(UNSUPPORTED_MEDIA_TYPE, argumentCaptor.getValue().getStatusInfo());
    }

    @Test
    public void returnsResponseWithStatus_NOT_ACCEPTABLE_WhenSubResourceMethodDoesNotProduceContentAcceptableByCaller() throws Exception {
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b", "c", "d;x=y"));
        when(request.getAcceptMediaTypeList()).thenReturn(newArrayList(new AcceptMediaType(TEXT_PLAIN_TYPE)));

        Resource resource = new Resource();
        SubResourceMethodDescriptor subResourceMethod = mockSubResourceMethod(Resource.class.getMethod("echo", String.class), "c/d", "POST",
                                                                              newArrayList(WILDCARD_TYPE), newArrayList(TEXT_XML_TYPE, APPLICATION_XML_TYPE));
        ObjectFactory resourceFactory = mockResourceFactory(resource, newArrayList(), newArrayList(subResourceMethod), newArrayList());

        matchRequestPath("c/d");
        when(resources.getMatchedResource(eq("/a/b/c/d"), anyList())).thenReturn(resourceFactory);

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response, atLeastOnce()).setResponse(argumentCaptor.capture());
        assertEquals(NOT_ACCEPTABLE, argumentCaptor.getValue().getStatusInfo());
    }

    @Test
    public void callsSubResourceLocator() throws Exception {
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b", "c", "d;x=y"));

        LocatorResource locatorResource = new LocatorResource();
        SubResourceLocatorDescriptor subResourceLocator = mockSubResourceLocator(LocatorResource.class.getMethod("resource"), "c/d");
        ObjectFactory resourceFactory = mockResourceFactory(locatorResource, newArrayList(), newArrayList(), newArrayList(subResourceLocator));

        matchRequestPath("c/d");
        when(resources.getMatchedResource(eq("/a/b/c/d"), anyList())).thenReturn(resourceFactory);

        SubResource subResource = new SubResource();
        when(methodInvoker.invokeMethod(same(locatorResource), same(subResourceLocator), same(applicationContext))).thenReturn(subResource);
        when(methodInvoker.invokeMethod(same(subResource), isA(ResourceMethodDescriptor.class), same(applicationContext))).thenReturn("foo");

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response, atLeastOnce()).setResponse(argumentCaptor.capture());
        assertEquals(OK, argumentCaptor.getValue().getStatusInfo());
        assertEquals("foo", argumentCaptor.getValue().getEntity());
    }

    @Test
    public void returnsResponseWithStatus_METHOD_NOT_ALLOWED_WhenSubResourceLocatorProducesResourceThatDoesNotSupportRequestedHttpMethod() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b", "c", "d;x=y"));

        LocatorResource locatorResource = new LocatorResource();
        SubResourceLocatorDescriptor subResourceLocator = mockSubResourceLocator(LocatorResource.class.getMethod("resource"), "c/d");
        ObjectFactory resourceFactory = mockResourceFactory(locatorResource, newArrayList(), newArrayList(), newArrayList(subResourceLocator));

        matchRequestPath("c/d");
        when(resources.getMatchedResource(eq("/a/b/c/d"), anyList())).thenReturn(resourceFactory);

        when(methodInvoker.invokeMethod(same(locatorResource), same(subResourceLocator), same(applicationContext))).thenReturn(new SubResource());

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response, atLeastOnce()).setResponse(argumentCaptor.capture());
        assertEquals(METHOD_NOT_ALLOWED, argumentCaptor.getValue().getStatusInfo());
        String allowHeader = argumentCaptor.getValue().getHeaderString(ALLOW);
        assertEquals("POST,OPTIONS", allowHeader);
    }

    @Test
    public void returnsResponseWithStatus_UNSUPPORTED_MEDIA_TYPE_WhenSubResourceLocatorProducesResourceThatDoesNotSupportContentTypeOfRequest() throws Exception {
        when(request.getMediaType()).thenReturn(APPLICATION_JSON_TYPE);
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b", "c", "d;x=y"));

        LocatorResource locatorResource = new LocatorResource();
        SubResourceLocatorDescriptor subResourceLocator = mockSubResourceLocator(LocatorResource.class.getMethod("resource"), "c/d");
        ObjectFactory resourceFactory = mockResourceFactory(locatorResource, newArrayList(), newArrayList(), newArrayList(subResourceLocator));

        matchRequestPath("c/d");
        when(resources.getMatchedResource(eq("/a/b/c/d"), anyList())).thenReturn(resourceFactory);

        when(methodInvoker.invokeMethod(same(locatorResource), same(subResourceLocator), same(applicationContext))).thenReturn(new SubResource());

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response, atLeastOnce()).setResponse(argumentCaptor.capture());
        assertEquals(UNSUPPORTED_MEDIA_TYPE, argumentCaptor.getValue().getStatusInfo());
    }

    @Test
    public void returnsResponseWithStatus_NOT_ACCEPTABLE_WhenSubResourceLocatorProducesResourceThatDoesNotProduceContentAcceptableByCaller() throws Exception {
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b", "c", "d;x=y"));
        when(request.getAcceptMediaTypeList()).thenReturn(newArrayList(new AcceptMediaType(TEXT_PLAIN_TYPE)));

        LocatorResource locatorResource = new LocatorResource();
        SubResourceLocatorDescriptor subResourceLocator = mockSubResourceLocator(LocatorResource.class.getMethod("resource"), "c/d");
        ObjectFactory resourceFactory = mockResourceFactory(locatorResource, newArrayList(), newArrayList(), newArrayList(subResourceLocator));

        matchRequestPath("c/d");
        when(resources.getMatchedResource(eq("/a/b/c/d"), anyList())).thenReturn(resourceFactory);

        when(methodInvoker.invokeMethod(same(locatorResource), same(subResourceLocator), same(applicationContext))).thenReturn(new SubResource());

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response, atLeastOnce()).setResponse(argumentCaptor.capture());
        assertEquals(NOT_ACCEPTABLE, argumentCaptor.getValue().getStatusInfo());
    }

    @Test
    public void subResourceMethodTakesPrecedenceOverSubResourceLocatorWithTheSamePath() throws Exception {
        when(applicationContext.getPathSegments(false)).thenReturn(createPathSegments("a", "b", "c", "d;x=y"));

        LocatorResource locatorResource = new LocatorResource();
        SubResourceLocatorDescriptor subResourceLocator =
                mockSubResourceLocator(LocatorResource.class.getMethod("resource"), "c/d");
        SubResourceMethodDescriptor subResourceMethod =
                mockSubResourceMethod(LocatorResource.class.getMethod("echo", String.class), "c/d", "POST", newArrayList(WILDCARD_TYPE), newArrayList(WILDCARD_TYPE));
        ObjectFactory resourceFactory = mockResourceFactory(locatorResource,
                                                            newArrayList(),
                                                            newArrayList(subResourceMethod),
                                                            newArrayList(subResourceLocator));

        matchRequestPath("c/d");
        when(resources.getMatchedResource(eq("/a/b/c/d"), anyList())).thenReturn(resourceFactory);

        SubResource subResource = new SubResource();
        when(methodInvoker.invokeMethod(same(locatorResource), same(subResourceMethod), same(applicationContext))).thenReturn("foo");
        when(methodInvoker.invokeMethod(same(locatorResource), same(subResourceLocator), same(applicationContext))).thenReturn(subResource);
        when(methodInvoker.invokeMethod(same(subResource), isA(ResourceMethodDescriptor.class), same(applicationContext))).thenReturn("bar");

        requestDispatcher.dispatch(request, response);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(response, atLeastOnce()).setResponse(argumentCaptor.capture());
        assertEquals(OK, argumentCaptor.getValue().getStatusInfo());
        assertEquals("foo", argumentCaptor.getValue().getEntity());
    }

    private List<PathSegment> createPathSegments(String... segments) {
        return Arrays.stream(segments).map(s -> PathSegmentImpl.fromString(s, false)).collect(toList());
    }

    private void matchRequestPath() {
        pathParameterValues.clear();
        pathParameterValues.add(null);
    }

    private void matchRequestPath(String subResourcePath) {
        pathParameterValues.clear();
        pathParameterValues.add(subResourcePath);
    }

    private ObjectFactory mockResourceFactory(Object resource,
                                              List<ResourceMethodDescriptor> resourceMethods,
                                              List<SubResourceMethodDescriptor> subResourceMethods,
                                              List<SubResourceLocatorDescriptor> subResourceLocators) throws Exception {
        ResourceDescriptor resourceDescriptor = mock(ResourceDescriptor.class, RETURNS_DEEP_STUBS);

        Map<String, List<ResourceMethodDescriptor>> resourceMethodsMap = createResourceMethodsMap(resourceMethods);
        when(resourceDescriptor.getResourceMethods()).thenReturn(resourceMethodsMap);

        Map<UriPattern, Map<String, List<SubResourceMethodDescriptor>>> subResourceMethodsMap = createSubResourceMethodsMap(subResourceMethods);
        when(resourceDescriptor.getSubResourceMethods()).thenReturn(subResourceMethodsMap);

        Map<UriPattern, SubResourceLocatorDescriptor> subResourceLocatorsMap = createSubResourceLocatorsMap(subResourceLocators);
        when(resourceDescriptor.getSubResourceLocators()).thenReturn(subResourceLocatorsMap);

        ObjectFactory objectFactory = mock(ObjectFactory.class, RETURNS_DEEP_STUBS);
        when(objectFactory.getObjectModel()).thenReturn(resourceDescriptor);
        when(objectFactory.getInstance(isA(ApplicationContext.class))).thenReturn(resource);
        return objectFactory;
    }

    private Map<String, List<ResourceMethodDescriptor>> createResourceMethodsMap(List<ResourceMethodDescriptor> resourceMethods) {
        Map<String, List<ResourceMethodDescriptor>> resourceMethodsMap = resourceMethods.stream()
                                                                               .collect(groupingBy(ResourceMethodDescriptor::getHttpMethod));

        ResourceMethodComparator resourceMethodComparator = new ResourceMethodComparator();

        resourceMethodsMap.values().forEach(_resourceMethods -> Collections.sort(_resourceMethods, resourceMethodComparator));
        return resourceMethodsMap;
    }

    private Map<UriPattern, Map<String, List<SubResourceMethodDescriptor>>> createSubResourceMethodsMap(List<SubResourceMethodDescriptor> subResourceMethods) {
        Map<UriPattern, Map<String, List<SubResourceMethodDescriptor>>> subResourceMethodsMap = new TreeMap<>(new UriPatternComparator());
        for (SubResourceMethodDescriptor subResourceMethod : subResourceMethods) {
            Map<String, List<SubResourceMethodDescriptor>> resourceMethodsMap = subResourceMethodsMap.get(subResourceMethod.getUriPattern());
            if (resourceMethodsMap == null) {
                subResourceMethodsMap.put(subResourceMethod.getUriPattern(), resourceMethodsMap = new HashMap<>());
            }
            List<SubResourceMethodDescriptor> resourceMethods = resourceMethodsMap.get(subResourceMethod.getHttpMethod());
            if (resourceMethods == null) {
                resourceMethodsMap.put(subResourceMethod.getHttpMethod(), resourceMethods = new ArrayList<>());
            }
            resourceMethods.add(subResourceMethod);
        }
        return subResourceMethodsMap;

    }

    private Map<UriPattern, SubResourceLocatorDescriptor> createSubResourceLocatorsMap(List<SubResourceLocatorDescriptor> subResourceLocators) {
        Map<UriPattern, SubResourceLocatorDescriptor> subResourceLocatorsMap = new TreeMap<>(new UriPatternComparator());
        for (SubResourceLocatorDescriptor subResourceLocator : subResourceLocators) {
            subResourceLocatorsMap.put(subResourceLocator.getUriPattern(), subResourceLocator);
        }
        return subResourceLocatorsMap;
    }

    private ResourceMethodDescriptor mockResourceMethod(Method method, String httpMethod, List<MediaType> consumes, List<MediaType> produces) {
        ResourceMethodDescriptor resourceMethod = mock(ResourceMethodDescriptor.class);
        when(resourceMethod.getMethod()).thenReturn(method);
        when(resourceMethod.getHttpMethod()).thenReturn(httpMethod);
        when(resourceMethod.consumes()).thenReturn(consumes);
        when(resourceMethod.produces()).thenReturn(produces);
        return resourceMethod;
    }

    private SubResourceMethodDescriptor mockSubResourceMethod(Method method, String path, String httpMethod, List<MediaType> consumes, List<MediaType> produces) {
        SubResourceMethodDescriptor subResourceMethod = mock(SubResourceMethodDescriptor.class);
        UriPattern uriPattern = mockUriPattern(path);
        when(subResourceMethod.getUriPattern()).thenReturn(uriPattern);
        when(subResourceMethod.getMethod()).thenReturn(method);
        when(subResourceMethod.getHttpMethod()).thenReturn(httpMethod);
        when(subResourceMethod.consumes()).thenReturn(consumes);
        when(subResourceMethod.produces()).thenReturn(produces);
        return subResourceMethod;
    }

    private SubResourceLocatorDescriptor mockSubResourceLocator(Method method, String path) {
        SubResourceLocatorDescriptor subResourceLocator = mock(SubResourceLocatorDescriptor.class);
        UriPattern uriPattern = mockUriPattern(path);
        when(subResourceLocator.getMethod()).thenReturn(method);
        when(subResourceLocator.getUriPattern()).thenReturn(uriPattern);
        when(subResourceLocator.getMethod()).thenReturn(method);
        return subResourceLocator;
    }

    private UriPattern mockUriPattern(String path) {
        UriPattern uriPattern = mock(UriPattern.class);
        when(uriPattern.getTemplate()).thenReturn(path);
        when(uriPattern.getRegex()).thenReturn(path);
        when(uriPattern.match(eq(path), anyList())).thenAnswer(invocation -> {
            matchRequestPath();
            return true;
        });
        return uriPattern;
    }

    public static class Resource {
        public String echo(String phrase) {
            return phrase;
        }
    }

    public static class LocatorResource {
        public SubResource resource() {
            return null;
        }

        public String echo(String phrase) {
            return phrase;
        }
    }

    @Path("c/d")
    public static class SubResource {
        @Consumes("text/plain")
        @Produces("text/xml")
        @POST
        public String echo(String phrase) {
            return phrase;
        }
    }
}
