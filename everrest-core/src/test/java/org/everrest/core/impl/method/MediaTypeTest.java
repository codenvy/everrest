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

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.MultivaluedMapImpl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Created by The eXo Platform SAS.
 * <p/>
 * <br/>
 * Date: 22 Jan 2009
 *
 * @author <a href="mailto:dmitry.kataev@exoplatform.com.ua">Dmytro Katayev</a>
 * @version $Id: MediaTypeTest.java
 */
public class MediaTypeTest extends BaseTest {

    public void setUp() throws Exception {
        super.setUp();
    }

    @Path("/a")
    public static class Resource1 {

        @GET
        public String m0() {
            return "m0";
        }

    }

    @Path("/b")
    @Produces(MediaType.TEXT_PLAIN)
    public static class Resource2 {

        @GET
        @Path("/c")
        public String m0() {
            return "m0";
        }

        @GET
        @Path("/d")
        @Produces(MediaType.TEXT_XML)
        public String m1() {
            return "m0";
        }

        @GET
        @Path("/e")
        @Produces(MediaType.TEXT_XML)
        public String m2() {
            return "m0";
        }

    }

    public void testProducedMediaTypes() throws Exception {
        Resource1 resource1 = new Resource1();
        Resource2 resource2 = new Resource2();
        registry(resource1);
        registry(resource2);

        assertEquals(200, launcher.service("GET", "/a", "", null, null, null).getStatus());
        assertEquals("m0", launcher.service("GET", "/a", "", null, null, null).getEntity());
        assertEquals(MediaType.WILDCARD_TYPE, launcher.service("GET", "/a", "", null, null, null).getContentType());

        assertEquals(200, launcher.service("GET", "/b/c", "", null, null, null).getStatus());
        assertEquals(MediaType.TEXT_PLAIN_TYPE, launcher.service("GET", "/b/c", "", null, null, null).getContentType());

        assertEquals(200, launcher.service("GET", "/b/d", "", null, null, null).getStatus());
        assertEquals(MediaType.TEXT_XML_TYPE, launcher.service("GET", "/b/d", "", null, null, null).getContentType());

        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);

        assertEquals(406, launcher.service("GET", "/b/d", "", headers, null, null).getStatus());
        assertEquals(MediaType.TEXT_XML_TYPE, launcher.service("GET", "/b/d", "", null, null, null).getContentType());

        unregistry(resource1);
        unregistry(resource2);

    }

    @Path("/d")
    @Consumes(MediaType.TEXT_PLAIN)
    public static class Resource4 {

        @GET
        @Path("/e")
        public String m0(@HeaderParam(HttpHeaders.CONTENT_TYPE) String type) {
            assertEquals(MediaType.TEXT_PLAIN, type);
            return "m0";
        }

        @GET
        @Path("/f")
        @Consumes(MediaType.APPLICATION_JSON)
        public String m1(@HeaderParam(HttpHeaders.CONTENT_TYPE) String type) {
            assertEquals(MediaType.APPLICATION_JSON, type);
            return "m1";
        }

    }

    public void testConsumedMediaTypes() throws Exception {

        Resource4 resource4 = new Resource4();
        registry(resource4);

        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);

        assertEquals(200, launcher.service("GET", "/d/e", "", headers, null, null).getStatus());
        assertEquals("m0", launcher.service("GET", "/d/e", "", headers, null, null).getEntity());

        headers = new MultivaluedMapImpl();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        assertEquals(200, launcher.service("GET", "/d/f", "", headers, null, null).getStatus());
        assertEquals("m1", launcher.service("GET", "/d/f", "", headers, null, null).getEntity());

        headers = new MultivaluedMapImpl();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML);

        assertEquals(415, launcher.service("GET", "/d/f", "", headers, null, null).getStatus());

        unregistry(resource4);

    }

}
