/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
