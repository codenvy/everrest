/**
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.everrest.core.impl.provider;

import org.everrest.core.impl.ApplicationPublisher;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

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
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class XSLTTransformationTest extends BaseTest {

    public static class Application0 extends javax.ws.rs.core.Application {
        private final Set<Class<?>> classes = new HashSet<Class<?>>();

        private final Set<Object> objects = new HashSet<Object>();

        public Application0() throws Exception {
            XSLTTemplatesContextResolver resolver = new XSLTTemplatesContextResolver();
            resolver.addAsTemplate("test.template", new StreamSource(Thread.currentThread().getContextClassLoader()
                                                                           .getResourceAsStream("xslt/book.xsl")));
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


    public void testTransformToHtml() throws Exception {
        ApplicationPublisher deployer = new ApplicationPublisher(resources, providers);
        deployer.publish(new Application0());
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service("GET", "/a", "", null, null, writer, null);
        assertEquals(200, response.getStatus());
        System.out.println(new String(writer.getBody()));
    }

}
