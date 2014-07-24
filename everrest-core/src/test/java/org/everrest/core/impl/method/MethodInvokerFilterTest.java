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
package org.everrest.core.impl.method;

import org.everrest.core.Filter;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.resource.GenericMethodResource;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.resource.SubResourceMethodDescriptor;
import org.everrest.test.mock.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class MethodInvokerFilterTest extends BaseTest {

    @Filter
    public static class MethodInvokerFilter1 implements MethodInvokerFilter {

        private UriInfo uriInfo;

        private HttpHeaders httpHeaders;

        @Context
        private Providers providers;

        @Context
        private HttpServletRequest httpRequest;

        public MethodInvokerFilter1(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders) {
            this.uriInfo = uriInfo;
            this.httpHeaders = httpHeaders;
        }

        public void accept(GenericMethodResource genericMethodResource) {
            if (uriInfo != null && httpHeaders != null && providers != null && httpRequest != null) {
                if (genericMethodResource instanceof SubResourceMethodDescriptor)
                    // not invoke sub-resource method
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());
                else if (genericMethodResource instanceof ResourceMethodDescriptor)
                    System.out.println("MethodInvokerFilter1: >>>>>>>>>>>> ResourceMethodDescriptor");
            } else {
                throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
            }
        }

    }

    @Filter
    @Path("b/c")
    public static class MethodInvokerFilter2 implements MethodInvokerFilter {

        public void accept(GenericMethodResource genericMethodResource) {
            System.out.println("MethodInvokerFilter2: >>>>>>>>>>>>");
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());
        }

    }

    @Path("a")
    public static class Resource1 {
        @GET
        public void m0() {
        }

        @GET
        @Path("b")
        public void m1() {
        }
    }

    @Path("b")
    public static class Resource2 {
        @GET
        @Path("c")
        public void m0() {
        }

        @GET
        @Path("d")
        public void m1() {
        }
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testInvokerFilter() throws Exception {
        Resource1 r = new Resource1();
        registry(r);
        assertEquals(204, launcher.service("GET", "/a/b", "", null, null, null).getStatus());
        assertEquals(204, launcher.service("GET", "/a", "", null, null, null).getStatus());
        providers.addMethodInvokerFilter(MethodInvokerFilter1.class);
        EnvironmentContext env = new EnvironmentContext();
        env.put(HttpServletRequest.class, new MockHttpServletRequest("", new ByteArrayInputStream(new byte[0]), 0, "GET",
                                                                     new HashMap<String, List<String>>()));
        assertEquals(400, launcher.service("GET", "/a/b", "", null, null, env).getStatus());
        assertEquals(204, launcher.service("GET", "/a", "", null, null, env).getStatus());
        unregistry(r);
    }

    public void testInvokerFilter2() throws Exception {
        Resource2 r = new Resource2();
        registry(r);
        assertEquals(204, launcher.service("GET", "/b/c", "", null, null, null).getStatus());
        assertEquals(204, launcher.service("GET", "/b/d", "", null, null, null).getStatus());
        providers.addMethodInvokerFilter(new MethodInvokerFilter2());
        assertEquals(400, launcher.service("GET", "/b/c", "", null, null, null).getStatus());
        assertEquals(204, launcher.service("GET", "/b/d", "", null, null, null).getStatus());
        unregistry(r);
    }

}
