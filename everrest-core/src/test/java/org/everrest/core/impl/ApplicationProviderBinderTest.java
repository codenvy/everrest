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
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.ObjectFactory;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResponseFilter;
import org.everrest.core.impl.provider.StringEntityProvider;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.provider.ProviderDescriptor;
import org.everrest.core.resource.GenericResourceMethod;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static javax.ws.rs.core.MediaType.WILDCARD_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class ApplicationProviderBinderTest {
    private ApplicationContext context;
    private ProviderBinder     embeddedProviders;

    private ApplicationProviderBinder applicationProviders;

    @Before
    public void setUp() throws Exception {
        context = mock(ApplicationContext.class);
        ApplicationContext.setCurrent(context);

        embeddedProviders = mock(ProviderBinder.class);
        ProviderBinder.setInstance(embeddedProviders);

        applicationProviders = new ApplicationProviderBinder();
    }

    @Test
    public void retrievesAcceptableWriterMediaTypesFromApplicationProviderBinderAndEmbeddedProviderBinder() throws Exception {
        when(embeddedProviders.getAcceptableWriterMediaTypes(String.class, null, null)).thenReturn(newArrayList(new MediaType("text", "xml"), new MediaType("application", "xml")));
        ObjectFactory<ProviderDescriptor> applicationWriterFactory = mockStringWriterFactory(newArrayList(new MediaType("text", "*"), new MediaType("text", "plain")));
        applicationProviders.addMessageBodyWriter(applicationWriterFactory);

        assertEquals(newArrayList(new MediaType("text", "plain"), new MediaType("text", "*"), new MediaType("text", "xml"), new MediaType("application", "xml")),
                     applicationProviders.getAcceptableWriterMediaTypes(String.class, null, null));
    }

    private ObjectFactory<ProviderDescriptor> mockStringWriterFactory(List<MediaType> supportedMediaTypes) {
        ObjectFactory<ProviderDescriptor> writerFactory = mock(ObjectFactory.class);
        ProviderDescriptor providerDescriptor = mockStringWriterProviderDescriptor(supportedMediaTypes);
        when(writerFactory.getObjectModel()).thenReturn(providerDescriptor);
        MessageBodyWriter<String> writer = mockStringMessageBodyWriter();
        when(writerFactory.getInstance(context)).thenReturn(writer);
        return writerFactory;
    }

    private ProviderDescriptor mockStringWriterProviderDescriptor(List<MediaType> supportedMediaTypes) {
        ProviderDescriptor providerDescriptor = mock(ProviderDescriptor.class);
        when(providerDescriptor.produces()).thenReturn(supportedMediaTypes);
        when(providerDescriptor.getObjectClass()).thenReturn((Class)StringEntityProvider.class);
        return providerDescriptor;
    }

    private MessageBodyWriter<String> mockStringMessageBodyWriter() {
        MessageBodyWriter<String> writer = mock(MessageBodyWriter.class);
        when(writer.isWriteable(eq(String.class), (Type)isNull(), any(Annotation[].class), eq(WILDCARD_TYPE))).thenReturn(true);
        return writer;
    }

    @Test
    public void retrievesContextResolverByClassAndMediaType() {
        ContextResolverText stringContextResolver = new ContextResolverText();
        applicationProviders.addContextResolver(stringContextResolver);
        assertSame(stringContextResolver, applicationProviders.getContextResolver(String.class, TEXT_PLAIN_TYPE));
    }

    @Test
    public void retrievesContextResolverFromEmbeddedProviderBinderByClassAndMediaTypeWhenRequiredContextResolverIsNotAvailableInApplicationProviderBinder() {
        ContextResolver<String> contextResolver = mock(ContextResolver.class);
        when(embeddedProviders.getContextResolver(String.class, TEXT_PLAIN_TYPE)).thenReturn(contextResolver);
        assertSame(contextResolver, applicationProviders.getContextResolver(String.class, TEXT_PLAIN_TYPE));
    }

    @Provider
    @Produces("text/plain")
    public static class ContextResolverText implements ContextResolver<String> {
        public String getContext(Class<?> type) {
            return null;
        }
    }

    @Test
    public void retrievesExceptionMapperByExceptionType() {
        ExceptionMapper<RuntimeException> exceptionMapper = new RuntimeExceptionMapper();
        applicationProviders.addExceptionMapper(exceptionMapper);
        assertSame(exceptionMapper, applicationProviders.getExceptionMapper(RuntimeException.class));
    }

    @Test
    public void retrievesExceptionMapperFromEmbeddedProviderBinderByExceptionTypeWhenRequiredExceptionMapperIsNotAvailableInApplicationProviderBinder() {
        ExceptionMapper<RuntimeException> exceptionMapper = mock(ExceptionMapper.class);
        when(embeddedProviders.getExceptionMapper(RuntimeException.class)).thenReturn(exceptionMapper);
        assertSame(exceptionMapper, applicationProviders.getExceptionMapper(RuntimeException.class));
    }

    @Provider
    public static class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
        @Override
        public Response toResponse(RuntimeException exception) {
            return null;
        }
    }

    @Test
    public void retrievesMessageBodyReaderByTypeAndMediaType() {
        MessageBodyReader<String> stringMessageBodyReader = new StringEntityProvider();
        applicationProviders.addMessageBodyReader(stringMessageBodyReader);
        assertSame(stringMessageBodyReader, applicationProviders.getMessageBodyReader(String.class, null, null, TEXT_PLAIN_TYPE));
    }

    @Test
    public void retrievesMessageBodyReaderFromEmbeddedProviderBinderByTypeAndMediaTypeWhenRequiredMessageBodyReaderIsNotAvailableInApplicationProviderBinder() {
        MessageBodyReader<String> stringMessageBodyReader = mock(MessageBodyReader.class);
        when(applicationProviders.getMessageBodyReader(String.class, null, null, TEXT_PLAIN_TYPE)).thenReturn(stringMessageBodyReader);
        assertSame(stringMessageBodyReader, applicationProviders.getMessageBodyReader(String.class, null, null, TEXT_PLAIN_TYPE));
    }

    @Test
    public void retrievesMessageBodyWriterByTypeAndMediaType() {
        MessageBodyWriter<String> stringMessageBodyWriter = new StringEntityProvider();
        applicationProviders.addMessageBodyWriter(stringMessageBodyWriter);
        assertSame(stringMessageBodyWriter, applicationProviders.getMessageBodyWriter(String.class, null, null, TEXT_PLAIN_TYPE));
    }

    @Test
    public void retrievesMessageBodyWriterFromEmbeddedProviderBinderByTypeAndMediaTypeWhenRequiredMessageBodyWriterIsNotAvailableInApplicationProviderBinder() {
        MessageBodyWriter<String> stringMessageBodyWriter = mock(MessageBodyWriter.class);
        when(applicationProviders.getMessageBodyWriter(String.class, null, null, TEXT_PLAIN_TYPE)).thenReturn(stringMessageBodyWriter);
        assertSame(stringMessageBodyWriter, applicationProviders.getMessageBodyWriter(String.class, null, null, TEXT_PLAIN_TYPE));
    }

    @Test
    public void retrievesMethodInvokerFiltersByPath() {
        MethodInvokerFilter embeddedMethodInvokerFilter = mock(MethodInvokerFilter.class);
        when(embeddedProviders.getMethodInvokerFilters("/a")).thenReturn(newArrayList(embeddedMethodInvokerFilter));
        PathMatchesMethodInvokerFilter pathMatchesMethodInvokerFilter = new PathMatchesMethodInvokerFilter();
        AllMatchesMethodInvokerFilter allMatchesMethodInvokerFilter = new AllMatchesMethodInvokerFilter();
        applicationProviders.addMethodInvokerFilter(pathMatchesMethodInvokerFilter);
        applicationProviders.addMethodInvokerFilter(allMatchesMethodInvokerFilter);

        assertEquals(newArrayList(allMatchesMethodInvokerFilter, embeddedMethodInvokerFilter), applicationProviders.getMethodInvokerFilters("/a"));
    }

    @Filter
    public static class AllMatchesMethodInvokerFilter implements MethodInvokerFilter {
        @Override
        public void accept(GenericResourceMethod genericResourceMethod, Object[] params) {
        }
    }

    @Filter
    @Path("/a/b")
    public static class PathMatchesMethodInvokerFilter implements MethodInvokerFilter {
        @Override
        public void accept(GenericResourceMethod genericResourceMethod, Object[] params) {
        }
    }

    @Test
    public void retrievesRequestFiltersByPath() {
        RequestFilter embeddedRequestFilter = mock(RequestFilter.class);
        when(embeddedProviders.getRequestFilters("/a")).thenReturn(newArrayList(embeddedRequestFilter));
        PathMatchesRequestFilter pathMatchesRequestFilter = new PathMatchesRequestFilter();
        AllMatchesRequestFilter allMatchesRequestFilter = new AllMatchesRequestFilter();
        applicationProviders.addRequestFilter(pathMatchesRequestFilter);
        applicationProviders.addRequestFilter(allMatchesRequestFilter);

        assertEquals(newArrayList(allMatchesRequestFilter, embeddedRequestFilter), applicationProviders.getRequestFilters("/a"));
    }

    @Filter
    public static class AllMatchesRequestFilter implements RequestFilter {
        @Override
        public void doFilter(GenericContainerRequest request) {
        }
    }

    @Filter
    @Path("/a/b")
    public static class PathMatchesRequestFilter implements RequestFilter {
        @Override
        public void doFilter(GenericContainerRequest request) {
        }
    }

    @Test
    public void retrievesResponseFiltersByPath() {
        ResponseFilter embeddedResponseFilter = mock(ResponseFilter.class);
        when(embeddedProviders.getResponseFilters("/a")).thenReturn(newArrayList(embeddedResponseFilter));
        PathMatchesResponseFilter pathMatchesResponseFilter = new PathMatchesResponseFilter();
        AllMatchesResponseFilter allMatchesResponseFilter = new AllMatchesResponseFilter();
        applicationProviders.addResponseFilter(pathMatchesResponseFilter);
        applicationProviders.addResponseFilter(allMatchesResponseFilter);

        assertEquals(newArrayList(allMatchesResponseFilter, embeddedResponseFilter), applicationProviders.getResponseFilters("/a"));
    }

    @Filter
    public static class AllMatchesResponseFilter implements ResponseFilter {
        @Override
        public void doFilter(GenericContainerResponse response) {
        }
    }

    @Filter
    @Path("/a/b")
    public static class PathMatchesResponseFilter implements ResponseFilter {
        @Override
        public void doFilter(GenericContainerResponse response) {
        }
    }
}