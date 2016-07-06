/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors:
 * Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.integration;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Dmytro Katayev
 */
public class OPTIONSMethodTest extends BaseTest {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @HttpMethod("OPTIONS")
    public @interface OPTIONS {
    }

    @Path("/a")
    public static class Resource_with_OPTIONS_method {
        @OPTIONS
        public String m0() {
            return "options";
        }
    }

    @Path("/b")
    public static class Resource_without_OPTIONS_method {
        @GET
        public String m0() {
            return "get";
        }
    }

    @Test
    public void whenOPTIONSMethodIsNotAvailableThenWADLResponseIsProvided() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Object> getSingletons() {
                return newHashSet(new Resource_without_OPTIONS_method());
            }
        });

        ContainerResponse response = launcher.service("OPTIONS", "/b", "", null, null, null);

        assertEquals(200, response.getStatus());
        assertEquals(new MediaType("application", "vnd.sun.wadl+xml"), response.getContentType());
        assertNotNull(response.getResponse().getEntity());
    }

    @Test
    public void whenOPTIONSMethodIsAvailableUseIt() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Object> getSingletons() {
                return newHashSet(new Resource_with_OPTIONS_method());
            }
        });

        ContainerResponse response = launcher.service("OPTIONS", "/a", "", null, null, null);

        assertEquals(200, response.getStatus());
        assertEquals("options", response.getEntity());
    }
}
