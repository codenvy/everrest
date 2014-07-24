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
package org.everrest.core.impl;

import org.everrest.core.ResourcePublicationException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class ResourceBinderTest extends BaseTest {

    /** {@inheritDoc} */
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testBind() {
        int prevSize = resources.getSize();
        resources.addResource(Resource.class, null);
        assertEquals((prevSize + 1), resources.getSize());
    }

    public void testUnbind() {
        int prevSize = resources.getSize();
        resources.addResource(Resource.class, null);
        resources.removeResource(Resource.class);
        assertEquals(prevSize, resources.getSize());
    }

    @Path("/a/b/{c}")
    public static class Resource {

        @SuppressWarnings("unused")
        @PathParam("c")
        private String pathsegm;

        public Resource() {
        }

        public Resource(@Context UriInfo uriInfo) {
        }

        @GET
        @Produces("text/html")
        public void m1() {
        }

        @GET
        @Path("d")
        @Produces("text/html")
        public void m2() {
        }

        @Path("d")
        public void m3() {
        }
    }

    //-------------------------------------

    public void testSameResourceURI() {
        int initSize = resources.getSize();
        resources.addResource(SameURIResource1.class, null);
        assertEquals(initSize + 1, resources.getSize());
        try {
            resources.addResource(SameURIResource2.class, null);
        } catch (ResourcePublicationException e) {
        }
        assertEquals(initSize + 1, resources.getSize());

        resources.removeResource(SameURIResource1.class);
        resources.addResource(SameURIResource2.class, null);
        assertEquals(initSize + 1, resources.getSize());
        try {
            resources.addResource(SameURIResource1.class, null);
        } catch (ResourcePublicationException e) {
        }
        assertEquals(initSize + 1, resources.getSize());

        resources.removeResource(SameURIResource2.class);
        resources.addResource(new SameURIResource1(), null);
        assertEquals(initSize + 1, resources.getSize());
        try {
            resources.addResource(new SameURIResource2(), null);
        } catch (ResourcePublicationException e) {
        }
        assertEquals(initSize + 1, resources.getSize());

        resources.removeResource(SameURIResource1.class);
        resources.addResource(new SameURIResource2(), null);
        assertEquals(initSize + 1, resources.getSize());
        try {
            resources.addResource(new SameURIResource1(), null);
        } catch (ResourcePublicationException e) {
        }
        assertEquals(initSize + 1, resources.getSize());
    }

    @Path("/a/b/c/{d}/e")
    public static class SameURIResource1 {
        @GET
        public void m0() {
        }
    }

    @Path("/a/b/c/{d}/e")
    public static class SameURIResource2 {
        @GET
        public void m0() {
        }
    }

}
