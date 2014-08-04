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
package org.everrest.groovy;

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import java.io.InputStream;

/**
 * @author andrew00x
 */
public class GroovySecureRestrictionTest extends BaseTest {

    private InputStream script;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        assertNotNull("SecurityManager not installed", System.getSecurityManager());
        script = Thread.currentThread().getContextClassLoader().getResourceAsStream("a/b/GroovyResource3.groovy");
        assertNotNull(script);
    }

    public void testReadSystemPropertyFail() throws Exception {
        groovyPublisher.publishPerRequest(script, new BaseResourceId("g1"), null, null, null);
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse resp = launcher.service("GET", "/a/b", "", null, null, writer, null);
        assertEquals(500, resp.getStatus());
        assertTrue(new String(writer.getBody()).startsWith("access denied"));
    }
}
