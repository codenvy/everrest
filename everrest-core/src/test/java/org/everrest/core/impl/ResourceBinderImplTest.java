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

import org.everrest.core.ApplicationContext;
import org.everrest.core.ObjectFactory;
import org.everrest.core.ResourcePublicationException;
import org.everrest.core.resource.ResourceDescriptor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedHashMap;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class ResourceBinderImplTest {
    @Rule public ExpectedException thrown = ExpectedException.none();

    private ResourceBinderImpl resourceBinder;

    @Before
    public void setUp() throws Exception {
        resourceBinder = new ResourceBinderImpl();
    }

    @Test
    public void bindsPerRequestResource() throws Exception {
       resourceBinder.addResource(EchoResource.class, new MultivaluedHashMap<>());

        ObjectFactory<ResourceDescriptor> resourceFactory = resourceBinder.getMatchedResource("/a/b", newArrayList());
        assertEquals(EchoResource.class, resourceFactory.getObjectModel().getObjectClass());
    }

    @Test
    public void throwsResourcePublicationExceptionWhenResourceClassIsNotAnnotatedWithPathAnnotation() throws Exception {
        thrown.expect(ResourcePublicationException.class);

        resourceBinder.addResource(NoPathAnnotationResource.class, new MultivaluedHashMap<>());
    }

    @Test
    public void bindsPerRequestResourceWithNewPath() throws Exception {
        resourceBinder.addResource("/x/y", EchoResource.class, new MultivaluedHashMap<>());

        ObjectFactory<ResourceDescriptor> resourceFactory = resourceBinder.getMatchedResource("/x/y", newArrayList());
        assertEquals(EchoResource.class, resourceFactory.getObjectModel().getObjectClass());
    }

    @Test
    public void bindsSingletonResource() throws Exception {
        EchoResource resource = new EchoResource();
        resourceBinder.addResource(resource, new MultivaluedHashMap<>());

        ObjectFactory<ResourceDescriptor> resourceFactory = resourceBinder.getMatchedResource("/a/b", newArrayList());
        assertSame(resource, resourceFactory.getInstance(mock(ApplicationContext.class)));
    }

    @Test
    public void throwsResourcePublicationExceptionWhenResourceIsNotAnnotatedWithPathAnnotation() throws Exception {
        thrown.expect(ResourcePublicationException.class);

        resourceBinder.addResource(new NoPathAnnotationResource(), new MultivaluedHashMap<>());
    }

    @Test
    public void bindsSingletonResourceWithNewPath() throws Exception {
        EchoResource resource = new EchoResource();
        resourceBinder.addResource("x/y", resource, new MultivaluedHashMap<>());

        ObjectFactory<ResourceDescriptor> resourceFactory = resourceBinder.getMatchedResource("/x/y", newArrayList());
        assertSame(resource, resourceFactory.getInstance(mock(ApplicationContext.class)));
    }

    @Test
    public void throwsPerRequestResourcePublicationExceptionWhenTryRegisterTwoResourcesWithTheSamePath() throws Exception {
        resourceBinder.addResource(EchoResource.class, new MultivaluedHashMap<>());

        thrown.expect(ResourcePublicationException.class);
        resourceBinder.addResource(OtherEchoResource.class, new MultivaluedHashMap<>());
    }

    @Test
    public void ignoresMultipleAttemptsToRegisterPerRequestResourcesOfTheSameClass() throws Exception {
        resourceBinder.addResource(EchoResource.class, new MultivaluedHashMap<>());
        ObjectFactory<ResourceDescriptor> resourceFactory = resourceBinder.getMatchedResource("/a/b", newArrayList());

        resourceBinder.addResource(EchoResource.class, new MultivaluedHashMap<>());

        assertSame(resourceFactory, resourceBinder.getMatchedResource("/a/b", newArrayList()));
    }

    @Test
    public void clearsAllRegisteredResources() throws Exception {
        resourceBinder.addResource(EchoResource.class, new MultivaluedHashMap<>());
        assertEquals(1, resourceBinder.getSize());

        resourceBinder.clear();
        assertEquals(0, resourceBinder.getSize());
    }

    @Test
    public void bindsResourceWithSubResourceMethod() throws Exception {
        resourceBinder.addResource(EchoResourceWithSubResourceMethod.class, new MultivaluedHashMap<>());

        List<String> parameterValues = newArrayList();
        ObjectFactory<ResourceDescriptor> resourceFactory = resourceBinder.getMatchedResource("/a/b", parameterValues);
        assertEquals(EchoResourceWithSubResourceMethod.class, resourceFactory.getObjectModel().getObjectClass());
        assertEquals(newArrayList("/b"), parameterValues);
    }

    @Test
    public void resourceWithLongerPathTakesPrecedenceOverResourceWithSorterPathInCaseIfTwoResourcesAreMatched() throws Exception {
        resourceBinder.addResource(EchoResourceWithSubResourceMethod.class, new MultivaluedHashMap<>());
        resourceBinder.addResource(EchoResource.class, new MultivaluedHashMap<>());
        assertEquals(2, resourceBinder.getSize());

        ObjectFactory<ResourceDescriptor> resourceFactory = resourceBinder.getMatchedResource("/a/b", newArrayList());
        assertEquals(EchoResource.class, resourceFactory.getObjectModel().getObjectClass());
    }

    @Test
    public void returnsNullIfNoMatchedResources() throws Exception {
        resourceBinder.addResource(EchoResource.class, new MultivaluedHashMap<>());

        ObjectFactory<ResourceDescriptor> resourceFactory = resourceBinder.getMatchedResource("/x/y", newArrayList());
        assertNull(resourceFactory);
    }

    @Test
    public void returnsListOfAllRegisteredResourcesAndAnyChangesOnThisListDoNotImpactOriginalList() throws Exception {
        resourceBinder.addResource(EchoResourceWithSubResourceMethod.class, new MultivaluedHashMap<>());
        resourceBinder.addResource(EchoResource.class, new MultivaluedHashMap<>());
        assertEquals(2, resourceBinder.getSize());

        resourceBinder.getResources().clear();
        assertEquals(2, resourceBinder.getSize());
    }

    @Test
    public void removesBoundResourceByClass() throws Exception {
        resourceBinder.addResource(EchoResource.class, new MultivaluedHashMap<>());

        ObjectFactory<ResourceDescriptor> resourceFactory = resourceBinder.getMatchedResource("/a/b", newArrayList());
        assertNotNull(resourceFactory);

        resourceBinder.removeResource(EchoResource.class);

        resourceFactory = resourceBinder.getMatchedResource("/a/b", newArrayList());
        assertNull(resourceFactory);
    }

    @Test
    public void removesBoundResourceByPath() throws Exception {
        resourceBinder.addResource(EchoResource.class, new MultivaluedHashMap<>());

        ObjectFactory<ResourceDescriptor> resourceFactory = resourceBinder.getMatchedResource("/a/b", newArrayList());
        assertNotNull(resourceFactory);

        resourceBinder.removeResource("a/b");

        resourceFactory = resourceBinder.getMatchedResource("/a/b", newArrayList());
        assertNull(resourceFactory);
    }

    @Path("a/b")
    public static class EchoResource {
        @Consumes("text/plain")
        @Produces("text/xml")
        @POST
        public String echo(String phrase) {
            return phrase;
        }
    }

    @Path("a/b")
    public static class OtherEchoResource {
        @Consumes("text/plain")
        @Produces("text/xml")
        @POST
        public String echo(String phrase) {
            return phrase;
        }
    }

    @Path("a")
    public static class EchoResourceWithSubResourceMethod {
        @Consumes("text/plain")
        @Produces("text/xml")
        @POST
        @Path("b")
        public String echo(String phrase) {
            return phrase;
        }
    }

    public static class NoPathAnnotationResource {
        @Consumes("text/plain")
        @Produces("text/xml")
        @GET
        public void m() {
        }
    }
}