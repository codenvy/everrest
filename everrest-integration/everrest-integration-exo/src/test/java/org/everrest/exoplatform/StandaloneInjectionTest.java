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
package org.everrest.exoplatform;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author andrew00x
 */
public class StandaloneInjectionTest extends StandaloneBaseTest {
    public interface Injectable {
        String getName();
    }

    public static class InjectableImpl implements Injectable {
        @Override
        public String getName() {
            return getClass().getName();
        }
    }

    @Path("StandaloneInjectionTest.Resource1")
    public static class Resource1 {
        @javax.inject.Inject
        Injectable inj;

        @GET
        public void m0() {
            assertNotNull(inj);
            assertEquals(InjectableImpl.class.getName(), inj.getName());
        }
    }

    @Path("StandaloneInjectionTest.Resource2")
    public static class Resource2 {
        @javax.inject.Inject
        javax.inject.Provider<Injectable> pInj;

        @GET
        public void m0() {
            assertNotNull(pInj);
            Injectable inj = pInj.get();
            assertNotNull(inj);
            assertEquals(InjectableImpl.class.getName(), inj.getName());
        }
    }

   /* ------------------------------------------------------------- */

    // Implementation of this interface used as ExoContainer component.
    public interface InjectableComponent {
        String getName();
    }

    public static class InjectableComponentImpl implements InjectableComponent {
        @Override
        public String getName() {
            return getClass().getName();
        }
    }

    @Path("StandaloneInjectionTest.Resource3")
    public static class Resource3 {
        @javax.inject.Inject
        InjectableComponent inj;

        @GET
        public void m0() {
            assertNotNull(inj);
            assertEquals(InjectableComponentImpl.class.getName(), inj.getName());
        }
    }

    @Path("StandaloneInjectionTest.Resource4")
    public static class Resource4 {
        @javax.inject.Inject
        javax.inject.Provider<InjectableComponent> pInj;

        @GET
        public void m0() {
            assertNotNull(pInj);
            InjectableComponent inj = pInj.get();
            assertNotNull(inj);
            assertEquals(InjectableComponentImpl.class.getName(), inj.getName());
        }
    }

   /* ================================================================ */

    private final String injectableProviderKey = "StandaloneInjectionTest.Provider.Injectable";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        javax.inject.Provider<Injectable> provider = new javax.inject.Provider<Injectable>() {
            @Override
            public Injectable get() {
                return new InjectableImpl();
            }
        };
        container.registerComponentInstance(injectableProviderKey, provider);
        container.registerComponentInstance(InjectableComponent.class, new InjectableComponentImpl());
    }

    @After
    @Override
    public void tearDown() throws Exception {
        container.unregisterComponent(injectableProviderKey);
        container.unregisterComponent(InjectableComponent.class);
        super.tearDown();
    }

    @Test
    public void testInjectInstance() throws Exception {
        resources.addResource(Resource1.class, null);
        assertEquals(204, launcher.service("GET", "/StandaloneInjectionTest.Resource1", "", null, null, null).getStatus());
        resources.removeResource(Resource1.class);
    }

    @Test
    public void testInjectProvider() throws Exception {
        resources.addResource(Resource2.class, null);
        assertEquals(204, launcher.service("GET", "/StandaloneInjectionTest.Resource2", "", null, null, null).getStatus());
        resources.removeResource(Resource2.class);
    }

    /*
     * Test to inject instance to JAX-RS resource directly from ExoContainer.
     */
    @Test
    public void testInjectInstance2() throws Exception {
        resources.addResource(Resource3.class, null);
        assertEquals(204, launcher.service("GET", "/StandaloneInjectionTest.Resource3", "", null, null, null).getStatus());
        resources.removeResource(Resource3.class);
    }

    /*
     * Test to inject javax.inject.Provider to JAX-RS resource from ExoContainer.
     * ExoContainer does not have javax.inject.Provider for InjectableComponent
     * but if should be created because implementation of InjectableComponent
     * registered in container.
     */
    @Test
    public void testInjectProvider2() throws Exception {
        resources.addResource(Resource4.class, null);
        assertEquals(204, launcher.service("GET", "/StandaloneInjectionTest.Resource4", "", null, null, null).getStatus());
        resources.removeResource(Resource4.class);
    }
}
