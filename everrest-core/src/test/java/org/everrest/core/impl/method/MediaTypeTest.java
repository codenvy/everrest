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

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;
import java.util.Set;

/**
 * @author Dmytro Katayev
 */
public class MediaTypeTest extends BaseTest {

    @Path("/1")
    @Produces(MediaType.TEXT_PLAIN)
    public static class Resource1 {
        @GET
        public String m0() {
            return "m0";
        }

        @GET
        @Path("/c")
        public String m1() {
            return "m1";
        }

        @GET
        @Path("/d")
        @Produces(MediaType.TEXT_XML)
        public String m2() {
            return "m2";
        }

        @GET
        @Path("/e")
        @Produces(MediaType.TEXT_XML)
        public String m3() {
            return "m3";
        }
    }

    @Test
    public void testProducedMediaTypes() throws Exception {
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

        Assert.assertEquals(200, launcher.service("GET", "/1", "", null, null, null).getStatus());
        Assert.assertEquals("m0", launcher.service("GET", "/1", "", null, null, null).getEntity());
        Assert.assertEquals(MediaType.TEXT_PLAIN_TYPE, launcher.service("GET", "/1", "", null, null, null).getContentType());

        Assert.assertEquals(200, launcher.service("GET", "/1/c", "", null, null, null).getStatus());
        Assert.assertEquals(MediaType.TEXT_PLAIN_TYPE, launcher.service("GET", "/1/c", "", null, null, null).getContentType());

        Assert.assertEquals(200, launcher.service("GET", "/1/d", "", null, null, null).getStatus());
        Assert.assertEquals(MediaType.TEXT_XML_TYPE, launcher.service("GET", "/1/d", "", null, null, null).getContentType());

        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);

        Assert.assertEquals(406, launcher.service("GET", "/1/d", "", headers, null, null).getStatus());
        Assert.assertEquals(MediaType.TEXT_XML_TYPE, launcher.service("GET", "/1/d", "", null, null, null).getContentType());
    }

    @Path("/2")
    @Consumes(MediaType.TEXT_PLAIN)
    public static class Resource2 {
        @GET
        @Path("/e")
        public String m0(@HeaderParam(HttpHeaders.CONTENT_TYPE) String type) {
            Assert.assertEquals(MediaType.TEXT_PLAIN, type);
            return "m0";
        }

        @GET
        @Path("/f")
        @Consumes(MediaType.APPLICATION_JSON)
        public String m1(@HeaderParam(HttpHeaders.CONTENT_TYPE) String type) {
            Assert.assertEquals(MediaType.APPLICATION_JSON, type);
            return "m1";
        }
    }

    @Test
    public void testConsumedMediaTypes() throws Exception {
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
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);

        Assert.assertEquals(200, launcher.service("GET", "/2/e", "", headers, null, null).getStatus());
        Assert.assertEquals("m0", launcher.service("GET", "/2/e", "", headers, null, null).getEntity());

        headers = new MultivaluedMapImpl();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        Assert.assertEquals(200, launcher.service("GET", "/2/f", "", headers, null, null).getStatus());
        Assert.assertEquals("m1", launcher.service("GET", "/2/f", "", headers, null, null).getEntity());

        headers = new MultivaluedMapImpl();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML);

        Assert.assertEquals(415, launcher.service("GET", "/2/f", "", headers, null, null).getStatus());
    }
}
