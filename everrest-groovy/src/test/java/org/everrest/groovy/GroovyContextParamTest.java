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

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: GroovyContextParamTest.java 2647 2010-06-17 08:39:29Z aparfonov
 *          $
 */
public class GroovyContextParamTest extends BaseTest {

    private InputStream script;

    public void setUp() throws Exception {
        super.setUp();
        script = Thread.currentThread().getContextClassLoader().getResourceAsStream("a/b/GroovyResource1.groovy");
        assertNotNull(script);
    }

    @Override
    public void tearDown() throws Exception {
        groovyPublisher.resources.clear();
        super.tearDown();
    }

    public void testPerRequest() throws Exception {
        int initSize = resources.getSize();
        assertEquals(0, groovyPublisher.resources.size());

        groovyPublisher.publishPerRequest(script, new BaseResourceId("g1"), null, null, null);

        assertEquals(initSize + 1, resources.getSize());
        assertEquals(1, groovyPublisher.resources.size());

        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

        EnvironmentContext envctx = new EnvironmentContext();

        HttpServletRequest httpRequest =
                new MockHttpServletRequest("http://localhost:8080/context/a/b", null, 0, "GET", null);
        envctx.put(HttpServletRequest.class, httpRequest);

        ContainerResponse resp =
                launcher.service("GET", "http://localhost:8080/context/a/b", "http://localhost:8080/context", null, null,
                                 writer, envctx);
        assertEquals(200, resp.getStatus());
        assertEquals("GET\n/context/a/b", new String(writer.getBody()));
    }

}
