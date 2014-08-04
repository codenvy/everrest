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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Dmytro Katayev
 */
public class MethodReturnTypeTest extends BaseTest {

    @Path("/a")
    public static class Resource1 {
        @GET
        @Path("/0")
        public void m0() {
        }

        @GET
        @Path("/1")
        public Response m1() {
            // will return Response with 200 (ok) status code
            return Response.ok("body").build();
        }

        @GET
        @Path("/2")
        public Response m2() {
            return null;
        }

        @GET
        @Path("/3")
        public GenericEntity<List<String>> m3() {
            return null;
        }

        @GET
        @Path("/5")
        public String m5() {
            return null;
        }

        @GET
        @Path("/6")
        public String m6() {
            return "body";
        }
    }

    @Before
    @Override
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
    public void testResourceMethodReturnTypeVoid() throws Exception {
        Assert.assertEquals(204, launcher.service("GET", "/a/0", "", null, null, null).getStatus());
        Assert.assertNull(launcher.service("GET", "/a/0", "", null, null, null).getEntity());
    }

    @Test
    public void testResourceMethodReturnTypeResponse() throws Exception {
        Assert.assertEquals(200, launcher.service("GET", "/a/1", "", null, null, null).getStatus());
        Assert.assertEquals("body", launcher.service("GET", "/a/1", "", null, null, null).getEntity());
    }

    @Test
    public void testResourceMethodReturnTypeResponseNull() throws Exception {
        Assert.assertEquals(204, launcher.service("GET", "/a/2", "", null, null, null).getStatus());
        Assert.assertNull(launcher.service("GET", "/a/2", "", null, null, null).getEntity());
    }

    @Test
    public void testResourceMethodReturnTypeGenericEntity() throws Exception {
        Assert.assertEquals(204, launcher.service("GET", "/a/3", "", null, null, null).getStatus());
        Assert.assertNull(launcher.service("GET", "/a/3", "", null, null, null).getEntity());
    }

    @Test
    public void testResourceMethodReturnTypeOtherNull() throws Exception {
        Assert.assertEquals(204, launcher.service("GET", "/a/5", "", null, null, null).getStatus());
        Assert.assertNull(launcher.service("GET", "/a/5", "", null, null, null).getEntity());
    }

    @Test
    public void testResourceMethodReturnTypeOther() throws Exception {
        Assert.assertEquals(200, launcher.service("GET", "/a/6", "", null, null, null).getStatus());
        Assert.assertNotNull(launcher.service("GET", "/a/6", "", null, null, null).getEntity());
    }
}
