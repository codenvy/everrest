/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors:
 * Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.integration;

import org.everrest.core.Filter;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.resource.GenericResourceMethod;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;

/**
 * @author andrew00x
 */
public class MethodInvokerFilterTest extends BaseTest {

    @Filter
    public static class Filter1 implements MethodInvokerFilter {
        @Override
        public void accept(GenericResourceMethod genericResourceMethod, Object[] params) throws WebApplicationException {
            if (params[0].equals("filter me")) {
                throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity("Rejected by Filter1").build());
            }
        }
    }

    @Filter
    @Path("b")
    public static class Filter2 implements MethodInvokerFilter {
        @Override
        public void accept(GenericResourceMethod genericResourceMethod, Object[] params) {
            if (params[0].equals("filter me")) {
                throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity("Rejected by Filter2").build());
            }
        }
    }

    @Path("a")
    public static class Resource1 {
        @POST
        public void m1(String entity) {
        }
    }

    @Path("b")
    public static class Resource2 {
        @POST
        public void m1(String entity) {
        }
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        processor.addApplication(new Application() {
            @Override
            public Set<Object> getSingletons() {
                return newHashSet(new Resource1(), new Resource2(), new Filter1(), new Filter2());
            }
        });
    }

    @Test
    public void allFiltersAllowMethodInvocation() throws Exception {
        ContainerResponse response = launcher.service("POST", "/a", "", null, null, null);
        assertEquals(204, response.getStatus());
    }

    @Test
    public void generalFilterRejectsMethodInvocationAndGeneratesInternalServerErrorResponse() throws Exception {
        ContainerResponse response = launcher.service("POST", "/a", "", null, "filter me".getBytes(), null);
        assertEquals(500, response.getStatus());
        assertEquals("Rejected by Filter1", response.getEntity());
    }

    @Test
    public void pathAnnotatedFilterRejectsMethodInvocationAndGeneratesBadRequestResponse() throws Exception {
        ContainerResponse response = launcher.service("POST", "/b", "", null, "filter me".getBytes(), null);
        assertEquals(400, response.getStatus());
        assertEquals("Rejected by Filter2", response.getEntity());
    }
}
