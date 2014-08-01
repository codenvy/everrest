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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 23 Jan 2009
 *
 * @author <a href="mailto:dmitry.kataev@exoplatform.com.ua">Dmytro Katayev</a>
 * @version $Id: AnnotationInheritanceTest.java
 */
public class AnnotationInheritanceTest extends BaseTest {

    @Produces(MediaType.TEXT_XML)
    public static interface ResourceInterface {
        @GET
        String m0(String type);
    }

    @Path("/a")
    public static class Resource1 implements ResourceInterface {
        public String m0(String type) {
            return "m0";
        }
    }

    @Path("/a")
    public static class Resource2 implements ResourceInterface {
        @Produces(MediaType.APPLICATION_ATOM_XML)
        public String m0(String type) {
            return "m0";
        }
    }

    //

    public static interface ResourceInterface1 {
        @GET
        void m0();
    }

    public static interface ResourceInterface2 {
        @GET
        void m0();
    }

    @Path("a")
    public static class Resource3 implements ResourceInterface1, ResourceInterface2 {
        public void m0() {
        }
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testFailedInheritance() {
        try {
            new AbstractResourceDescriptorImpl(Resource3.class);
            fail("Should be failed here, equivocality annotation on method m0");
        } catch (RuntimeException e) {
        }
    }

    public void testAnnotationsInheritance() throws Exception {
        Resource1 resource1 = new Resource1();
        Resource2 resource2 = new Resource2();

        registry(resource1);

        assertEquals(200, launcher.service("GET", "/a", "", null, null, null).getStatus());
        assertEquals("m0", launcher.service("GET", "/a", "", null, null, null).getEntity());
        assertEquals(MediaType.TEXT_XML_TYPE, launcher.service("GET", "/a", "", null, null, null).getContentType());

        unregistry(resource1);

        registry(resource2);
        assertEquals(200, launcher.service("GET", "/a", "", null, null, null).getStatus());
        assertEquals("m0", launcher.service("GET", "/a", "", null, null, null).getEntity());
        assertEquals(MediaType.APPLICATION_ATOM_XML_TYPE, launcher.service("GET", "/a", "", null, null, null)
                                                                  .getContentType());
        unregistry(resource2);

    }

}
