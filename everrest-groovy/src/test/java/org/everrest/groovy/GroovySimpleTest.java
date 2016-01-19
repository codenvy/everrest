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
package org.everrest.groovy;

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

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
        Assert.assertEquals(0, groovyPublisher.resources.size());

        if (singleton) {
            groovyPublisher.publishSingleton(script, resourceId, null, null, null);
        } else {
            groovyPublisher.publishPerRequest(script, resourceId, null, null, null);
        }

        Assert.assertEquals(initSize + 1, resources.getSize());
        Assert.assertEquals(1, groovyPublisher.resources.size());
        Assert.assertTrue(groovyPublisher.isPublished(resourceId));

        String cs = resources.getMatchedResource("/a", new ArrayList<String>()).getObjectModel().getObjectClass().getProtectionDomain()
                             .getCodeSource().getLocation().toString();
        Assert.assertEquals("file:/groovy/script/jaxrs", cs);

        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse resp = launcher.service("GET", "/a/groovy", "", null, null, writer, null);
        Assert.assertEquals(200, resp.getStatus());
        Assert.assertEquals("hello groovy", new String(writer.getBody()));

        groovyPublisher.unpublishResource(resourceId);

        Assert.assertEquals(initSize, resources.getSize());
        Assert.assertEquals(0, groovyPublisher.resources.size());
    }
}
