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

import org.everrest.core.ExtHttpHeaders;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Set;

/**
 * @author Max Shaposhnik
 */
public class ExceptionsTest extends BaseTest {

    @Path("a")
    public static class Resource1 {
        @GET
        @Path("0")
        public void m0() throws WebApplicationException {
            Exception e = new Exception(errorMessage);
            throw new WebApplicationException(e, 500);
        }

        @GET
        @Path("1")
        public void m1() throws WebApplicationException {
            Response response = Response.status(500).entity(errorMessage).type("text/plain").build();
            throw new WebApplicationException(new Exception(), response);
        }

        @GET
        @Path("2")
        public Response m2() throws WebApplicationException {
            throw new WebApplicationException(500);
        }

        @GET
        @Path("3")
        public void m3() throws Exception {
            throw new RuntimeException("Runtime exception");
        }

        @GET
        @Path("4")
        public Response m4() throws Exception {
            return Response.status(500).entity(errorMessage).type("text/plain").build();
        }
    }

    private static String errorMessage = "test-error-message";

    @Before
    public void setUp() throws Exception {
        super.setUp();
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource1());
            }
        });
    }

    @Test
    public void testErrorResponse() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service("GET", "/a/4", "", null, null, writer, null);
        Assert.assertEquals(500, response.getStatus());
        String entity = new String(writer.getBody());
        Assert.assertEquals(errorMessage, entity);
        Assert.assertNotNull(/*response*/writer.getHeaders().getFirst(ExtHttpHeaders.JAXRS_BODY_PROVIDED));
    }

    @Test
    public void testUncheckedException() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

        ContainerResponse response = launcher.service("GET", "/a/3", "", null, null, writer, null);
        Assert.assertEquals(500, response.getStatus());
        String entity = new String(writer.getBody());
        Assert.assertEquals("Runtime exception", entity);
        Assert.assertNotNull(writer.getHeaders().getFirst(ExtHttpHeaders.JAXRS_BODY_PROVIDED));
    }

    @Test
    public void testWebApplicationExceptionWithCause() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service("GET", "/a/0", "", null, null, writer, null);
        Assert.assertEquals(500, response.getStatus());
        String entity = new String(writer.getBody());
        Assert.assertEquals(new Exception(errorMessage).toString(), entity);
        Assert.assertNotNull(writer.getHeaders().getFirst(ExtHttpHeaders.JAXRS_BODY_PROVIDED));
    }

    @Test
    public void testWebApplicationExceptionWithoutCause() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service("GET", "/a/2", "", null, null, writer, null);
        Assert.assertEquals(500, response.getStatus());
        Assert.assertNull(response.getEntity());
        Assert.assertNull(writer.getHeaders().getFirst(ExtHttpHeaders.JAXRS_BODY_PROVIDED));
    }

    @Test
    public void testWebApplicationExceptionWithResponse() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service("GET", "/a/1", "", null, null, writer, null);
        Assert.assertEquals(500, response.getStatus());
        String entity = new String(writer.getBody());
        Assert.assertEquals(errorMessage, entity);
        Assert.assertNotNull(writer.getHeaders().getFirst(ExtHttpHeaders.JAXRS_BODY_PROVIDED));
    }
}
