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
 * @author Max Shaposhnik
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
    }

    @Test
    public void testExceptionMessage() throws Exception {
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

        Assert.assertEquals(500, launcher.service("GET", "/a/1", "", null, null, null).getStatus());
        Assert.assertEquals(null, launcher.service("GET", "/a/1", "", null, null, null).getEntity());

        Assert.assertEquals(500, launcher.service("GET", "/a/0", "", null, null, null).getStatus());
        String entity = (String)launcher.service("GET", "/a/0", "", null, null, null).getEntity();
        Assert.assertTrue(entity.indexOf("testmsg") > 0);
    }
}
