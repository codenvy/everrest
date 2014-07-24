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

import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.everrest.test.mock.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class RequestFilterTest extends BaseTest {

    public void setUp() throws Exception {
        super.setUp();
    }

    @Filter
    public static class RequestFilter1 implements RequestFilter {

        @Context
        private UriInfo uriInfo;

        @Context
        private HttpHeaders httpHeaders;

        private Providers providers;

        private HttpServletRequest httpRequest;

        public RequestFilter1(@Context Providers providers, @Context HttpServletRequest httpRequest) {
            this.providers = providers;
            this.httpRequest = httpRequest;
        }

        public void doFilter(GenericContainerRequest request) {
            if (uriInfo != null && httpHeaders != null && providers != null && httpRequest != null)
                request.setMethod("POST");
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

    public void testWithoutFilter1() throws Exception {
        registry(Resource1.class);
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse resp = launcher.service("GET", "/a", "", null, null, writer, null);
        assertEquals(405, resp.getStatus());
        assertEquals(1, writer.getHeaders().get("allow").size());
        assertTrue(writer.getHeaders().get("allow").get(0).toString().contains("POST"));
        unregistry(Resource1.class);
    }

    public void testWithFilter2() throws Exception {
        registry(Resource1.class);

        // add filter that can change method
        providers.addRequestFilter(RequestFilter1.class);
        EnvironmentContext env = new EnvironmentContext();
        env.put(HttpServletRequest.class, new MockHttpServletRequest("", new ByteArrayInputStream(new byte[0]), 0, "GET",
                                                                     new HashMap<String, List<String>>()));

        // should get status 204
        ContainerResponse resp = launcher.service("GET", "/a", "", null, null, env);
        assertEquals(204, resp.getStatus());

        unregistry(Resource1.class);

    }

    public void testFilter2() throws Exception {
        registry(Resource1.class);
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse resp = launcher.service("GET", "/a/b/c/d/e", "", null, null, writer, null);
        assertEquals(405, resp.getStatus());
        assertEquals(1, writer.getHeaders().get("allow").size());
        assertTrue(writer.getHeaders().get("allow").get(0).toString().contains("DELETE"));

        // add filter that can change method
        providers.addRequestFilter(new RequestFilter2());

        // not should get status 204
        resp = launcher.service("GET", "/a/b/c/d/e", "", null, null, null);
        assertEquals(204, resp.getStatus());

        unregistry(Resource1.class);
    }

}
