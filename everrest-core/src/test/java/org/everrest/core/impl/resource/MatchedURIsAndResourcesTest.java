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
