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
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 21 Jan 2009
 *
 * @author <a href="mailto:dmitry.kataev@exoplatform.com.ua">Dmytro Katayev</a>
 * @version $Id: TestMethodException.java
 */
public class MethodExceptionTest extends BaseTest {

    @SuppressWarnings("serial")
    public static class UncheckedException extends Exception {

        public UncheckedException() {
            super();
        }

        public UncheckedException(String msg) {
            super(msg);
        }

    }

    @Path("/a")
    public static class Resource1 {

        @GET
        @Path("/0")
        public void m0() throws WebApplicationException {
            throw new WebApplicationException();
        }

        @GET
        @Path("/1")
        public Response m1() throws WebApplicationException {
            return new WebApplicationException().getResponse();
        }

        @GET
        @Path("/2")
        public void m2() throws Exception {
            throw new UncheckedException("Unchecked exception");
        }

    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testExceptionProcessing() throws Exception {
        Resource1 resource = new Resource1();
        registry(resource);

        assertEquals(500, launcher.service("GET", "/a/0", "", null, null, null).getStatus());
        assertEquals(500, launcher.service("GET", "/a/1", "", null, null, null).getStatus());

        assertEquals(500, launcher.service("GET", "/a/2", "", null, null, null).getStatus());
        //      try
        //      {
        //         assertEquals(500, launcher.service("GET", "/a/2", "", null, null, null).getStatus());
        //         fail();
        //      }
        //      catch (UnhandledException e)
        //      {
        //      }
        unregistry(resource);
    }

    //
}
