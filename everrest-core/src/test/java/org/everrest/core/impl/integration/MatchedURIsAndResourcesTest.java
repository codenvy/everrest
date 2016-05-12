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
package org.everrest.core.impl.integration;

import org.everrest.core.impl.BaseTest;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

public class MatchedURIsAndResourcesTest extends BaseTest {

    @Path("/a/b")
    public static class Resource1 {
        @GET
        @Path("1")
        public String m0(@Context UriInfo uriInfo) {
            return uriInfo.getMatchedURIs().stream().collect(joining(","));
        }

        @GET
        @Path("2")
        public String m1(@Context UriInfo uriInfo) {
            return uriInfo.getMatchedResources().stream().map(o -> o.getClass().getSimpleName()).collect(joining(","));
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
            return uriInfo.getMatchedURIs().stream().collect(joining(","));
        }

        @GET
        @Path("2")
        public String m1(@Context UriInfo uriInfo) {
            return uriInfo.getMatchedResources().stream().map(o -> o.getClass().getSimpleName()).collect(joining(","));
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
            return uriInfo.getMatchedURIs().stream().collect(joining(","));
        }

        @GET
        @Path("2")
        public String m1(@Context UriInfo uriInfo) {
            return uriInfo.getMatchedResources().stream().map(o -> o.getClass().getSimpleName()).collect(joining(","));
        }
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        processor.addApplication(new Application() {
            @Override
            public Set<Object> getSingletons() {
                return newHashSet(new Resource1());
            }
        });
    }

    @Test
    public void testResource() throws Exception {
        assertEquals("/1,/a/b", launcher.service("GET", "http://localhost/test/a/b/1", "http://localhost/test", null,
                                                 null, null).getEntity());
        assertEquals("Resource1", launcher.service("GET", "http://localhost/test/a/b/2", "http://localhost/test", null,
                                                   null, null).getEntity());
    }

    @Test
    public void testSubResource() throws Exception {
        assertEquals("/1,/sub,/a/b", launcher.service("GET", "http://localhost/test/a/b/sub/1", "http://localhost/test",
                                                      null, null, null).getEntity());
        assertEquals("SubResource1,Resource1", launcher.service("GET", "http://localhost/test/a/b/sub/2",
                                                                "http://localhost/test", null, null, null).getEntity());
    }

    @Test
    public void testSubSubResource() throws Exception {
        assertEquals("/1,/sub-sub,/sub,/a/b", launcher.service("GET", "http://localhost/test/a/b/sub/sub-sub/1",
                                                               "http://localhost/test", null, null, null).getEntity());
        assertEquals("SubResource2,SubResource1,Resource1", launcher.service("GET",
                                                                             "http://localhost/test/a/b/sub/sub-sub/2",
                                                                             "http://localhost/test", null, null, null).getEntity());
    }
}
