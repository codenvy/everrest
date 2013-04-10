/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.core.impl.resource;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.header.HeaderHelper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: MatchedURIsAndResourcesTest.java 497 2009-11-08 13:19:25Z
 *          aparfonov $
 */
public class MatchedURIsAndResourcesTest extends BaseTest {

    @Path("/a/b")
    public static class Resource1 {
        @GET
        @Path("1")
        public String m0(@Context UriInfo uriInfo) {
            return HeaderHelper.convertToString(uriInfo.getMatchedURIs());
        }

        @GET
        @Path("2")
        public String m1(@Context UriInfo uriInfo) {
            List<String> l = new ArrayList<String>();
            for (Object o : uriInfo.getMatchedResources())
                l.add(o.getClass().getSimpleName());
            return HeaderHelper.convertToString(l);
        }

        @Path("sub")
        public SubResource1 m2() {
            return new SubResource1();
        }

    }

    public static class SubResource1 {
        @GET
        @Path("1")
        public String m0(@Context UriInfo uriInfo) {
            return HeaderHelper.convertToString(uriInfo.getMatchedURIs());
        }

        @GET
        @Path("2")
        public String m1(@Context UriInfo uriInfo) {
            List<String> l = new ArrayList<String>();
            for (Object o : uriInfo.getMatchedResources())
                l.add(o.getClass().getSimpleName());
            return HeaderHelper.convertToString(l);
        }

        @Path("sub-sub")
        public SubResource2 m2() {
            return new SubResource2();
        }
    }

    public static class SubResource2 {
        @GET
        @Path("1")
        public String m0(@Context UriInfo uriInfo) {
            return HeaderHelper.convertToString(uriInfo.getMatchedURIs());
        }

        @GET
        @Path("2")
        public String m1(@Context UriInfo uriInfo) {
            List<String> l = new ArrayList<String>();
            for (Object o : uriInfo.getMatchedResources())
                l.add(o.getClass().getSimpleName());
            return HeaderHelper.convertToString(l);
        }
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testLevel1() throws Exception {
        Resource1 r1 = new Resource1();
        registry(r1);
        assertEquals("/1,/a/b", launcher.service("GET", "http://localhost/test/a/b/1", "http://localhost/test", null,
                                                 null, null).getEntity());
        assertEquals("Resource1", launcher.service("GET", "http://localhost/test/a/b/2", "http://localhost/test", null,
                                                   null, null).getEntity());
        unregistry(r1);
    }

    public void testLevel2() throws Exception {
        Resource1 r1 = new Resource1();
        registry(r1);
        assertEquals("/1,/sub,/a/b", launcher.service("GET", "http://localhost/test/a/b/sub/1", "http://localhost/test",
                                                      null, null, null).getEntity());
        assertEquals("SubResource1,Resource1", launcher.service("GET", "http://localhost/test/a/b/sub/2",
                                                                "http://localhost/test", null, null, null).getEntity());
        unregistry(r1);
    }

    public void testLevel3() throws Exception {
        Resource1 r1 = new Resource1();
        registry(r1);
        assertEquals("/1,/sub-sub,/sub,/a/b", launcher.service("GET", "http://localhost/test/a/b/sub/sub-sub/1",
                                                               "http://localhost/test", null, null, null).getEntity());
        assertEquals("SubResource2,SubResource1,Resource1", launcher.service("GET",
                                                                             "http://localhost/test/a/b/sub/sub-sub/2",
                                                                             "http://localhost/test", null, null, null).getEntity());
        unregistry(r1);
    }
}
