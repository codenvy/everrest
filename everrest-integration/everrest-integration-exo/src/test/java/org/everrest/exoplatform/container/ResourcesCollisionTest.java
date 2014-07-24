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

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class ResourcesCollisionTest extends StandaloneBaseTest {
    private RestfulContainer restfulContainer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        restfulContainer = new RestfulContainer(container);
    }

    /** @see org.everrest.exoplatform.BaseTest#tearDown() */
    @Override
    protected void tearDown() throws Exception {
        restfulContainer.stop();
        super.tearDown();
    }

    public void testSameResource() {
        restfulContainer.registerComponentImplementation("X", X.class);
        try {
            restfulContainer.registerComponentImplementation("X", X.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
    }

    public void testResourcesWithSameURITemplate() {
        restfulContainer.registerComponentImplementation("X", X.class);
        try {
            restfulContainer.registerComponentImplementation("Y", Y.class);
            fail("PicoRegistrationException must be thrown. ");
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
