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
package org.everrest.core.impl.method;

import org.everrest.core.impl.BaseTest;

import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 21 Jan 2009
 *
 * @author <a href="mailto:dmitry.kataev@exoplatform.com.ua">Dmytro Katayev</a>
 * @version $Id: OptionsMethodTest.java
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

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testOptionsMethod() throws Exception {
        Resource1 resource1 = new Resource1();
        registry(resource1);
        assertEquals("options", launcher.service("OPTIONS", "/a", "", null, null, null).getEntity());
        unregistry(resource1);

        Resource2 resource2 = new Resource2();
        registry(resource2);
        assertEquals(200, launcher.service("OPTIONS", "/b", "", null, null, null).getStatus());
        assertNotNull(launcher.service("OPTIONS", "/b", "", null, null, null).getResponse().getMetadata());
        unregistry(resource2);
    }

}
