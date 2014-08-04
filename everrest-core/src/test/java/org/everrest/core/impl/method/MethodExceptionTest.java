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
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Set;

/**
 * @author Dmytro Katayev
 */
public class MethodExceptionTest extends BaseTest {

    @SuppressWarnings("serial")
    public static class MyException extends Exception {
        public MyException() {
            super();
        }

        public MyException(String msg) {
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
            throw new MyException("My exception");
        }
    }

    @Test
    public void testExceptionProcessing() throws Exception {
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

        Assert.assertEquals(500, launcher.service("GET", "/a/0", "", null, null, null).getStatus());
        Assert.assertEquals(500, launcher.service("GET", "/a/1", "", null, null, null).getStatus());
        Assert.assertEquals(500, launcher.service("GET", "/a/2", "", null, null, null).getStatus());
    }
}
