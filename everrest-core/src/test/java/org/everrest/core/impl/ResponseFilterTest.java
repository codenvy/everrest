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
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.ResponseFilter;
import org.everrest.test.mock.MockHttpServletRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
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
public class ResponseFilterTest extends BaseTest {

    @Before
    @Override
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

    @Filter
    public static class ResponseFilter1 implements ResponseFilter {
        @Context
        private UriInfo            uriInfo;
        @Context
        private HttpHeaders        httpHeaders;
        private Providers          providers;
        private HttpServletRequest httpRequest;

        public ResponseFilter1(@Context Providers providers, @Context HttpServletRequest httpRequest) {
            this.providers = providers;
            this.httpRequest = httpRequest;
        }

        public void doFilter(GenericContainerResponse response) {
            if (uriInfo != null && httpHeaders != null && providers != null && httpRequest != null) {
                response.setResponse(Response.status(200).entity("to be or not to be").type("text/plain").build());
            }
        }
    }

    @Path("a/b/c/{x:.*}")
    @Filter
    public static class ResponseFilter2 implements ResponseFilter {
        public void doFilter(GenericContainerResponse response) {
            response.setResponse(Response.status(response.getStatus()).entity(response.getEntity()).type("application/json").build());
        }
    }

    @Path("a")
    public static class Resource1 {
        @POST
        public void m0() {
        }

        @POST
        @Path("b/c/d/e")
        @Produces("text/plain")
        public String m1() {
            // text/plain will be overridden in response filter
            return "{\"name\":\"andrew\", \"password\":\"hello\"}";
        }
    }

    //------------------------------------

    @Test
    public void testFilter() throws Exception {
        ContainerResponse resp = launcher.service("POST", "/a", "", null, null, null);
        Assert.assertEquals(204, resp.getStatus());
        // add response filter and try again
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.<Class<?>>singleton(ResponseFilter1.class);
            }
        });
        EnvironmentContext env = new EnvironmentContext();
        env.put(HttpServletRequest.class,
                new MockHttpServletRequest("", new ByteArrayInputStream(new byte[0]), 0, "POST", new HashMap<String, List<String>>()));
        resp = launcher.service("POST", "/a", "", null, null, env);
        Assert.assertEquals(200, resp.getStatus());
        Assert.assertEquals("text/plain", resp.getContentType().toString());
        Assert.assertEquals("to be or not to be", resp.getEntity());
    }

    @Test
    public void testFilter2() throws Exception {
        ContainerResponse resp = launcher.service("POST", "/a/b/c/d/e", "", null, null, null);
        Assert.assertEquals(200, resp.getStatus());
        Assert.assertEquals("text/plain", resp.getContentType().toString());
        Assert.assertEquals("{\"name\":\"andrew\", \"password\":\"hello\"}", resp.getEntity());
        // add response filter and try again
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.<Class<?>>singleton(ResponseFilter2.class);
            }
        });
        resp = launcher.service("POST", "/a/b/c/d/e", "", null, null, null);
        Assert.assertEquals(200, resp.getStatus());
        Assert.assertEquals("application/json", resp.getContentType().toString());
        Assert.assertEquals("{\"name\":\"andrew\", \"password\":\"hello\"}", resp.getEntity());
    }
}
