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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.Set;

/**
 * @author andrew00x
 */
public class RequestDispatcherTest extends BaseTest {
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
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
    }

    @Path("/a")
    public static class Resource1 {
        @POST
        public String m0() {
            return "m0";
        }

        @POST
        @Path("/b")
        public String m1() {
            return "m1";
        }

        @Path("b/c")
        public SubResource1 m2() {
            return new SubResource1();
        }
    }

    public static class SubResource1 {
        @POST
        public String m0() {
            return "m2.0";
        }

        @POST
        @Path("d")
        public String m1() {
            return "m2.1";
        }

        @Path("d/e")
        public SubResource2 m2() {
            return new SubResource2();
        }
    }

    public static class SubResource2 {
        @POST
        public String m0() {
            return "m3.0";
        }

        @POST
        @Path("f")
        public String m1() {
            return "m3.1";
        }
    }

    @Test
    public void testDispatcher() throws Exception {
        Assert.assertEquals("m0", launcher.service("POST", "/a", "", null, null, null).getEntity());
        Assert.assertEquals("m1", launcher.service("POST", "/a/b", "", null, null, null).getEntity());
        Assert.assertEquals("m2.0", launcher.service("POST", "/a/b/c", "", null, null, null).getEntity());
        Assert.assertEquals("m2.1", launcher.service("POST", "/a/b/c/d", "", null, null, null).getEntity());
        Assert.assertEquals("m3.0", launcher.service("POST", "/a/b/c/d/e", "", null, null, null).getEntity());
        Assert.assertEquals("m3.1", launcher.service("POST", "/a/b/c/d/e/f", "", null, null, null).getEntity());
    }
}
