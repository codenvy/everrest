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
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.everrest.test.mock.MockHttpServletRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

/**
 * @author andrew00x
 */
public class GroovyContextParamTest extends BaseTest {

    private InputStream script;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        script = Thread.currentThread().getContextClassLoader().getResourceAsStream("a/b/GroovyResource1.groovy");
        Assert.assertNotNull(script);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        groovyPublisher.resources.clear();
        super.tearDown();
    }

    @Test
    public void testPerRequest() throws Exception {
        int initSize = resources.getSize();
        Assert.assertEquals(0, groovyPublisher.resources.size());

        groovyPublisher.publishPerRequest(script, new BaseResourceId("g1"), null, null, null);

        Assert.assertEquals(initSize + 1, resources.getSize());
        Assert.assertEquals(1, groovyPublisher.resources.size());

        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

        EnvironmentContext envctx = new EnvironmentContext();

        HttpServletRequest httpRequest = new MockHttpServletRequest("http://localhost:8080/context/a/b", null, 0, "GET", null);
        envctx.put(HttpServletRequest.class, httpRequest);

        ContainerResponse resp =
                launcher.service("GET", "http://localhost:8080/context/a/b", "http://localhost:8080/context", null, null, writer, envctx);
        Assert.assertEquals(200, resp.getStatus());
        Assert.assertEquals("GET\n/context/a/b", new String(writer.getBody()));
    }
}
