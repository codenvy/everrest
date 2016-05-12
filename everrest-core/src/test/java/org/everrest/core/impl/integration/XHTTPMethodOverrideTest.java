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
package org.everrest.core.impl.integration;

import org.everrest.core.ExtHttpHeaders;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;

/**
 * @author andrew00x
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
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return newHashSet(Resource1.class);
            }
        });
    }

    @Test
    public void doesNotOverrideHttpMethod() throws Exception {
        assertEquals(405, launcher.service("GET", "/a", "", null, null, null).getStatus());
    }

    @Test
    public void overridesHttpMethod() throws Exception {
        MultivaluedMapImpl headers = new MultivaluedMapImpl();
        headers.putSingle(ExtHttpHeaders.X_HTTP_METHOD_OVERRIDE, "POST");
        ContainerResponse response = launcher.service("GET", "/a", "", headers, null, null);
        assertEquals(200, response.getStatus());
        assertEquals("m0", response.getEntity());
    }
}
