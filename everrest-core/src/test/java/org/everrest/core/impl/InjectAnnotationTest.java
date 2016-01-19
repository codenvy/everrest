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
package org.everrest.core.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.Set;

/**
 * @author andrew00x
 */
public class InjectAnnotationTest extends BaseTest {
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        dependencySupplier.addComponent(InjectableComponent.class, new InjectableComponent());
    }

    public static class InjectableComponent {
    }

    @Path("a")
    public static class Resource1 {
        @javax.inject.Inject
        private InjectableComponent ic;

        @GET
        public void m0() {
            Assert.assertNotNull(ic);
        }
    }

    @Path("a")
    public static class Resource2 {
        @javax.inject.Inject
        private javax.inject.Provider<InjectableComponent> pic;

        @GET
        public void m0() {
            Assert.assertNotNull(pic);
            Assert.assertNotNull(pic.get());
        }
    }

    @Path("a")
    public static class Resource3 {
        @javax.inject.Inject
        private InjectableComponent injected;
        private boolean injectedThroughSetter = false;

        @GET
        public void m0() {
            Assert.assertNotNull(injected);
            Assert.assertTrue(injectedThroughSetter);
        }

        public void setInjected(InjectableComponent injected) {
            this.injectedThroughSetter = true;
            this.injected = injected;
        }
    }

    @Test
    public void testResourceInjectInstance() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.<Class<?>>singleton(Resource1.class);
            }
        });
        Assert.assertEquals(204, launcher.service("GET", "/a", "", null, null, null).getStatus());
    }

    @Test
    public void testResourceInjectProvider() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.<Class<?>>singleton(Resource2.class);
            }
        });
        Assert.assertEquals(204, launcher.service("GET", "/a", "", null, null, null).getStatus());
    }

    @Test
    public void testInjectWithSetter() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.<Class<?>>singleton(Resource3.class);
            }
        });
        Assert.assertEquals(204, launcher.service("GET", "/a", "", null, null, null).getStatus());
    }
}
