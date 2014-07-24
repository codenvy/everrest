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

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 21 Jan 2009
 *
 * @author <a href="mailto:dmitry.kataev@exoplatform.com.ua">Dmytro Katayev</a>
 * @version $Id: HeadMethodTest.java
 */
public class HeadMethodTest extends BaseTest {

    @Path("/a")
    public static class Resource1 {

        @GET
        public String m0() {
            return "get";
        }

        @HEAD
        public String m1() {
            return "head";
        }

    }

    @Path("/b")
    public static class Resource2 {

        @GET
        public String m0() {
            return "get";
        }

    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testHeadMethod() throws Exception {
        Resource1 resource1 = new Resource1();
        registry(resource1);

        assertEquals("get", launcher.service("GET", "/a", "", null, null, null).getEntity());
        assertEquals(200, launcher.service("HEAD", "/a", "", null, null, null).getStatus());

        unregistry(resource1);

        Resource2 resource2 = new Resource2();

        registry(resource2);

        assertEquals("get", launcher.service("GET", "/b", "", null, null, null).getEntity());
        assertEquals(200, launcher.service("HEAD", "/b", "", null, null, null).getStatus());
        assertNull(launcher.service("HEAD", "/b", "", null, null, null).getEntity());

        unregistry(resource2);
    }

}
