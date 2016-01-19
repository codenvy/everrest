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
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;

/**
 * @author andrew00x
 */
public class GroovyDependenciesTest extends BaseTest {
    private InputStream script;
    private URL         root;
    private URL         file;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        root = Thread.currentThread().getContextClassLoader().getResource("repo");
        file = Thread.currentThread().getContextClassLoader().getResource("repo/dependencies/GDependency1.groovy");
        script = Thread.currentThread().getContextClassLoader().getResourceAsStream("a/b/GMain1.groovy");
        Assert.assertNotNull(script);
    }

    @Test
    public void testDependencyFolder() throws Exception {
        groovyPublisher.publishPerRequest(script, new BaseResourceId("GMain1"), null, new SourceFolder[]{new SourceFolder(root)}, null);
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse resp = launcher.service("GET", "/a", "", null, null, writer, null);
        Assert.assertEquals(200, resp.getStatus());
        Assert.assertEquals("dependencies.GDependency1", new String(writer.getBody()));
    }

    @Test
    public void testDependencyFile() throws Exception {
        groovyPublisher.publishPerRequest(script, new BaseResourceId("GMain1"), null, null, new SourceFile[]{new SourceFile(file)});
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse resp = launcher.service("GET", "/a", "", null, null, writer, null);
        Assert.assertEquals(200, resp.getStatus());
        Assert.assertEquals("dependencies.GDependency1", new String(writer.getBody()));
    }
}
