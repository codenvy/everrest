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
package org.everrest.core.impl.method;

import org.everrest.core.Property;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author andrew00x
 */
public class ParametersInjectionTest extends BaseTest {

    @Path("/a/{x}")
    public static class Resource1 {
        @GET
        @Path("/0/{y}/")
        public void m0(@PathParam("x") String x, @PathParam("y") String y) {
            Assert.assertNotNull(x);
            Assert.assertNotNull(y);
            Assert.assertEquals(x, y);
        }

        @GET
        @Path("/00/{y}/")
        public void m00(@Encoded @PathParam("x") String x, @PathParam("y") String y) throws Exception {
            Assert.assertNotNull(x);
            Assert.assertNotNull(y);
            Assert.assertEquals(URLDecoder.decode(x, "UTF-8"), y);
        }

        @GET
        @Path("/1/{y}/")
        public void m1(@PathParam("x") List<String> x, @PathParam("y") List<String> y) {
            Assert.assertNotNull(x);
            Assert.assertEquals(1, x.size());
            Assert.assertNotNull(y);
            Assert.assertEquals(1, y.size());
            Assert.assertEquals(x, y);
        }

        @GET
        @Path("/2/{y}/")
        public void m2(@PathParam("x") Set<String> x, @PathParam("y") Set<String> y) {
            Assert.assertNotNull(x);
            Assert.assertEquals(1, x.size());
            Assert.assertNotNull(y);
            Assert.assertEquals(1, y.size());
            Assert.assertEquals(x, y);
        }

        @GET
        @Path("/3/{y}/")
        public void m3(@PathParam("x") SortedSet<String> x, @PathParam("y") SortedSet<String> y) {
            Assert.assertNotNull(x);
            Assert.assertEquals(1, x.size());
            Assert.assertNotNull(y);
            Assert.assertEquals(1, y.size());
            Assert.assertEquals(x, y);
        }

        @GET
        @Path("/4/{y}/")
        public void m4(@PathParam("x") Integer x, @PathParam("y") Integer y) {
            Assert.assertNotNull(x);
            Assert.assertNotNull(y);
            Assert.assertEquals(1111, x - y);
        }

        @GET
        @Path("/5/{y}/")
        public void m5(@PathParam("x") long x, @PathParam("y") long y) {
            Assert.assertNotNull(x);
            Assert.assertNotNull(y);
            Assert.assertEquals(1111, x - y);
        }

        @GET
        @Path("/6/{y}/")
        public void m6(@PathParam("x") TestStringConstructorClass x, @PathParam("y") TestStringConstructorClass y) {
            Assert.assertNotNull(x);
            Assert.assertNotNull(y);
            Assert.assertEquals(x.toString(), y.toString());
        }

        @GET
        @Path("/7/{y}/")
        public void m7(@QueryParam("x") List<String> x, @QueryParam("y") List<String> y) {
            Assert.assertNotNull(x);
            Assert.assertEquals(3, x.size());
            Assert.assertNotNull(y);
            Assert.assertEquals(2, y.size());
            Assert.assertEquals("1", x.get(0));
            Assert.assertEquals("3", x.get(1));
            Assert.assertEquals("5", x.get(2));
            Assert.assertEquals("2", y.get(0));
            Assert.assertEquals("4", y.get(1));
        }

        @GET
        @Path("/8/{y}/")
        public void m8(@HeaderParam("foo") String x, @HeaderParam("bar") String y) {
            Assert.assertNotNull(x);
            Assert.assertNotNull(y);
            Assert.assertEquals(x, y);
        }

        @POST
        @Path("/9/{y}/")
        public void m9(@FormParam("foo") String x, @FormParam("bar") String y) {
            Assert.assertNotNull(x);
            Assert.assertNotNull(y);
            Assert.assertEquals(x, y);
        }

        @GET
        @Path("/10/{y}/")
        public void m10(@MatrixParam("foo") int x, @MatrixParam("bar") int y) {
            Assert.assertNotNull(x);
            Assert.assertNotNull(y);
            Assert.assertEquals(1111, x - y);
        }

        @GET
        @Path("/11/{y}/")
        public void m11(@CookieParam("foo") Cookie x, @CookieParam("bar") Cookie y) {
            Assert.assertNotNull(x);
            Assert.assertNotNull(y);
            Assert.assertEquals(x.getDomain(), y.getDomain());
            Assert.assertEquals(x.getPath(), y.getPath());
        }

