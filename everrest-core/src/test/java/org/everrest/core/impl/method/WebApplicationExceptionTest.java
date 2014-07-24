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
import org.everrest.core.impl.method.MethodExceptionTest.UncheckedException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 24 Dec 2009
 *
 * @author <a href="mailto:max.shaposhnik@exoplatform.com">Max Shaposhnik</a>
 * @version $Id: WebApplicationExceptionTest.java
 */
public class WebApplicationExceptionTest extends BaseTest {

    @Path("/a")
    public static class Resource1 {

        @GET
        @Path("/0")
        public void m0() throws WebApplicationException {
            Exception e = new Exception("testmsg");
            throw new WebApplicationException(e, 500);
        }

        @GET
        @Path("/1")
        public Response m1() throws WebApplicationException {
            throw new WebApplicationException(500);
        }

        @GET
        @Path("/2")
        public void m2() throws Exception {
            throw new UncheckedException("Unchecked exception");
        }

    }

    public void testExceptionMessage() throws Exception {
        Resource1 resource = new Resource1();
        registry(resource);

        assertEquals(500, launcher.service("GET", "/a/0", "", null, null, null).getStatus());
        String entity = (String)launcher.service("GET", "/a/0", "", null, null, null).getEntity();
        assertTrue(entity.indexOf("testmsg") > 0);

        assertEquals(500, launcher.service("GET", "/a/1", "", null, null, null).getStatus());
        assertEquals(null, launcher.service("GET", "/a/1", "", null, null, null).getEntity());
        unregistry(resource);
    }

}
