/**
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.everrest.core.impl.resource;

import org.everrest.core.ExtHttpHeaders;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.RequestHandlerImpl;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class XHTTPMethodOverrideTest extends BaseTest {

    @Path("/a")
    public static class Resource1 {
        @POST
        public String m0() {
            return "m0";
        }
    }

    public void setUp() throws Exception {
        super.setUp();
        registry(Resource1.class);
    }

    public void tearDown() throws Exception {
        super.tearDown();
        unregistry(Resource1.class);
    }

    public void testNoOverride() throws Exception {
        // Provide GET instead of POST - method not allowed response
        assertEquals(405, launcher.service("GET", "/a", "", null, null, null).getStatus());
    }

    public void testOverride() throws Exception {
        MultivaluedMapImpl headers = new MultivaluedMapImpl();
        headers.putSingle(ExtHttpHeaders.X_HTTP_METHOD_OVERRIDE, "POST");
        RequestHandlerImpl.setProperty("org.everrest.x-http-method-override", "true");
        ContainerResponse response = launcher.service("GET", "/a", "", headers, null, null);
        assertEquals(200, response.getStatus());
        assertEquals("m0", response.getEntity());
    }
}
