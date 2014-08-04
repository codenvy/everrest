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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

/**
 * @author andrew00x
 */
public class GroovyIoCInjectTest extends BaseTest {

    private InputStream script;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        script = Thread.currentThread().getContextClassLoader().getResourceAsStream("a/b/GroovyResource2.groovy");
        Assert.assertNotNull(script);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        groovyPublisher.resources.clear();
        super.tearDown();
    }

    @Test
    public void testComponentPerRequest() throws Exception {
        dependencySupplier.addComponent(Component1.class, new Component1());
        iocComponentTest(false, new BaseResourceId("g1"));
    }

    @Test
    public void testComponentSingleton() throws Exception {
        dependencySupplier.addComponent(Component1.class, new Component1());
        iocComponentTest(true, new BaseResourceId("g2"));
    }

    private void iocComponentTest(boolean singleton, ResourceId resourceId) throws Exception {
        int initSize = resources.getSize();
        Assert.assertEquals(0, groovyPublisher.resources.size());

        if (singleton) {
            groovyPublisher.publishSingleton(script, resourceId, null, null, null);
        } else {
            groovyPublisher.publishPerRequest(script, resourceId, null, null, null);
        }

        Assert.assertEquals(initSize + 1, resources.getSize());
        Assert.assertEquals(1, groovyPublisher.resources.size());

        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse resp = launcher.service("GET", "/a/b", "", null, null, writer, null);
        Assert.assertEquals(200, resp.getStatus());
        Assert.assertEquals("ioc component", new String(writer.getBody()));
    }

    public static class Component1 {
        public String getName() {
            return "ioc component";
        }
    }
}
