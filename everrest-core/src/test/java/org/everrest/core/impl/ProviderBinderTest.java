/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.everrest.core.ApplicationContext;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.ObjectFactory;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResponseFilter;
import org.everrest.core.impl.provider.ByteEntityProvider;
import org.everrest.core.impl.provider.DefaultExceptionMapper;
import org.everrest.core.impl.provider.DuplicateProviderException;
import org.everrest.core.impl.provider.StringEntityProvider;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.provider.ProviderDescriptor;
import org.everrest.core.resource.GenericResourceMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.security.Permission;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static javax.ws.rs.core.MediaType.WILDCARD_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(DataProviderRunner.class)
public class ProviderBinderTest {
    @Provider
    @Produces("text/plain")
    public static class ContextResolverText implements ContextResolver<String> {
        public String getContext(Class<?> type) {
            return null;
        }
    }

    @Provider
    public static class ContextResolverWildcard implements ContextResolver<String> {
        public String getContext(Class<?> type) {
            return null;
        }
    }

    @Provider
    @Produces("text/xml")
    public static class ContextResolverXml implements ContextResolver<String> {
        public String getContext(Class<?> type) {
            return null;
        }
    }

    @Provider
    @Produces("text/*")
    public static class ContextResolverAnyText implements ContextResolver<String> {
        public String getContext(Class<?> type) {
            return null;
        }
    }


    @Provider
    public static class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
        @Override
        public Response toResponse(RuntimeException exception) {
            return null;
        }
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

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ProviderBinder providers;

    private static ContextResolverText     contextResolverText     = new ContextResolverText();
    private static ContextResolverWildcard contextResolverWildcard = new ContextResolverWildcard();
    private static ContextResolverXml      contextResolverXml      = new ContextResolverXml();
    private static ContextResolverAnyText  contextResolverAnyText  = new ContextResolverAnyText();

    private static RuntimeExceptionMapper runtimeExceptionMapper = new RuntimeExceptionMapper();
    private static DefaultExceptionMapper defaultExceptionMapper = new DefaultExceptionMapper();

    private static MessageBodyReader<String> stringMessageBodyReader = new StringEntityProvider();
    private static MessageBodyReader<byte[]> byteMessageBodyReader   = new ByteEntityProvider();

    private static MessageBodyWriter<String> stringMessageBodyWriter = new StringEntityProvider();
    private static MessageBodyWriter<byte[]> byteMessageBodyWriter   = new ByteEntityProvider();

    private static RequestFilter allMatchesRequestFilter  = new AllMatchesRequestFilter();
    private static RequestFilter pathMatchesRequestFilter = new PathMatchesRequestFilter();

    private static ResponseFilter allMatchesResponseFilter  = new AllMatchesResponseFilter();
    private static ResponseFilter pathMatchesResponseFilter = new PathMatchesResponseFilter();

    private static MethodInvokerFilter allMatchesMethodInvokerFilter  = new AllMatchesMethodInvokerFilter();
    private static MethodInvokerFilter pathMatchesMethodInvokerFilter = new PathMatchesMethodInvokerFilter();

    private static final boolean SINGLETON = true;
    private static final boolean PER_REQUEST = false;

    private Appender<ILoggingEvent> mockLogbackAppender;

    private ApplicationContext context;

    @Before
    public void setUp() throws Exception {
        context = mock(ApplicationContext.class);
        ApplicationContext.setCurrent(context);
        providers = new ProviderBinder();

        setUpLogbackAppender();
    }