        @GET
        @Path("/12/{y}/")
        public void m12(@Context UriInfo uriInfo) {
            Assert.assertNotNull(uriInfo);
            Assert.assertEquals(2, uriInfo.getPathParameters().size());
        }

        @GET
        @Path("/13")
        public String m13(@QueryParam("query") @DefaultValue("111") String param) {
            Assert.assertNotNull(param);
            return param;
        }

        @GET
        @Path("/14")
        public String m14(@Property("prop1") @DefaultValue("hello") String prop) {
            Assert.assertNotNull(prop);
            return prop;
        }
    }

    public static class TestStringConstructorClass {
        private final String s;

        public TestStringConstructorClass(String s) {
            this.s = s;
        }

        public String toString() {
            return s;
        }
    }

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
    public void testPathParameterString() throws Exception {
        Assert.assertEquals(204, launcher.service("GET", "/a/test/0/test", "", null, null, null).getStatus());
    }

    @Test
    public void testPathParameterStringEncoded() throws Exception {
        Assert.assertEquals(204, launcher.service("GET", "/a/te%20st/0/te%20st", "", null, null, null).getStatus());
    }

    @Test
    public void testPathParameterListOfStrings() throws Exception {
        Assert.assertEquals(204, launcher.service("GET", "/a/test/1/test", "", null, null, null).getStatus());
    }

    @Test
    public void testPathParameterSetOfStrings() throws Exception {
        Assert.assertEquals(204, launcher.service("GET", "/a/test/2/test", "", null, null, null).getStatus());
    }

    @Test
    public void testPathParameterSortedSetOfStrings() throws Exception {
        Assert.assertEquals(204, launcher.service("GET", "/a/test/3/test", "", null, null, null).getStatus());
    }

    @Test
    public void testPathParameterInteger() throws Exception {
        Assert.assertEquals(204, launcher.service("GET", "/a/3333/4/2222", "", null, null, null).getStatus());
    }

    @Test
    public void testPathParameterLong() throws Exception {
        Assert.assertEquals(204, launcher.service("GET", "/a/5555/5/4444", "", null, null, null).getStatus());
    }

    @Test
    public void testPathParameterCustomTypeWithStringConstructor() throws Exception {
        Assert.assertEquals(204, launcher.service("GET", "/a/test/6/test", "", null, null, null).getStatus());
    }

    @Test
    public void testQueyrParameterListOfString() throws Exception {
        Assert.assertEquals(204, launcher.service("GET", "/a/test/7/test?x=1&y=2&x=3&y=4&x=5", "", null, null, null).getStatus());
    }

    @Test
    public void testHeaderParameterString() throws Exception {
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("foo", "to be or not to be");
        h.putSingle("bar", "to be or not to be");
        Assert.assertEquals(204, launcher.service("GET", "/a/test/8/test", "", h, null, null).getStatus());
    }

    @Test
    public void testFormParameterString() throws Exception {
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("Content-Type", "application/x-www-form-urlencoded");
        Assert.assertEquals(204, launcher.service("POST", "/a/test/9/test", "", h,
                                                  "bar=to%20be%20or%20not%20to%20be&foo=to%20be%20or%20not%20to%20be".getBytes("UTF-8"),
                                                  null).getStatus());
    }

    @Test
    public void testCookieParameterCookie() throws Exception {
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("Cookie", "$Version=1;foo=foo;$Domain=exo.com;$Path=/exo,$Version=1;bar=ar;$Domain=exo.com;$Path=/exo");
        Assert.assertEquals(204, launcher.service("GET", "/a/test/11/test", "", h, null, null).getStatus());
    }

    @Test
    public void testContextParameterUriInfo() throws Exception {
        Assert.assertEquals(204, launcher.service("GET", "/a/111/12/222", "", null, null, null).getStatus());
    }

    @Test
    public void testQueryParameterWithDefaultValue() throws Exception {
        Assert.assertEquals("111", launcher.service("GET", "/a/111/13", "", null, null, null).getEntity());
        Assert.assertEquals("222", launcher.service("GET", "/a/111/13?query=222", "", null, null, null).getEntity());
    }

    @Test
    public void testPropertyParameter() throws Exception {
        Assert.assertEquals("hello", launcher.service("GET", "/a/111/14", "", null, null, null).getEntity());
        processor.setProperty("prop1", "to be or not to be");
        Assert.assertEquals("to be or not to be", launcher.service("GET", "/a/111/14", "", null, null, null).getEntity());
    }
}
