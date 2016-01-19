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
package org.everrest.core.impl.resource;

import org.everrest.core.InitialProperties;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.header.HeaderHelper;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author andrew00x
 */
public class ContextParametersInjectionTest extends BaseTest {

    @Path("/a/b")
    public static class Resource1 {
        @GET
        @Path("c")
        public String m0(@Context UriInfo uriInfo) {
            return uriInfo.getRequestUri().toString();
        }

        @GET
        @Path("d")
        public String m1(@Context HttpHeaders headers) {
            List<String> l = headers.getRequestHeader("Accept");
            return HeaderHelper.convertToString(l);
        }

        @GET
        @Path("e")
        public String m2(@Context Request request) {
            return request.getMethod();
        }

        @GET
        @Path("f")
        public void m3(@Context Providers providers) {
            Assert.assertNotNull(providers);
        }

        @GET
        @Path("g")
        public void m4(@Context InitialProperties properties) {
            Assert.assertNotNull(properties);
        }
    }

    @Test
    public void testMethodContextInjection() throws Exception {
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
        injectionTest();
    }

    //--------------------

    @Path("/a/b")
    public static class Resource2 {
        @Context
        private UriInfo           uriInfo;
        @Context
        private HttpHeaders       headers;
        @Context
        private Request           request;
        @Context
        private Providers         providers;
        @Context
        private InitialProperties properties;

        @GET
        @Path("c")
        public String m0() {
            return uriInfo.getRequestUri().toString();
        }

        @GET
        @Path("d")
        public String m1() {
            List<String> l = headers.getRequestHeader("Accept");
            return HeaderHelper.convertToString(l);
        }

        @GET
        @Path("e")
        public String m2() {
            return request.getMethod();
        }

        @GET
        @Path("f")
        public void m3() {
            Assert.assertNotNull(providers);
        }

        @GET
        @Path("g")
        public void m4() {
            Assert.assertNotNull(properties);
        }

    }

    @Test
    public void testFieldInjection() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.<Class<?>>singleton(Resource2.class);
            }
        });
        injectionTest();
    }

    //--------------------

    @Path("/a/b")
    public static class Resource3 {
        private UriInfo           uriInfo;
        private HttpHeaders       headers;
        private Request           request;
        private Providers         providers;
        private InitialProperties properties;

        public Resource3(@Context UriInfo uriInfo, @Context HttpHeaders headers, @Context Request request,
                         @Context Providers providers, @Context InitialProperties properties) {
            this.uriInfo = uriInfo;
            this.headers = headers;
            this.request = request;
            this.providers = providers;
            this.properties = properties;
        }

        @GET
        @Path("c")
        public String m0() {
            return uriInfo.getRequestUri().toString();
        }

        @GET
        @Path("d")
        public String m1() {
            List<String> l = headers.getRequestHeader("Accept");
            return HeaderHelper.convertToString(l);
        }

        @GET
        @Path("e")
        public String m2() {
            return request.getMethod();
        }

        @GET
        @Path("f")
        public void m3() {
            Assert.assertNotNull(providers);
        }

        @GET
        @Path("g")
        public void m4() {
            Assert.assertNotNull(properties);
            properties.setProperty("ws.rs.tmpdir", "null");
        }
    }

    @Test
    public void testConstructorInjection() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.<Class<?>>singleton(Resource3.class);
            }
        });
        injectionTest();
    }

    //

    private void injectionTest() throws Exception {
        Assert.assertEquals("http://localhost/test/a/b/c",
                            launcher.service("GET", "http://localhost/test/a/b/c", "http://localhost/test", null, null, null).getEntity());
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.add("Accept", "text/xml");
        h.add("Accept", "text/plain;q=0.7");
        Assert.assertEquals("text/xml,text/plain;q=0.7",
                            launcher.service("GET", "http://localhost/test/a/b/d", "http://localhost/test", h, null, null).getEntity());
        Assert.assertEquals("GET",
                            launcher.service("GET", "http://localhost/test/a/b/e", "http://localhost/test", null, null, null).getEntity());
        Assert.assertEquals(204,
                            launcher.service("GET", "http://localhost/test/a/b/f", "http://localhost/test", null, null, null).getStatus());
        Assert.assertEquals(204,
                            launcher.service("GET", "http://localhost/test/a/b/g", "http://localhost/test", null, null, null).getStatus());
    }
}
