/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.groovy;

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author andrew00x
 */
public class GroovySecureRestrictionTest extends BaseTest {

    private InputStream script;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        assertNotNull("SecurityManager not installed", System.getSecurityManager());
        script = Thread.currentThread().getContextClassLoader().getResourceAsStream("a/b/GroovyResource3.groovy");
        assertNotNull(script);
    }

    @Test
    public void testReadSystemPropertyFail() throws Exception {
        groovyPublisher.publishPerRequest(script, new BaseResourceId("g1"), null, null, null);
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse resp = launcher.service("GET", "/a/b", "", null, null, writer, null);
        assertEquals(500, resp.getStatus());
        assertTrue(new String(writer.getBody()).startsWith("access denied"));
    }
}
