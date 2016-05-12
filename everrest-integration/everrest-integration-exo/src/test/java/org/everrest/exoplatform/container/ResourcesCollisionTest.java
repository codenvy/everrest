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
package org.everrest.exoplatform.container;

import org.everrest.exoplatform.StandaloneBaseTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author andrew00x
 */
// TODO : refactor!!
public class ResourcesCollisionTest extends StandaloneBaseTest {
    private RestfulContainer restfulContainer;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        restfulContainer = new RestfulContainer(container);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        restfulContainer.stop();
        super.tearDown();
    }

    @Test
    public void testSameResource() {
        restfulContainer.registerComponentImplementation("X", X.class);
        try {
            restfulContainer.registerComponentImplementation("X", X.class);
            Assert.fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
    }

    @Test
    public void testResourcesWithSameURITemplate() {
        restfulContainer.registerComponentImplementation("X", X.class);
        try {
            restfulContainer.registerComponentImplementation("Y", Y.class);
            Assert.fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
    }

    @Path("x/{value-x}")
    public static class X {
        @GET
        public void a() {
        }
    }

    @Path("x/{value-y}")
    public static class Y {
        @GET
        public void a() {
        }
    }
}
