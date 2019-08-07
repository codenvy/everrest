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
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author andrew00x
 */
public class GroovySimpleTest extends BaseTest {

    @Override
    public void tearDown() throws Exception {
        groovyPublisher.resources.clear();
        super.tearDown();
    }

    @Test
    public void testPerRequest() throws Exception {
        publicationTest(false, new BaseResourceId("g1"));
    }

    @Test
    public void testSingleton() throws Exception {
        publicationTest(true, new BaseResourceId("g2"));
    }

    private void publicationTest(boolean singleton, ResourceId resourceId) throws Exception {
        String script = //
                "@javax.ws.rs.Path(\"a\")" //
                + "class GroovyResource {" //
                + "@javax.ws.rs.GET @javax.ws.rs.Path(\"{who}\")" //
                + "def m0(@javax.ws.rs.PathParam(\"who\") String who) { return (\"hello \" + who)}" //
                + "}";

        int initSize = resources.getSize();
        assertEquals(0, groovyPublisher.resources.size());

        if (singleton) {
            groovyPublisher.publishSingleton(script, resourceId, null, null, null);
        } else {
            groovyPublisher.publishPerRequest(script, resourceId, null, null, null);
        }

        assertEquals(initSize + 1, resources.getSize());
        assertEquals(1, groovyPublisher.resources.size());
        assertTrue(groovyPublisher.isPublished(resourceId));

        String cs = resources.getMatchedResource("/a", new ArrayList<>()).getObjectModel().getObjectClass().getProtectionDomain()
                             .getCodeSource().getLocation().toString();
        assertEquals("file:/groovy/script/jaxrs", cs);

        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse resp = launcher.service("GET", "/a/groovy", "", null, null, writer, null);
        assertEquals(200, resp.getStatus());
        assertEquals("hello groovy", new String(writer.getBody()));

        groovyPublisher.unpublishResource(resourceId);

        assertEquals(initSize, resources.getSize());
        assertEquals(0, groovyPublisher.resources.size());
    }
}
