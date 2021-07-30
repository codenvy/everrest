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
package org.everrest.core.impl.async;

import org.everrest.core.ApplicationContext;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AsynchronousJobPoolTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final String asyncServicePath = "/async";

    private ApplicationContext       applicationContext;
    private AsynchronousJobPool      asynchronousJobPool;
    private Resource                 resource;
    private ResourceMethodDescriptor methodDescriptor;

    @Before
    public void setUp() throws Exception {
        applicationContext = mockApplicationContext();
        ApplicationContext.setCurrent(applicationContext);

        final EverrestConfiguration configuration = new EverrestConfiguration();
        configuration.setAsynchronousQueueSize(1);
        configuration.setAsynchronousPoolSize(1);
        configuration.setAsynchronousServicePath(asyncServicePath);
        asynchronousJobPool = new AsynchronousJobPool(configuration);

        AsynchronousFuture job = mock(AsynchronousFuture.class);
        when(job.getJobId()).thenReturn(1L);
        Map<String, Object> jobContext = new HashMap<>();
        when(job.getContext()).thenReturn(jobContext);
        when(job.getExpirationDate()).thenReturn(System.currentTimeMillis() + 10000);
        doAnswer(sleep(500)).when(job).run();

        final AsynchronousFutureFactory asynchronousFutureFactory = mock(AsynchronousFutureFactory.class);
        when(asynchronousFutureFactory.createAsynchronousFuture(isA(Callable.class),
                                                                anyLong(),
                                                                isA(ResourceMethodDescriptor.class),
                                                                anyListOf(AsynchronousJobListener.class)))
                .thenReturn(job);
        asynchronousJobPool.setAsynchronousFutureFactory(asynchronousFutureFactory);

        resource = new Resource();
        methodDescriptor = mock(ResourceMethodDescriptor.class);
        when(methodDescriptor.getMethod()).thenReturn(Resource.class.getMethod("m"));
    }

    @After
    public void tearDown() throws Exception {
        asynchronousJobPool.stop();
    }

    @Test
    public void readsServicePathFromConfiguration() throws Exception {
        final EverrestConfiguration configuration = new EverrestConfiguration();
        configuration.setAsynchronousServicePath("/async/bla/bla");
        asynchronousJobPool = new AsynchronousJobPool(configuration);
        assertEquals("/async/bla/bla", asynchronousJobPool.getAsynchronousServicePath());
    }

    @Test
    public void readsMaxCacheFromConfiguration() throws Exception {
        final EverrestConfiguration configuration = new EverrestConfiguration();
        configuration.setAsynchronousCacheSize(123);
        asynchronousJobPool = new AsynchronousJobPool(configuration);
        assertEquals(123, asynchronousJobPool.getMaxCacheSize());
    }

    @Test
    public void readsTimeoutFromConfiguration() throws Exception {
        final EverrestConfiguration configuration = new EverrestConfiguration();
        configuration.setAsynchronousJobTimeout(777);
        asynchronousJobPool = new AsynchronousJobPool(configuration);
        assertEquals(777, asynchronousJobPool.getJobTimeout());
    }

    @Test
    public void readsQueueSizeFromConfiguration() throws Exception {
        final EverrestConfiguration configuration = new EverrestConfiguration();
        configuration.setAsynchronousQueueSize(555);
        asynchronousJobPool = new AsynchronousJobPool(configuration);
        assertEquals(555, asynchronousJobPool.getMaxQueueSize());
    }

    @Test
    public void readsPoolSizeFromConfiguration() throws Exception {
        final EverrestConfiguration configuration = new EverrestConfiguration();
        configuration.setAsynchronousPoolSize(111);
        asynchronousJobPool = new AsynchronousJobPool(configuration);
        assertEquals(111, asynchronousJobPool.getThreadPoolSize());
    }

    @Test
    public void addsJobInPool() throws Exception {
        AsynchronousFuture job = (AsynchronousFuture)asynchronousJobPool.addJob(resource, methodDescriptor, new Object[]{});

        ArgumentCaptor<String> jobUriCaptor = ArgumentCaptor.forClass(String.class);
        verify(job).setJobURI(jobUriCaptor.capture());
        assertEquals(String.format("%s/%d", asyncServicePath, job.getJobId()), jobUriCaptor.getValue());

        assertSame(applicationContext.getProviders(), job.getContext().get("org.everrest.async.providers"));

        GenericContainerRequest originRequest = applicationContext.getContainerRequest();
        GenericContainerRequest copiedRequest = (GenericContainerRequest)job.getContext().get("org.everrest.async.request");
        assertEquals(originRequest.getMethod(), copiedRequest.getMethod());
        assertEquals(originRequest.getRequestUri(), copiedRequest.getRequestUri());
        assertEquals(originRequest.getBaseUri(), copiedRequest.getBaseUri());
        assertEquals(originRequest.getRequestHeaders(), copiedRequest.getRequestHeaders());

        Thread.sleep(100);
        verify(job).run();
    }

    @Test
    public void failsAddNewJobIfTooManyJobsInProgress() throws Exception {
        asynchronousJobPool.addJob(resource, methodDescriptor, new Object[]{});
        asynchronousJobPool.addJob(resource, methodDescriptor, new Object[]{});

        thrown.expect(AsynchronousJobRejectedException.class);
        thrown.expectMessage("Can't accept new asynchronous request. Too many asynchronous jobs in progress");
        asynchronousJobPool.addJob(resource, methodDescriptor, new Object[]{});
    }

    @Test
    public void removesJobFromPool() throws Exception {
        AsynchronousFuture job = (AsynchronousFuture)asynchronousJobPool.addJob(resource, methodDescriptor, new Object[]{});

        Thread.sleep(100);
        asynchronousJobPool.removeJob(job.getJobId());

        verify(job).cancel();
    }

    @Test
    public void getsJobById() throws Exception {
        AsynchronousFuture job = (AsynchronousFuture)asynchronousJobPool.addJob(resource, methodDescriptor, new Object[]{});
        assertSame(job, asynchronousJobPool.getJob(job.getJobId()));
    }

    private Answer sleep(long millis) {
        return invocation -> {
            Thread.sleep(millis);
            return null;
        };
    }

    private ApplicationContext mockApplicationContext() {
        GenericContainerRequest containerRequest = mockContainerRequest();
        GenericContainerResponse containerResponse = mock(GenericContainerResponse.class);

        ApplicationContext applicationContext = mock(ApplicationContext.class);

        when(applicationContext.getContainerRequest()).thenReturn(containerRequest);
        when(applicationContext.getContainerResponse()).thenReturn(containerResponse);

        return applicationContext;
    }

    private GenericContainerRequest mockContainerRequest() {
        GenericContainerRequest containerRequest = mock(GenericContainerRequest.class);
        when(containerRequest.getMethod()).thenReturn("POST");
        when(containerRequest.getRequestUri()).thenReturn(URI.create("/a"));
        when(containerRequest.getBaseUri()).thenReturn(URI.create(""));
        when(containerRequest.getRequestHeaders()).thenReturn(new MultivaluedMapImpl());
        return containerRequest;
    }

    public static class Resource {
        public void m() {
        }
    }
}