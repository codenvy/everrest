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

import org.everrest.core.tools.DependencySupplierImpl;
import org.everrest.core.tools.ResourceLauncher;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class InjectAnnotationTest extends BaseTest {
    @Override
    public void setUp() throws Exception {
        resources = new ResourceBinderImpl();
        DependencySupplierImpl depInjector = new DependencySupplierImpl();
        depInjector.addComponent(InjectableComponent.class, new InjectableComponent());
        ProviderBinder.setInstance(new ProviderBinder());
        providers = new ApplicationProviderBinder();
        requestHandler =
                new RequestHandlerImpl(new RequestDispatcher(resources), depInjector, new EverrestConfiguration());
        launcher = new ResourceLauncher(requestHandler);
    }

    @Override
    public void tearDown() throws Exception {
    }

    public static class InjectableComponent {
    }

    @Path("a")
    public static class Resource1 {
        @javax.inject.Inject
        private InjectableComponent ic;

        @GET
        public void m0() {
            assertNotNull(ic);
        }
    }

    @Path("a")
    public static class Resource2 {
        @javax.inject.Inject
        private javax.inject.Provider<InjectableComponent> pic;

        @GET
        public void m0() {
            assertNotNull(pic);
            assertNotNull(pic.get());
        }
    }

    @Path("a")
    public static class Resource3 {
        @javax.inject.Inject
        private InjectableComponent injected;
        private boolean injectedThroughSetter = false;

        @GET
        public void m0() {
            assertNotNull(injected);
            assertTrue(injectedThroughSetter);
        }

        public void setInjected(InjectableComponent injected) {
            this.injectedThroughSetter = true;
            this.injected = injected;
        }
    }

    public void testResourceInjectInstance() throws Exception {
        registry(Resource1.class);
        assertEquals(204, launcher.service("GET", "/a", "", null, null, null).getStatus());
        unregistry(Resource1.class);
    }

    public void testResourceInjectProvider() throws Exception {
        registry(Resource2.class);
        assertEquals(204, launcher.service("GET", "/a", "", null, null, null).getStatus());
        unregistry(Resource2.class);
    }

    public void testInjectWithSetter() throws Exception {
        registry(Resource3.class);
        assertEquals(204, launcher.service("GET", "/a", "", null, null, null).getStatus());
        unregistry(Resource3.class);
    }
}
