/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
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
import org.everrest.core.impl.ContainerResponse;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;

/**
 * @author Max Shaposhnik
 */
public class WebApplicationExceptionProcessingTest extends BaseTest {

    @Path("/a")
    public static class Resource {
        @GET
        @Path("/0")
        public void m0() throws WebApplicationException {
            Exception e = new Exception("error message");
            throw new WebApplicationException(e, 500);
        }

        @GET
        @Path("/1")
        public Response m1() throws WebApplicationException {
            throw new WebApplicationException(500);
        }

        @GET
        @Path("2")
        public void m2() throws WebApplicationException {
            Response response = Response.status(500).entity("error response message").type("text/plain").build();
            throw new WebApplicationException(new Exception(), response);
        }
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        processor.addApplication(new Application() {
            @Override
            public Set<Object> getSingletons() {
                return newHashSet(new Resource());
            }
        });
    }

    @Test
    public void whenWebApplicationExceptionDoesNotHaveResponseThenStatusMessageInResponseEntity() throws Exception {
        ContainerResponse response = launcher.service("GET", "/a/1", "", null, null, null);
        assertEquals(500, response.getStatus());
        assertEquals("HTTP 500 Internal Server Error", response.getEntity());
    }

    @Test
    public void whenWebApplicationExceptionHasResponseThenSendIt() throws Exception {
        ContainerResponse response = launcher.service("GET", "/a/2", "", null, null, null);
        assertEquals(500, response.getStatus());
        assertEquals("error response message", response.getEntity());
    }
}
