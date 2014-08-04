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
package org.everrest.core.impl.resource;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;
import java.util.Set;

/**
 * @author andrew00x
 */
public class AcceptResourceTest extends BaseTest {

    @Path("/a")
    public static class Resource1 {
        @POST
        @Consumes({"text/*"})
        public String m0() {
            return "m0";
        }

        @POST
        @Consumes({"image/*"})
        public String m1() {
            return "m1";
        }

        @POST
        @Consumes({"text/xml", "application/xml"})
        public String m2() {
            return "m2";
        }

        @POST
        @Consumes({"image/jpeg", "image/png"})
        public String m3() {
            return "m3";
        }

        @POST
        public String m4() {
            return "m4";
        }
    }

    @Path("/a")
    public static class Resource2 {
        @GET
        @Produces({"text/plain", "text/html"})
        public String m0() {
            return "m0";
        }

        @GET
        @Produces({"text/xml", "text/*"})
        public String m1() {
            return "m1";
        }

        @GET
        @Produces({"image/*"})
        public String m2() {
            return "m2";
        }

        @GET
        @Produces({"image/jpeg", "image/png"})
        public String m3() {
            return "m3";
        }

        @GET
        public String m4() {
            return "m4";
        }
    }

    @Path("/a")
    public static class Resource3 {
        @POST
        @Consumes({"text/plain", "text/xml"})
        @Produces({"text/xml"})
        public String m0() {
            return "m0";
        }

        @POST
        @Consumes({"image/*", "image/png"})
        @Produces({"image/gif"})
        public String m1() {
            return "m1";
        }

        @POST
        @Consumes({"application/xml", "text/xml"})
        @Produces({"text/html"})
        public String m2() {
            return "m2";
        }

        @POST
        public String m3() {
            return "m3";
        }
    }

    @Path("/a")
    public static class Resource4 {
        @POST
        @Consumes({"text/*+xml"})
        @Produces({"text/*+xml"})
        public String m0() {
            return "m0";
        }

        @POST
        @Consumes({"application/*+xml"})
        @Produces({"application/xhtml+xml"})
        public String m1() {
            return "m1";
        }
    }

    @Test
    public void testContentType() throws Exception {
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
        Assert.assertEquals("m0", testContentType("text/html"));
        Assert.assertEquals("m2", testContentType("text/xml"));
        Assert.assertEquals("m2", testContentType("application/xml"));
        Assert.assertEquals("m1", testContentType("image/gif"));
        Assert.assertEquals("m3", testContentType("image/jpeg"));
        Assert.assertEquals("m3", testContentType("image/png"));
        Assert.assertEquals("m4", testContentType("application/x-www-form-urlencoded"));
    }

    @Test
    public void testAcceptedMediaType() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource2());
            }
        });
        Assert.assertEquals("m0", testAcceptedMediaType("text/plain;q=0.9,text/html;q=0.7,text/*;q=0.5"));
        Assert.assertEquals("m0", testAcceptedMediaType("text/plain;q=0.7,text/html;q=0.9,text/*;q=0.5"));
        Assert.assertEquals("m0", testAcceptedMediaType("text/plain;q=0.5,text/html;q=0.7,text/*;q=0.9"));

        Assert.assertEquals("m1", testAcceptedMediaType("text/xml;q=0.9,text/bell;q=0.5"));
        Assert.assertEquals("m1", testAcceptedMediaType("text/foo"));
        Assert.assertEquals("m2", testAcceptedMediaType("image/gif"));

        Assert.assertEquals("m3", testAcceptedMediaType("image/jpeg;q=0.8,  image/png;q=0.9"));
        Assert.assertEquals("m3", testAcceptedMediaType("image/foo;q=0.8,  image/png;q=0.9"));
        Assert.assertEquals("m2", testAcceptedMediaType("image/foo;q=0.9,  image/png;q=0.8"));

        Assert.assertEquals("m2", testAcceptedMediaType("image/foo;q=0.9,  image/gif;q=0.8"));

        Assert.assertEquals("m4", testAcceptedMediaType("application/x-www-form-urlencoded"));
        Assert.assertEquals("m0", testAcceptedMediaType("application/x-www-form-urlencoded;q=0.5,text/plain"));
    }

    @Test
    public void testComplex() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource3());
            }
        });
        Assert.assertEquals("m3", testComplex("text/plain", "text/plain;q=0.9"));
        Assert.assertEquals("m0", testComplex("text/plain", "text/plain;q=0.3,text/xml;q=0.9"));
        Assert.assertEquals("m3", testComplex("text/xml", "text/plain;q=0.9,text/html;q=0.3"));
        Assert.assertEquals("m0", testComplex("text/xml", "text/xml,text/*;q=0.3"));
        Assert.assertEquals("m1", testComplex("image/*", "image/*"));
        Assert.assertEquals("m3", testComplex("image/*", "image/png"));
        Assert.assertEquals("m3", testComplex("image/*", "image/png,image/gif;q=0.1"));
        Assert.assertEquals("m1", testComplex("image/*", "image/*,image/gif;q=0.1"));
        Assert.assertEquals("m3", testComplex("foo/bar", "foo/bar"));
    }

    @Test
    public void testExtSubtypeWithWildcard() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.<Class<?>>singleton(Resource4.class);
            }
        });
        Assert.assertEquals("m0", testComplex("text/xml", "text/xml,text/*+xml;q=.8"));
    }

    @Test
    public void testExtSubtype() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.<Class<?>>singleton(Resource4.class);
            }
        });
        Assert.assertEquals("m1", testComplex("application/atom+xml", "application/xhtml+xml"));
    }

    private String testContentType(String contentType) throws Exception {
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("content-type", contentType);
        return (String)launcher.service("POST", "/a", "", h, null, null).getEntity();
    }

    private String testAcceptedMediaType(String acceptMediaType) throws Exception {
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("accept", acceptMediaType);
        return (String)launcher.service("GET", "/a", "", h, null, null).getEntity();
    }

    private String testComplex(String contentType, String acceptMediaType) throws Exception {
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("content-type", contentType);
        h.putSingle("accept", acceptMediaType);
        return (String)launcher.service("POST", "/a", "", h, null, null).getEntity();
    }
}
