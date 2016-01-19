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
package org.everrest.core.impl.method;

import org.everrest.core.impl.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.Set;

/**
 * @author Dmytro Katayev
 */
public class OptionsMethodTest extends BaseTest {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @HttpMethod("OPTIONS")
    public @interface OPTIONS {
    }

    @Path("/a")
    public static class Resource1 {
        @OPTIONS
        public String m0() {
            return "options";
        }
    }

    @Path("/b")
    public static class Resource2 {
        @GET
        public String m0() {
            return "get";
        }
    }

    @Test
    public void testOptionsMethod() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource2());
            }
        });
        Assert.assertEquals(200, launcher.service("OPTIONS", "/b", "", null, null, null).getStatus());
        Assert.assertNotNull(launcher.service("OPTIONS", "/b", "", null, null, null).getResponse().getEntity());
    }

    @Test
    public void testOptionsMethodCustom() throws Exception {
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
        Assert.assertEquals("options", launcher.service("OPTIONS", "/a", "", null, null, null).getEntity());
    }
}
