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
package org.everrest.core.impl.provider;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author andrew00x
 */
public class ExceptionMapperTest extends BaseTest {

    @Provider
    public static class ExceptionMapper1 implements ExceptionMapper<IllegalArgumentException> {
        public Response toResponse(IllegalArgumentException exception) {
            return Response.status(200).entity("IllegalArgumentException").build();
        }
    }

    @Provider
    public static class ExceptionMapper2 implements ExceptionMapper<RuntimeException> {
        public Response toResponse(RuntimeException exception) {
            return Response.status(200).entity("RuntimeException").build();
        }
    }

    @Provider
    public static class ExceptionMapper3 implements ExceptionMapper<WebApplicationException> {
        public Response toResponse(WebApplicationException exception) {
            return Response.status(200).entity("WebApplicationException").build();
        }
    }

    @Provider
    public static class ExceptionMapper4 implements ExceptionMapper<MockException> {
        public Response toResponse(MockException exception) {
            return Response.status(200).entity("MockException").build();
        }
    }

    @Provider
    public static class MockException extends Exception {
        private static final long serialVersionUID = 5029726201933185270L;
    }


    @Before
    public void setUp() throws Exception {
        super.setUp();
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> classes = new LinkedHashSet<>();
                classes.add(ExceptionMapper1.class);
                classes.add(ExceptionMapper2.class);
                classes.add(ExceptionMapper3.class);
                classes.add(ExceptionMapper4.class);
                classes.add(Resource1.class);
                return classes;
            }
        });
    }

    @Path("a")
    public static class Resource1 {
        @GET
        @Path("1")
        public void m1() {
            throw new IllegalArgumentException();
        }

        @GET
        @Path("2")
        public void m2() {
            throw new RuntimeException();
        }

        @GET
        @Path("3")
        public void m3() {
            throw new WebApplicationException(Response.status(400).build());
        }

        @GET
        @Path("4")
        public void m4() {
            throw new WebApplicationException(
                    Response.status(500).entity("this exception must not be hidden by any ExceptionMapper").build());
        }

        @GET
        @Path("5")
        public void m5() throws MockException {
            throw new MockException();
        }
    }

    @Test
    public void testExceptionMapperRuntimeException1() throws Exception {
        ContainerResponse resp = launcher.service("GET", "/a/1", "", null, null, null);
        Assert.assertEquals(200, resp.getStatus());
        Assert.assertEquals("IllegalArgumentException", resp.getEntity());
    }

    @Test
    public void testExceptionMapperRuntimeException2() throws Exception {
        ContainerResponse resp = launcher.service("GET", "/a/2", "", null, null, null);
        Assert.assertEquals(200, resp.getStatus());
        Assert.assertEquals("RuntimeException", resp.getEntity());
    }

    @Test
    public void testExceptionMapperWebApplicationException() throws Exception {
        ContainerResponse resp = launcher.service("GET", "/a/3", "", null, null, null);
        Assert.assertEquals(200, resp.getStatus());
        Assert.assertEquals("WebApplicationException", resp.getEntity());
    }

    @Test
    public void testExceptionMapperWebApplicationExceptionWithEntity() throws Exception {
        ContainerResponse resp = launcher.service("GET", "/a/4", "", null, null, null);
        // WebApplicationException with entity - must not be overridden
        Assert.assertEquals(500, resp.getStatus());
        Assert.assertEquals("this exception must not be hidden by any ExceptionMapper", resp.getEntity());
    }

    @Test
    public void testExceptionMapperCustomException() throws Exception {
        ContainerResponse resp = launcher.service("GET", "/a/5", "", null, null, null);
        Assert.assertEquals(200, resp.getStatus());
        Assert.assertEquals("MockException", resp.getEntity());
    }
}
