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

import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.everrest.test.mock.MockHttpServletRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author andrew00x
 */
public class RequestFilterTest extends BaseTest {

    @Filter
    public static class RequestFilter1 implements RequestFilter {
        @Context
        private UriInfo            uriInfo;
        @Context
        private HttpHeaders        httpHeaders;
        private Providers          providers;
        private HttpServletRequest httpRequest;

        public RequestFilter1(@Context Providers providers, @Context HttpServletRequest httpRequest) {
            this.providers = providers;
            this.httpRequest = httpRequest;
        }

        public void doFilter(GenericContainerRequest request) {
            if (uriInfo != null && httpHeaders != null && providers != null && httpRequest != null) {
                request.setMethod("POST");
            }
        }

    }

    @Path("a/b/c/{x:.*}")
    @Filter
    public static class RequestFilter2 implements RequestFilter {
        public void doFilter(GenericContainerRequest request) {
            request.setMethod("DELETE");
        }
    }

    @Path("a")
    public static class Resource1 {
        @POST
        public void m0() {
        }

        @DELETE
        @Path("b/c/d/e")
        public void m1() {
        }

        @PUT
        @Path("c/d/e")
        public void m2() {
        }
    }

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
                Set<Object> singletons = new LinkedHashSet<>();
                singletons.add(new Resource1());
                return singletons;
            }
        });
    }

    @Test
    public void testFilter1() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse resp = launcher.service("GET", "/a", "", null, null, writer, null);
        Assert.assertEquals(405, resp.getStatus());
        Assert.assertEquals(1, writer.getHeaders().get("allow").size());
        Assert.assertTrue(writer.getHeaders().get("allow").get(0).toString().contains("POST"));
        // add filter that can change method
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.<Class<?>>singleton(RequestFilter1.class);
            }
        });
        EnvironmentContext env = new EnvironmentContext();
        env.put(HttpServletRequest.class, new MockHttpServletRequest("", new ByteArrayInputStream(new byte[0]), 0, "GET",
                                                                     new HashMap<String, List<String>>()));

        // should get status 204
        resp = launcher.service("GET", "/a", "", null, null, env);
        Assert.assertEquals(204, resp.getStatus());
    }

    @Test
    public void testFilter2() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse resp = launcher.service("GET", "/a/b/c/d/e", "", null, null, writer, null);
        Assert.assertEquals(405, resp.getStatus());
        Assert.assertEquals(1, writer.getHeaders().get("allow").size());
        Assert.assertTrue(writer.getHeaders().get("allow").get(0).toString().contains("DELETE"));

        // add filter that can change method
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.<Class<?>>singleton(RequestFilter2.class);
            }
        });

        // not should get status 204
        resp = launcher.service("GET", "/a/b/c/d/e", "", null, null, null);
        Assert.assertEquals(204, resp.getStatus());
    }
}
