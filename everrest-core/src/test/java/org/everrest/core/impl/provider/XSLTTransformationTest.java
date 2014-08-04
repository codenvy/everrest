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
package org.everrest.core.impl.provider;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamSource;
import java.util.HashSet;
import java.util.Set;

/**
 * @author andrew00x
 */
public class XSLTTransformationTest extends BaseTest {

    public static class Application0 extends javax.ws.rs.core.Application {
        private final Set<Class<?>> classes = new HashSet<Class<?>>();

        private final Set<Object> objects = new HashSet<Object>();

        public Application0() throws Exception {
            XSLTTemplatesContextResolver resolver = new XSLTTemplatesContextResolver();
            resolver.addAsTemplate("test.template", new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    "xslt/book.xsl")));
            objects.add(resolver);
            classes.add(Resource0.class);
        }

        @Override
        public Set<Class<?>> getClasses() {
            return classes;
        }

        @Override
        public Set<Object> getSingletons() {
            return objects;
        }
    }

    @Path("a")
    public static class Resource0 {
        @Context
        private Providers providers;

        @GET
        public StreamingOutput m0() {
            ContextResolver<XSLTTemplatesContextResolver> resolverWrapper =
                    providers.getContextResolver(XSLTTemplatesContextResolver.class, MediaType.APPLICATION_XML_TYPE);
            XSLTTemplatesContextResolver resolver = resolverWrapper.getContext(null);
            Templates templates = resolver.getTemplates("test.template");
            return new XSLTStreamingOutput(new StreamSource(Thread.currentThread().getContextClassLoader()
                                                                  .getResourceAsStream("book-in.xml")), templates);
        }
    }

    @Test
    public void testTransformToHtml() throws Exception {
        processor.addApplication(new Application0());
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service("GET", "/a", "", null, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        System.out.println(new String(writer.getBody()));
    }
}
