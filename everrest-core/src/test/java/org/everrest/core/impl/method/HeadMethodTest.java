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
package org.everrest.core.impl.method;

import org.everrest.core.impl.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.Set;

/**
 * @author Dmytro Katayev
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

    @Test
    public void testHeadMethod() throws Exception {
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
        Assert.assertEquals("get", launcher.service("GET", "/a", "", null, null, null).getEntity());
        Assert.assertEquals(200, launcher.service("HEAD", "/a", "", null, null, null).getStatus());
    }

    @Test
    public void testHeadMethodForGetMethod() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource2());
            }
        });
        Assert.assertEquals("get", launcher.service("GET", "/b", "", null, null, null).getEntity());
        Assert.assertEquals(200, launcher.service("HEAD", "/b", "", null, null, null).getStatus());
        Assert.assertNull(launcher.service("HEAD", "/b", "", null, null, null).getEntity());
    }
}
