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
package org.everrest.core.impl.integration;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.provider.TemplatesParser;
import org.everrest.core.impl.provider.XSLTStreamingOutput;
import org.everrest.core.impl.provider.XSLTTemplatesContextResolver;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import javax.xml.transform.stream.StreamSource;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_HTML_TYPE;
import static org.junit.Assert.assertEquals;

/**
 * @author andrew00x
 */
public class XSLTTransformationTest extends BaseTest {

    @Path("a")
    public static class Resource1 {
        @Context
        private Providers providers;

        @GET
        public Response m0() {
            ContextResolver<XSLTTemplatesContextResolver> resolverWrapper = providers.getContextResolver(XSLTTemplatesContextResolver.class, APPLICATION_XML_TYPE);
            XSLTTemplatesContextResolver resolver = resolverWrapper.getContext(null);
            return Response.ok()
                           .type(TEXT_HTML_TYPE)
                           .entity(new XSLTStreamingOutput(loadSource("book-in.xml"), resolver.getTemplates("template1")))
                           .build();
        }
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        XSLTTemplatesContextResolver resolver = new XSLTTemplatesContextResolver(new TemplatesParser());
        resolver.addAsTemplate("template1", loadSource("xslt/book.xsl"));

        processor.addApplication(new Application() {
            @Override
            public Set<Object> getSingletons() {
                return newHashSet(resolver);
            }

            @Override
            public Set<Class<?>> getClasses() {
                return newHashSet(Resource1.class);
            }
        });
    }

    @Test
    public void transformsXmlToHtml() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service("GET", "/a", "", null, null, writer, null);
        assertEquals(200, response.getStatus());
        assertEquals(TEXT_HTML_TYPE, response.getContentType());
    }

    private static StreamSource loadSource(String resource) {
        return new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream(resource));
    }
}
