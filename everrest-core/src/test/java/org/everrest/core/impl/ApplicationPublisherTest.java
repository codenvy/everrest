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

import com.google.common.collect.ImmutableMap;

import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.ObjectFactory;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResourceBinder;
import org.everrest.core.ResponseFilter;
import org.everrest.core.impl.provider.StringEntityProvider;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.resource.GenericResourceMethod;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationPublisherTest {
    private ResourceBinder resources;
    private ProviderBinder providers;

    private ApplicationPublisher publisher;

    @Before
    public void setUp() throws Exception {
        resources = mock(ResourceBinder.class);
        providers = mock(ProviderBinder.class);

        publisher = new ApplicationPublisher(resources, providers);
    }

    @Test
    public void publishesPerRequestResource() {
        Application application = mock(Application.class);
        when(application.getClasses()).thenReturn(newHashSet(Resource.class));

        publisher.publish(application);

        verify(resources).addResource(Resource.class, null);
    }

    @Test
    public void publishesSingletonResource() {
        Resource resource = new Resource();
        Application application = mock(Application.class);
        when(application.getSingletons()).thenReturn(newHashSet(resource));

        publisher.publish(application);

        verify(resources).addResource(resource, null);
    }

    @Path("a")
    public static class Resource {
    }

    @Test
    public void publishesPerRequestExceptionMapper() {
        Application application = mock(Application.class);
        when(application.getClasses()).thenReturn(newHashSet(RuntimeExceptionMapper.class));

        publisher.publish(application);

        verify(providers).addExceptionMapper(RuntimeExceptionMapper.class);
    }

    @Test
    public void publishesSingletonExceptionMapper() {
        ExceptionMapper exceptionMapper = new RuntimeExceptionMapper();
        Application application = mock(Application.class);
        when(application.getSingletons()).thenReturn(newHashSet(exceptionMapper));

        publisher.publish(application);

        verify(providers).addExceptionMapper(exceptionMapper);
    }

    @Provider
    public static class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
        @Override
        public Response toResponse(RuntimeException exception) {
            return null;
        }
    }

    @Test
    public void publishesPerRequestContextResolver() {
        Application application = mock(Application.class);
        when(application.getClasses()).thenReturn(newHashSet(ContextResolverText.class));

        publisher.publish(application);

        verify(providers).addContextResolver(ContextResolverText.class);
    }

    @Test
    public void publishesSingletonContextResolver() {
        ContextResolver contextResolver = new ContextResolverText();
        Application application = mock(Application.class);
        when(application.getSingletons()).thenReturn(newHashSet(contextResolver));

        publisher.publish(application);

        verify(providers).addContextResolver(contextResolver);
    }

    @Provider
    @Produces("text/plain")
    public static class ContextResolverText implements ContextResolver<String> {
        public String getContext(Class<?> type) {
            return null;
        }
    }

    @Test
    public void publishesPerRequestMessageBodyReader() {
        Application application = mock(Application.class);
        when(application.getClasses()).thenReturn(newHashSet(StringEntityProvider.class));

        publisher.publish(application);

        verify(providers).addMessageBodyReader(StringEntityProvider.class);
    }

    @Test
    public void publishesSingletonMessageBodyReader() {
        MessageBodyReader<String> messageBodyReader = new StringEntityProvider();
        Application application = mock(Application.class);
        when(application.getSingletons()).thenReturn(newHashSet(messageBodyReader));

        publisher.publish(application);

        verify(providers).addMessageBodyReader(messageBodyReader);
    }

    @Test
    public void publishesPerRequestMessageBodyWriter() {
        Application application = mock(Application.class);
        when(application.getClasses()).thenReturn(newHashSet(StringEntityProvider.class));

        publisher.publish(application);

        verify(providers).addMessageBodyWriter(StringEntityProvider.class);
    }

    @Test
    public void publishesSingletonMessageBodyWriter() {
        MessageBodyWriter<String> messageBodyWriter = new StringEntityProvider();
        Application application = mock(Application.class);
        when(application.getSingletons()).thenReturn(newHashSet(messageBodyWriter));

        publisher.publish(application);

        verify(providers).addMessageBodyWriter(messageBodyWriter);
    }

    @Test
    public void publishesPerRequestMethodInvokerFilter() {
        Application application = mock(Application.class);
        when(application.getClasses()).thenReturn(newHashSet(AllMatchesMethodInvokerFilter.class));

        publisher.publish(application);

        verify(providers).addMethodInvokerFilter(AllMatchesMethodInvokerFilter.class);
    }

    @Test
    public void publishesSingletonMethodInvokerFilter() {
        MethodInvokerFilter methodInvokerFilter = new AllMatchesMethodInvokerFilter();
        Application application = mock(Application.class);
        when(application.getSingletons()).thenReturn(newHashSet(methodInvokerFilter));

        publisher.publish(application);

        verify(providers).addMethodInvokerFilter(methodInvokerFilter);
    }

    @Filter
    public static class AllMatchesMethodInvokerFilter implements MethodInvokerFilter {
        @Override
        public void accept(GenericResourceMethod genericResourceMethod, Object[] params) {
        }
    }

    @Test
    public void publishesPerRequestRequestFilter() {
        Application application = mock(Application.class);
        when(application.getClasses()).thenReturn(newHashSet(AllMatchesRequestFilter.class));

        publisher.publish(application);

        verify(providers).addRequestFilter(AllMatchesRequestFilter.class);
    }

    @Test
    public void publishesSingletonRequestFilter() {
        RequestFilter requestFilter = new AllMatchesRequestFilter();
        Application application = mock(Application.class);
        when(application.getSingletons()).thenReturn(newHashSet(requestFilter));

        publisher.publish(application);

        verify(providers).addRequestFilter(requestFilter);
    }

    @Filter
    public static class AllMatchesRequestFilter implements RequestFilter {
        @Override
        public void doFilter(GenericContainerRequest request) {
        }
    }

    @Test
    public void publishesPerResponseResponseFilter() {
        Application application = mock(Application.class);
        when(application.getClasses()).thenReturn(newHashSet(AllMatchesResponseFilter.class));

        publisher.publish(application);

        verify(providers).addResponseFilter(AllMatchesResponseFilter.class);
    }

    @Test
    public void publishesSingletonResponseFilter() {
        ResponseFilter responseFilter = new AllMatchesResponseFilter();
        Application application = mock(Application.class);
        when(application.getSingletons()).thenReturn(newHashSet(responseFilter));

        publisher.publish(application);

        verify(providers).addResponseFilter(responseFilter);
    }

    @Filter
    public static class AllMatchesResponseFilter implements ResponseFilter {
        @Override
        public void doFilter(GenericContainerResponse response) {
        }
    }

    @Test
    public void publishesPerRequestResourceWithNewPathThroughEverrestApplication() {
        EverrestApplication application = mock(EverrestApplication.class);
        when(application.getClasses()).thenReturn(newHashSet(Resource.class));
        when(application.getResourceClasses()).thenReturn(ImmutableMap.of("/x", Resource.class));

        publisher.publish(application);

        verify(resources, never()).addResource(Resource.class, null);
        verify(resources).addResource("/x", Resource.class, null);
    }

    @Test
    public void publishesSingletonResourceWithSpecifiedPathThroughEverrestApplication() {
        Resource resource = new Resource();
        EverrestApplication application = mock(EverrestApplication.class);
        when(application.getSingletons()).thenReturn(newHashSet(resource));
        when(application.getResourceSingletons()).thenReturn(ImmutableMap.of("/x", resource));

        publisher.publish(application);

        verify(resources).addResource(resource, null);
        verify(resources).addResource("/x", resource, null);
    }

    @Test
    public void publishesResourceWithFactoryAndOverridesPerRequestResourceThroughEverrestApplication() {
        EverrestApplication application = mock(EverrestApplication.class);
        ObjectFactory resourceFactory = mockObjectFactory(Resource.class);
        when(application.getClasses()).thenReturn(newHashSet(Resource.class));
        when(application.getFactories()).thenReturn(newHashSet(resourceFactory));

        publisher.publish(application);

        verify(resources, never()).addResource(Resource.class, null);
        verify(resources).addResource(resourceFactory);
    }

    @Test
    public void publishesExceptionMapperWithFactoryAndOverridesPerRequestExceptionMapperThroughEverrestApplication() {
        EverrestApplication application = mock(EverrestApplication.class);
        ObjectFactory exceptionMapperFactory = mockObjectFactory(RuntimeExceptionMapper.class);
        when(application.getClasses()).thenReturn(newHashSet(RuntimeExceptionMapper.class));
        when(application.getFactories()).thenReturn(newHashSet(exceptionMapperFactory));

        publisher.publish(application);

        verify(providers, never()).addExceptionMapper(RuntimeExceptionMapper.class);
        verify(providers).addExceptionMapper(exceptionMapperFactory);
    }

    @Test
    public void publishesContextResolverWithFactoryAndOverridesPerRequestContextResolverThroughEverrestApplication() {
        EverrestApplication application = mock(EverrestApplication.class);
        ObjectFactory contextResolverFactory = mockObjectFactory(ContextResolverText.class);
        when(application.getClasses()).thenReturn(newHashSet(ContextResolverText.class));
        when(application.getFactories()).thenReturn(newHashSet(contextResolverFactory));

        publisher.publish(application);

        verify(providers, never()).addContextResolver(ContextResolverText.class);
        verify(providers).addContextResolver(contextResolverFactory);
    }

    @Test
    public void publishesMessageBodyReaderWithFactoryAndOverridesPerRequestMessageBodyReaderThroughEverrestApplication() {
        EverrestApplication application = mock(EverrestApplication.class);
        ObjectFactory messageBodyReaderFactory = mockObjectFactory(StringEntityProvider.class);
        when(application.getClasses()).thenReturn(newHashSet(StringEntityProvider.class));
        when(application.getFactories()).thenReturn(newHashSet(messageBodyReaderFactory));

        publisher.publish(application);

        verify(providers, never()).addMessageBodyReader(StringEntityProvider.class);
        verify(providers).addMessageBodyReader(messageBodyReaderFactory);
    }

    @Test
    public void publishesMessageBodyWriterWithFactoryAndOverridesPerRequestMessageBodyWriterThroughEverrestApplication() {
        EverrestApplication application = mock(EverrestApplication.class);
        ObjectFactory messageBodyWriterFactory = mockObjectFactory(StringEntityProvider.class);
        when(application.getClasses()).thenReturn(newHashSet(StringEntityProvider.class));
        when(application.getFactories()).thenReturn(newHashSet(messageBodyWriterFactory));

        publisher.publish(application);

        verify(providers, never()).addMessageBodyWriter(StringEntityProvider.class);
        verify(providers).addMessageBodyWriter(messageBodyWriterFactory);
    }

    @Test
    public void publishesMethodInvokerFilterWithFactoryAndOverridesPerRequestMethodInvokerFilterThroughEverrestApplication() {
        EverrestApplication application = mock(EverrestApplication.class);
        ObjectFactory methodInvokerFilterFactory = mockObjectFactory(AllMatchesMethodInvokerFilter.class);
        when(application.getClasses()).thenReturn(newHashSet(AllMatchesMethodInvokerFilter.class));
        when(application.getFactories()).thenReturn(newHashSet(methodInvokerFilterFactory));

        publisher.publish(application);

        verify(providers, never()).addMethodInvokerFilter(AllMatchesMethodInvokerFilter.class);
        verify(providers).addMethodInvokerFilter(methodInvokerFilterFactory);
    }

    @Test
    public void publishesRequestFilterWithFactoryAndOverridesPerRequestRequestFilterThroughEverrestApplication() {
        EverrestApplication application = mock(EverrestApplication.class);
        ObjectFactory requestFilterFactory = mockObjectFactory(AllMatchesRequestFilter.class);
        when(application.getClasses()).thenReturn(newHashSet(AllMatchesRequestFilter.class));
        when(application.getFactories()).thenReturn(newHashSet(requestFilterFactory));

        publisher.publish(application);

        verify(providers, never()).addRequestFilter(AllMatchesRequestFilter.class);
        verify(providers).addRequestFilter(requestFilterFactory);
    }

    @Test
    public void publishesResponseFilterWithFactoryAndOverridesPerRequestResponseFilterThroughEverrestApplication() {
        EverrestApplication application = mock(EverrestApplication.class);
        ObjectFactory responseFilterFactory = mockObjectFactory(AllMatchesResponseFilter.class);
        when(application.getClasses()).thenReturn(newHashSet(AllMatchesResponseFilter.class));
        when(application.getFactories()).thenReturn(newHashSet(responseFilterFactory));

        publisher.publish(application);

        verify(providers, never()).addResponseFilter(AllMatchesResponseFilter.class);
        verify(providers).addResponseFilter(responseFilterFactory);
    }

    private ObjectFactory mockObjectFactory(Class objectClass) {
        ObjectFactory objectFactory = mock(ObjectFactory.class, RETURNS_DEEP_STUBS);
        when(objectFactory.getObjectModel().getObjectClass()).thenReturn(objectClass);
        return objectFactory;
    }
}