    private void setUpLogbackAppender() {
        ch.qos.logback.classic.Logger providerBinderLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ProviderBinder.class);
        mockLogbackAppender = mockLogbackAppender();
        providerBinderLogger.addAppender(mockLogbackAppender);
    }

    private Appender mockLogbackAppender() {
        Appender mockAppender = mock(Appender.class);
        when(mockAppender.getName()).thenReturn("MockAppender");
        return mockAppender;
    }

    @After
    public void tearDown() {
        ch.qos.logback.classic.Logger providerBinderLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ProviderBinder.class);
        providerBinderLogger.detachAppender(mockLogbackAppender);

        ProviderBinder.setInstance(null);
    }

    @DataProvider
    public static Object[][] contextResolverByClassAndMediaTypeData() {
        return new Object[][] {
                {PER_REQUEST, String.class, new MediaType("text", "plain"), ContextResolverText.class},
                {PER_REQUEST, String.class, new MediaType("text", "xml"),   ContextResolverXml.class},
                {PER_REQUEST, String.class, new MediaType("text", "xxx"),   ContextResolverAnyText.class},
                {PER_REQUEST, String.class, new MediaType("xxx", "xxx"),    ContextResolverWildcard.class},
                {PER_REQUEST, Object.class, new MediaType("xxx", "xxx"),    null},
                {SINGLETON,   String.class, new MediaType("text", "plain"), contextResolverText},
                {SINGLETON,   String.class, new MediaType("text", "xml"),   contextResolverXml},
                {SINGLETON,   String.class, new MediaType("text", "xxx"),   contextResolverAnyText},
                {SINGLETON,   String.class, new MediaType("xxx", "xxx"),    contextResolverWildcard},
                {SINGLETON,   Object.class, new MediaType("xxx", "xxx"),    null}
        };
    }

    @Test
    @UseDataProvider("contextResolverByClassAndMediaTypeData")
    public void retrievesContextResolverByClassAndMediaType(boolean singletonOrPerRequest,
                                                            Class<?> aClass,
                                                            MediaType mediaType,
                                                            Object expectedContextResolverClassOrInstance) throws Exception {
        if (singletonOrPerRequest == SINGLETON) {
            registerSingletonContextResolvers();
        } else {
            registerPerRequestContextResolvers();
        }

        ContextResolver<?> contextResolver = providers.getContextResolver(aClass, mediaType);
        if (singletonOrPerRequest == SINGLETON) {
            assertSame(expectedContextResolverClassOrInstance, contextResolver);
        } else {
            if (expectedContextResolverClassOrInstance == null) {
                assertNull(contextResolver);
            } else {
                assertNotNull(contextResolver);
                assertEquals(expectedContextResolverClassOrInstance, contextResolver.getClass());
            }
        }
    }

    private void registerPerRequestContextResolvers() {
        providers.addContextResolver(ContextResolverText.class);
        providers.addContextResolver(ContextResolverWildcard.class);
        providers.addContextResolver(ContextResolverXml.class);
        providers.addContextResolver(ContextResolverAnyText.class);
    }

    private void registerSingletonContextResolvers() {
        providers.addContextResolver(contextResolverText);
        providers.addContextResolver(contextResolverWildcard);
        providers.addContextResolver(contextResolverXml);
        providers.addContextResolver(contextResolverAnyText);
    }


    @DataProvider
    public static Object[][] exceptionMapperByExceptionType() {
        return new Object[][] {
                {PER_REQUEST, Exception.class, DefaultExceptionMapper.class},
                {PER_REQUEST, RuntimeException.class, RuntimeExceptionMapper.class},
                {PER_REQUEST, IOException.class, DefaultExceptionMapper.class},
                {SINGLETON, Exception.class, defaultExceptionMapper},
                {SINGLETON, RuntimeException.class, runtimeExceptionMapper},
                {SINGLETON, IOException.class, defaultExceptionMapper}
        };
    }

    @Test
    @UseDataProvider("exceptionMapperByExceptionType")
    public <T extends Throwable> void retrievesExceptionMapperByExceptionType(boolean singletonOrPerRequest,
                                                                              Class<T> errorClass,
                                                                              Object expectedExceptionMapperClassOrInstance) throws Exception {
        if (singletonOrPerRequest == SINGLETON) {
            registerSingletonExceptionMappers();
        } else {
            registerPerRequestExceptionMappers();
        }

        ExceptionMapper<T> exceptionMapper = providers.getExceptionMapper(errorClass);
        if (singletonOrPerRequest == SINGLETON) {
            assertSame(expectedExceptionMapperClassOrInstance, exceptionMapper);
        } else {
            assertNotNull(exceptionMapper);
            assertEquals(expectedExceptionMapperClassOrInstance, exceptionMapper.getClass());
        }
    }

    private void registerPerRequestExceptionMappers() {
        providers.addExceptionMapper(DefaultExceptionMapper.class);
        providers.addExceptionMapper(RuntimeExceptionMapper.class);
    }

    private void registerSingletonExceptionMappers() {
        providers.addExceptionMapper(defaultExceptionMapper);
        providers.addExceptionMapper(runtimeExceptionMapper);
    }


    @DataProvider
    public static Object[][] messageBodyReaderByTypeAndMediaType() {
        return new Object[][] {
                {SINGLETON,   String.class, null, TEXT_PLAIN_TYPE, stringMessageBodyReader},
                {SINGLETON,   byte[].class, null, TEXT_PLAIN_TYPE, byteMessageBodyReader},
                {SINGLETON,   Object.class, null, TEXT_PLAIN_TYPE, null},
                {PER_REQUEST, String.class, null, TEXT_PLAIN_TYPE, StringEntityProvider.class},
                {PER_REQUEST, byte[].class, null, TEXT_PLAIN_TYPE, ByteEntityProvider.class},
                {PER_REQUEST, Object.class, null, TEXT_PLAIN_TYPE, null}
        };
    }

    @Test
    @UseDataProvider("messageBodyReaderByTypeAndMediaType")
    public void retrievesMessageBodyReaderByTypeAndMediaType(boolean singletonOrPerRequest,
                                                             Class<?> readObjectType,
                                                             Type readObjectGenericType,
                                                             MediaType mediaType,
                                                             Object expectedMessageBodyReaderClassOrInstance) throws Exception {
        if (singletonOrPerRequest == SINGLETON) {
            registerSingletonMessageBodyReaders();
        } else {
            registerPerRequestMessageBodyReaders();
        }

        MessageBodyReader messageBodyReader = providers.getMessageBodyReader(readObjectType, readObjectGenericType, null, mediaType);
        if (singletonOrPerRequest == SINGLETON) {
            assertSame(expectedMessageBodyReaderClassOrInstance, messageBodyReader);
        } else {
            if (expectedMessageBodyReaderClassOrInstance == null) {
                assertNull(messageBodyReader);
            } else {
                assertNotNull(messageBodyReader);
                assertEquals(expectedMessageBodyReaderClassOrInstance, messageBodyReader.getClass());
            }
        }
    }

    private void registerPerRequestMessageBodyReaders() {
        providers.addMessageBodyReader(StringEntityProvider.class);
        providers.addMessageBodyReader(ByteEntityProvider.class);
    }

    private void registerSingletonMessageBodyReaders() {
        providers.addMessageBodyReader(stringMessageBodyReader);
        providers.addMessageBodyReader(byteMessageBodyReader);
    }


    @DataProvider
    public static Object[][] messageBodyWriterByTypeAndMediaType() {
        return new Object[][] {
                {SINGLETON,   String.class, null, TEXT_PLAIN_TYPE, stringMessageBodyWriter},
                {SINGLETON,   byte[].class, null, TEXT_PLAIN_TYPE, byteMessageBodyWriter},
                {SINGLETON,   Object.class, null, TEXT_PLAIN_TYPE, null},
                {PER_REQUEST, String.class, null, TEXT_PLAIN_TYPE, StringEntityProvider.class},
                {PER_REQUEST, byte[].class, null, TEXT_PLAIN_TYPE, ByteEntityProvider.class},
                {PER_REQUEST, Object.class, null, TEXT_PLAIN_TYPE, null}
        };
    }

    @Test
    @UseDataProvider("messageBodyWriterByTypeAndMediaType")
    public void retrievesMessageBodyWriterByTypeAndMediaType(boolean singletonOrPerRequest,
                                                             Class<?> writeObjectType,
                                                             Type writeObjectGenericType,
                                                             MediaType mediaType,
                                                             Object expectedMessageBodyWriterClassOrInstance) throws Exception {
        if (singletonOrPerRequest == SINGLETON) {
            registerSingletonMessageBodyWriters();
        } else {
            registerPerRequestMessageBodyWriters();
        }

        MessageBodyWriter messageBodyWriter = providers.getMessageBodyWriter(writeObjectType, writeObjectGenericType, null, mediaType);
        if (singletonOrPerRequest == SINGLETON) {
            assertSame(expectedMessageBodyWriterClassOrInstance, messageBodyWriter);
        } else {
            if (expectedMessageBodyWriterClassOrInstance == null) {
                assertNull(messageBodyWriter);
            } else {
                assertNotNull(messageBodyWriter);
                assertEquals(expectedMessageBodyWriterClassOrInstance, messageBodyWriter.getClass());
            }
        }
    }

    private void registerPerRequestMessageBodyWriters() {
        providers.addMessageBodyWriter(StringEntityProvider.class);
        providers.addMessageBodyWriter(ByteEntityProvider.class);
    }

    private void registerSingletonMessageBodyWriters() {
        providers.addMessageBodyWriter(stringMessageBodyWriter);
        providers.addMessageBodyWriter(byteMessageBodyWriter);
    }


    @Test
    public void logsErrorWhenTryRegisterPerRequestDuplicateContextResolver() throws Exception {
        providers.addContextResolver(ContextResolverAnyText.class);

        providers.addContextResolver(ContextResolverAnyText.class);

        assertThatErrorLoggingEventWithThrowableAppended(DuplicateProviderException.class);
    }

    @Test
    public void logsErrorWhenTryRegisterSingletonDuplicateContextResolver() throws Exception {
        providers.addContextResolver(contextResolverAnyText);

        providers.addContextResolver(contextResolverAnyText);

        assertThatErrorLoggingEventWithThrowableAppended(DuplicateProviderException.class);
    }

    @Test
    public void logsErrorWhenTryRegisterDuplicateContextResolver() throws Exception {
        providers.addContextResolver(ContextResolverAnyText.class);

        providers.addContextResolver(contextResolverAnyText);

        assertThatErrorLoggingEventWithThrowableAppended(DuplicateProviderException.class);
    }

    @Test
    public void logsErrorWhenTryRegisterPerRequestDuplicateExceptionMapper() throws Exception {
        providers.addExceptionMapper(RuntimeExceptionMapper.class);

        providers.addExceptionMapper(RuntimeExceptionMapper.class);

        assertThatErrorLoggingEventWithThrowableAppended(DuplicateProviderException.class);
    }

    @Test
    public void logsErrorWhenTryRegisterSingletonDuplicateExceptionMapper() throws Exception {
        providers.addExceptionMapper(runtimeExceptionMapper);

        providers.addExceptionMapper(runtimeExceptionMapper);

        assertThatErrorLoggingEventWithThrowableAppended(DuplicateProviderException.class);
    }

    @Test
    public void logsErrorWhenTryRegisterDuplicateExceptionMapper() throws Exception {
        providers.addExceptionMapper(RuntimeExceptionMapper.class);

        providers.addExceptionMapper(runtimeExceptionMapper);

        assertThatErrorLoggingEventWithThrowableAppended(DuplicateProviderException.class);
    }

    private void assertThatErrorLoggingEventWithThrowableAppended(Class<? extends Throwable> throwable) {
        for (ILoggingEvent loggingEvent : retrieveLoggingEvents()) {
            if (loggingEvent.getLevel() == Level.ERROR
                && loggingEvent.getThrowableProxy() != null
                && loggingEvent.getThrowableProxy().getClassName().equals(throwable.getName())) {
                return;
            }
        }
        fail(String.format("Error event with error type %s was not logged", throwable.getName()));
    }

    private List<ILoggingEvent> retrieveLoggingEvents() {
        ArgumentCaptor<ILoggingEvent> logEventCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(mockLogbackAppender, atLeastOnce()).doAppend(logEventCaptor.capture());
        return logEventCaptor.getAllValues();
    }


    @DataProvider
    public static Object[][] requestFiltersByPath() {
        return new Object[][] {
                {SINGLETON,   "/a/b", newArrayList(allMatchesRequestFilter, pathMatchesRequestFilter)},
                {SINGLETON,   "/a",   newArrayList(allMatchesRequestFilter)},
                {SINGLETON,   "/",    newArrayList(allMatchesRequestFilter)},
                {PER_REQUEST, "/a/b", newArrayList(AllMatchesRequestFilter.class, PathMatchesRequestFilter.class)},
                {PER_REQUEST, "/a",   newArrayList(AllMatchesRequestFilter.class)},
                {PER_REQUEST, "/",    newArrayList(AllMatchesRequestFilter.class)}
        };
    }

    @Test
    @UseDataProvider("requestFiltersByPath")
    public void retrievesRequestFiltersByPath(boolean singletonOrPerRequest,
                                              String path,
                                              List<Object> expectedRequestFilterClassesOrInstances) throws Exception {
        if (singletonOrPerRequest == SINGLETON) {
            registerSingletonRequestFilters();
        } else {
            registerPerRequestRequestFilters();
        }

        List<RequestFilter> requestFilters = providers.getRequestFilters(path);

        if (singletonOrPerRequest == SINGLETON) {
            expectedRequestFilterClassesOrInstances.removeAll(requestFilters);
            assertTrue(String.format("Request filters %s expected but not found", expectedRequestFilterClassesOrInstances),
                       expectedRequestFilterClassesOrInstances.isEmpty());
        } else {
            List<Class> methodInvokerFiltersClasses = requestFilters.stream().map(Object::getClass).collect(toList());
            expectedRequestFilterClassesOrInstances.removeAll(methodInvokerFiltersClasses);
            assertTrue(String.format("Request filters %s expected but not found", expectedRequestFilterClassesOrInstances),
                       expectedRequestFilterClassesOrInstances.isEmpty());
        }
    }

    private void registerPerRequestRequestFilters() {
        providers.addRequestFilter(PathMatchesRequestFilter.class);
        providers.addRequestFilter(AllMatchesRequestFilter.class);
    }

    private void registerSingletonRequestFilters() {
        providers.addRequestFilter(pathMatchesRequestFilter);
        providers.addRequestFilter(allMatchesRequestFilter);
    }


    @DataProvider
    public static Object[][] responseFiltersByPath() {
        return new Object[][] {
                {SINGLETON,   "/a/b", newArrayList(allMatchesResponseFilter, pathMatchesResponseFilter)},
                {SINGLETON,   "/a",   newArrayList(allMatchesResponseFilter)},
                {SINGLETON,   "/",    newArrayList(allMatchesResponseFilter)},
                {PER_REQUEST, "/a/b", newArrayList(AllMatchesResponseFilter.class, PathMatchesResponseFilter.class)},
                {PER_REQUEST, "/a",   newArrayList(AllMatchesResponseFilter.class)},
                {PER_REQUEST, "/",    newArrayList(AllMatchesResponseFilter.class)},
        };
    }

    @Test
    @UseDataProvider("responseFiltersByPath")
    public void retrievesResponseFiltersByPath(boolean singletonOrPerRequest,
                                               String path,
                                               List<Object> expectedResponseFilterClassesOrInstances) throws Exception {
        if (singletonOrPerRequest == SINGLETON) {
            registerSingletonResponseFilters();
        } else {
            registerPerRequestResponseFilters();
        }

        List<ResponseFilter> responseFilters = providers.getResponseFilters(path);

        if (singletonOrPerRequest == SINGLETON) {
            expectedResponseFilterClassesOrInstances.removeAll(responseFilters);
            assertTrue(String.format("Response filters %s expected but not found", expectedResponseFilterClassesOrInstances),
                       expectedResponseFilterClassesOrInstances.isEmpty());
        } else {
            List<Class> methodInvokerFiltersClasses = responseFilters.stream().map(Object::getClass).collect(toList());
            expectedResponseFilterClassesOrInstances.removeAll(methodInvokerFiltersClasses);
            assertTrue(String.format("Response filters %s expected but not found", expectedResponseFilterClassesOrInstances),
                       expectedResponseFilterClassesOrInstances.isEmpty());
        }
    }

    private void registerPerRequestResponseFilters() {
        providers.addResponseFilter(PathMatchesResponseFilter.class);
        providers.addResponseFilter(AllMatchesResponseFilter.class);
    }

    private void registerSingletonResponseFilters() {
        providers.addResponseFilter(pathMatchesResponseFilter);
        providers.addResponseFilter(allMatchesResponseFilter);
    }


    @DataProvider
    public static Object[][] methodInvokerFiltersByPath() {
        return new Object[][] {
                {SINGLETON,   "/a/b", newArrayList(allMatchesMethodInvokerFilter, pathMatchesMethodInvokerFilter)},
                {SINGLETON,   "/a",   newArrayList(allMatchesMethodInvokerFilter)},
                {SINGLETON,   "/",    newArrayList(allMatchesMethodInvokerFilter)},
                {PER_REQUEST, "/a/b", newArrayList(AllMatchesMethodInvokerFilter.class, PathMatchesMethodInvokerFilter.class)},
                {PER_REQUEST, "/a",   newArrayList(AllMatchesMethodInvokerFilter.class)},
                {PER_REQUEST, "/",    newArrayList(AllMatchesMethodInvokerFilter.class)},
        };
    }

    @Test
    @UseDataProvider("methodInvokerFiltersByPath")
    public void retrievesMethodInvokerFiltersByPath(boolean singletonOrPerRequest,
                                                    String path,
                                                    List<Object> expectedMethodInvokerFilterClassesOrInstances) throws Exception {
        if (singletonOrPerRequest == SINGLETON) {
            registerSingletonMethodInvokerFilters();
        } else {
            registerPerRequestMethodInvokerFilters();
        }

        List<MethodInvokerFilter> methodInvokerFilters = providers.getMethodInvokerFilters(path);

        if (singletonOrPerRequest == SINGLETON) {
            expectedMethodInvokerFilterClassesOrInstances.removeAll(methodInvokerFilters);
            assertTrue(String.format("MethodInvoker filters %s expected but not found", expectedMethodInvokerFilterClassesOrInstances),
                       expectedMethodInvokerFilterClassesOrInstances.isEmpty());
        } else {
            List<Class> methodInvokerFiltersClasses = methodInvokerFilters.stream().map(Object::getClass).collect(toList());
            expectedMethodInvokerFilterClassesOrInstances.removeAll(methodInvokerFiltersClasses);
            assertTrue(String.format("MethodInvoker filters %s expected but not found", expectedMethodInvokerFilterClassesOrInstances),
                       expectedMethodInvokerFilterClassesOrInstances.isEmpty());
        }
    }

    private void registerPerRequestMethodInvokerFilters() {
        providers.addMethodInvokerFilter(PathMatchesMethodInvokerFilter.class);
        providers.addMethodInvokerFilter(AllMatchesMethodInvokerFilter.class);
    }

    private void registerSingletonMethodInvokerFilters() {
        providers.addMethodInvokerFilter(pathMatchesMethodInvokerFilter);
        providers.addMethodInvokerFilter(allMatchesMethodInvokerFilter);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void retrievesAcceptableWriterMediaTypes() throws Exception {
        ObjectFactory<ProviderDescriptor> writerFactory = mock(ObjectFactory.class);
        ProviderDescriptor providerDescriptor = mock(ProviderDescriptor.class);
        when(providerDescriptor.produces()).thenReturn(newArrayList(new MediaType("text", "*"), new MediaType("text", "plain")));
        when(providerDescriptor.getObjectClass()).thenReturn((Class)StringEntityProvider.class);
        when(writerFactory.getObjectModel()).thenReturn(providerDescriptor);
        MessageBodyWriter<String> writer = mock(MessageBodyWriter.class);
        when(writer.isWriteable(eq(String.class), isNull(), any(), eq(WILDCARD_TYPE))).thenReturn(true);
        when(writerFactory.getInstance(context)).thenReturn(writer);

        providers.addMessageBodyWriter(writerFactory);

        assertEquals(newArrayList(new MediaType("text", "plain"), new MediaType("text", "*")),
                     providers.getAcceptableWriterMediaTypes(String.class, null, null));
    }

    @Test
    public void setsInstanceOfProviderBinder() throws Exception {
        ProviderBinder providerBinder = mock(ProviderBinder.class);
        ProviderBinder.setInstance(providerBinder);
        assertSame(providerBinder, ProviderBinder.getInstance());
    }

    @Test
    public void failsSetInstanceOfProviderBinderWhenDoNotHaveProvidersManagePermission() throws Exception {
        try {
            setupSecurityManager();
            ProviderBinder providerBinder = mock(ProviderBinder.class);

            thrown.expect(SecurityException.class);
            ProviderBinder.setInstance(providerBinder);
        } finally {
            restoreSecurityManager();
        }
    }

    @Test
    public void findsMessageBodyReaderThatSupportsTypeNearestToDeserializedTypeInClassesHierarchy() {
        EntityReader entityReader = new EntityReader();
        ExtendedEntityReader extendedEntityReader = new ExtendedEntityReader();
        ExtendedExtendedEntityReader extendedExtendedEntityReader = new ExtendedExtendedEntityReader();

        providers.addMessageBodyReader(entityReader);
        providers.addMessageBodyReader(extendedEntityReader);

        assertSame(entityReader, providers.getMessageBodyReader(Entity.class, null, null, TEXT_PLAIN_TYPE));
        assertSame(extendedEntityReader, providers.getMessageBodyReader(ExtendedEntity.class, null, null, TEXT_PLAIN_TYPE));
        assertSame(extendedEntityReader, providers.getMessageBodyReader(ExtendedExtendedEntity.class, null, null, TEXT_PLAIN_TYPE));

        providers.addMessageBodyReader(extendedExtendedEntityReader);

        assertSame(entityReader, providers.getMessageBodyReader(Entity.class, null, null, TEXT_PLAIN_TYPE));
        assertSame(extendedEntityReader, providers.getMessageBodyReader(ExtendedEntity.class, null, null, TEXT_PLAIN_TYPE));
        assertSame(extendedExtendedEntityReader, providers.getMessageBodyReader(ExtendedExtendedEntity.class, null, null, TEXT_PLAIN_TYPE));
    }

    @Test
    public void findsMessageBodyWriterThatSupportsTypeNearestToSerializedTypeInClassesHierarchy() {
        EntityWriter entityWriter = new EntityWriter();
        ExtendedEntityWriter extendedEntityWriter = new ExtendedEntityWriter();
        ExtendedExtendedEntityWriter extendedExtendedEntityWriter = new ExtendedExtendedEntityWriter();

        providers.addMessageBodyWriter(entityWriter);
        providers.addMessageBodyWriter(extendedEntityWriter);

        assertSame(entityWriter, providers.getMessageBodyWriter(Entity.class, null, null, TEXT_PLAIN_TYPE));
        assertSame(extendedEntityWriter, providers.getMessageBodyWriter(ExtendedEntity.class, null, null, TEXT_PLAIN_TYPE));
        assertSame(extendedEntityWriter, providers.getMessageBodyWriter(ExtendedExtendedEntity.class, null, null, TEXT_PLAIN_TYPE));

        providers.addMessageBodyWriter(extendedExtendedEntityWriter);

        assertSame(entityWriter, providers.getMessageBodyWriter(Entity.class, null, null, TEXT_PLAIN_TYPE));
        assertSame(extendedEntityWriter, providers.getMessageBodyWriter(ExtendedEntity.class, null, null, TEXT_PLAIN_TYPE));
        assertSame(extendedExtendedEntityWriter, providers.getMessageBodyWriter(ExtendedExtendedEntity.class, null, null, TEXT_PLAIN_TYPE));
    }

    static class Entity {
    }

    static class ExtendedEntity extends Entity {
    }

    static class ExtendedExtendedEntity extends ExtendedEntity {
    }

    @Provider static class EntityReader implements MessageBodyReader<Entity> {
        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return Entity.class.isAssignableFrom(type);
        }

        @Override
        public Entity readFrom(Class<Entity> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                          MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
            return null;
        }
    }

    @Provider static class ExtendedEntityReader implements MessageBodyReader<ExtendedEntity> {
        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return Entity.class.isAssignableFrom(type);
        }

        @Override
        public ExtendedEntity readFrom(Class<ExtendedEntity> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
            return null;
        }
    }

    @Provider static class ExtendedExtendedEntityReader implements MessageBodyReader<ExtendedExtendedEntity> {
        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return Entity.class.isAssignableFrom(type);
        }

        @Override
        public ExtendedExtendedEntity readFrom(Class<ExtendedExtendedEntity> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                                       MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
            return null;
        }
    }

    @Provider static class EntityWriter implements MessageBodyWriter<Entity> {
        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return Entity.class.isAssignableFrom(type);
        }

        @Override
        public long getSize(Entity entity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(Entity entity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        }
    }

    @Provider static class ExtendedEntityWriter implements MessageBodyWriter<ExtendedEntity> {
        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return Entity.class.isAssignableFrom(type);
        }

        @Override
        public long getSize(ExtendedEntity extendedEntity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(ExtendedEntity extendedEntity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        }
    }

    @Provider static class ExtendedExtendedEntityWriter implements MessageBodyWriter<ExtendedExtendedEntity> {
        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return Entity.class.isAssignableFrom(type);
        }

        @Override
        public long getSize(ExtendedExtendedEntity extendedExtendedEntity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(ExtendedExtendedEntity extendedExtendedEntity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        }
    }

    private SecurityManager defaultSecurityManager;

    private void setupSecurityManager() {
        defaultSecurityManager = System.getSecurityManager();
        final RuntimePermission providersManagePermission = new RuntimePermission("providersManagePermission");
        SecurityManager securityManager = new SecurityManager() {
            @Override
            public void checkPermission(Permission perm) {
                if (perm.equals(providersManagePermission)) {
                    throw new SecurityException();
                }
            }
        };
        System.setSecurityManager(securityManager);
    }

    private void restoreSecurityManager() {
        System.setSecurityManager(defaultSecurityManager);
    }

    @Test
    public void initializesProviderBinderOnFirstCallAndReusesSameInstance() throws Exception {
        ProviderBinder providerBinder = ProviderBinder.getInstance();

        assertSame(providerBinder, ProviderBinder.getInstance());
    }
}