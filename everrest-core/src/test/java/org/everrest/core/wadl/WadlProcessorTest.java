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
package org.everrest.core.wadl;

import org.everrest.core.impl.resource.AbstractResourceDescriptor;
import org.everrest.core.resource.ResourceDescriptor;
import org.everrest.core.wadl.research.Application;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static javax.xml.xpath.XPathConstants.NODESET;
import static javax.xml.xpath.XPathConstants.STRING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author andrew00x
 */
public class WadlProcessorTest {

    @Path("a/{b}")
    public static class Resource1 {

        @GET
        public String m1(@Context UriInfo uriInfo) {
            return uriInfo.getAbsolutePath().toString();
        }

        @POST
        @Consumes("text/plain")
        @Produces("text/plain")
        public String m2(@HeaderParam("content-type") String contentType, String data) {
            return data;
        }

        @DELETE
        public void m3(@DefaultValue("1") @MatrixParam("id") int j) {
        }

        @PUT
        @Consumes("text/xml")
        public void m4(DOMSource ds) {
        }

        @GET
        @Path("{c}/{d}")
        public String m5(@PathParam("b") String b, @PathParam("c") String a) {
            return b;
        }

        @POST
        @Path("{c}/{d}/{e}")
        public void m6(@PathParam("c") String b, @PathParam("e") String a) {
        }

        @Path("sub/{x}")
        public Resource2 m7() {
            return new Resource2();
        }

    }

    public static class Resource2 {
        @GET
        @Produces("text/plain")
        public String m0(@PathParam("x") String x) {
            return x;
        }
    }

    @Test
    public void testBaseWadlGenerator() throws Exception {
        ResourceDescriptor ard = new AbstractResourceDescriptor(Resource1.class);
        WadlProcessor wadlProcessor = new WadlProcessor();
        Application app = wadlProcessor.process(ard, new URI("http://localhost:8080/ws/rs"));

        JAXBContext jctx = JAXBContext.newInstance(Application.class);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        jctx.createMarshaller().marshal(app, bout);
        System.out.println(new String(bout.toByteArray()));

        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        Document doc = f.newDocumentBuilder().parse(new ByteArrayInputStream(bout.toByteArray()));

        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new DummyNamespaceContext());
        String str = (String)xp.evaluate("count(//wadl:resource)", doc, STRING);
        assertEquals("4", str);
        str = (String)xp.evaluate("count(//wadl:resource[@path='a/{b}'])", doc, STRING);
        assertEquals("1", str);
        str = (String)xp.evaluate("count(//wadl:resource[@path='{c}/{d}'])", doc, STRING);
        assertEquals("1", str);
        str = (String)xp.evaluate("count(//wadl:resource[@path='{c}/{d}/{e}'])", doc, STRING);
        assertEquals("1", str);
        str = (String)xp.evaluate("count(//wadl:resource[@path='sub/{x}'])", doc, STRING);
        assertEquals("1", str);

        // discover resource methods
        str = (String)xp.evaluate("count(//wadl:resource[@path='a/{b}']/wadl:method)", doc, STRING);
        assertEquals("5", str);
        NodeList nl = (NodeList)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:method[@id='m1']/@name", doc, NODESET);
        assertEquals(1, nl.getLength());
        boolean get = false;
        for (int i = 0; i < nl.getLength(); i++) {
            String t = nl.item(i).getTextContent();
            if (t.equals("GET")) {
                get = true;
            }
        }
        assertTrue(get);
        for (int i = 0; i < nl.getLength(); i++) {
            System.out.println(">>>>> resource method : " + nl.item(i).getTextContent());
        }
        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:method[@id='m2']/@name", doc, STRING);
        assertEquals("POST", str);
        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:method[@id='m2']/wadl:request/wadl:param[@style='header']/@name", doc, STRING);
        assertEquals("content-type", str);
        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:method[@id='m3']/@name", doc, STRING);
        assertEquals("DELETE", str);
        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:method[@id='m4']/@name", doc, STRING);
        assertEquals("PUT", str);

        // discover sub-resource methods
        nl = (NodeList)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:resource[@path='{c}/{d}']/wadl:method/@name", doc, NODESET);
        assertEquals(1, nl.getLength());
        boolean subget = false;
        for (int i = 0; i < nl.getLength(); i++) {
            String t = nl.item(i).getTextContent();
            if (t.equals("GET")) {
                subget = true;
            }
        }
        assertTrue(subget);
        for (int i = 0; i < nl.getLength(); i++) {
            System.out.println(">>>>> sub-resource method : " + nl.item(i).getTextContent());
        }
        str = (String)xp.evaluate("count(//wadl:resource[@path='a/{b}']/wadl:resource[@path='{c}/{d}/{e}']/wadl:method)", doc, STRING);
        assertEquals("1", str);

        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:resource[@path='{c}/{d}']/wadl:param[@name='c']/@style", doc, STRING);
        assertEquals("template", str);
        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:resource[@path='{c}/{d}']/wadl:param[@name='b']/@style", doc, STRING);
        assertEquals("template", str);
        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:resource[@path='{c}/{d}/{e}']/wadl:param[@name='c']/@style", doc, STRING);
        assertEquals("template", str);
        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:resource[@path='{c}/{d}/{e}']/wadl:param[@name='e']/@style", doc, STRING);
        assertEquals("template", str);
        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:resource[@path='{c}/{d}/{e}']/wadl:method[@id='m6']/@name", doc, STRING);
        assertEquals("POST", str);

        // discover sub-resource locators
        nl = (NodeList)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:resource[@path='sub/{x}']/wadl:method/@name", doc, NODESET);
        assertEquals(2, nl.getLength());
        boolean childget = false;
        boolean childopt = false;
        for (int i = 0; i < nl.getLength(); i++) {
            String t = nl.item(i).getTextContent();
            if (t.equals("GET")) {
                childget = true;
            }
            if (t.equals("OPTIONS")) {
                childopt = true;
            }
        }
        assertTrue(childget && childopt);
        for (int i = 0; i < nl.getLength(); i++) {
            System.out.println(">>>>> child resource method : " + nl.item(i).getTextContent());
        }

        str = (String)xp.evaluate("count(//wadl:resource[@path='a/{b}']/wadl:resource[@path='sub/{x}']/wadl:method)", doc, STRING);
        assertEquals("2", str);
        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:resource[@path='sub/{x}']/wadl:param[@name='x']/@style", doc, STRING);
        assertEquals("template", str);
        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:resource[@path='sub/{x}']/wadl:method[@id='m0']/wadl:response/wadl:representation/@mediaType", doc, STRING);
        assertEquals("text/plain", str);
    }

    @SuppressWarnings({"unchecked"})
    private static class DummyNamespaceContext implements NamespaceContext {
        private final String   nsPrefix;
        private final String   nsUri;
        private final Iterator nsIterator;

        public DummyNamespaceContext() {
            nsPrefix = "wadl";
            nsUri = "http://research.sun.com/wadl/2006/10";
            List l = new ArrayList(1);
            l.add(nsPrefix);
            nsIterator = l.iterator();
        }

        public String getNamespaceURI(String prefix) {
            if (prefix.equals(nsPrefix)) {
                return nsUri;
            }
            return "";
        }

        public String getPrefix(String namespaceURI) {
            if (namespaceURI.equals(nsUri)) {
                return nsPrefix;
            }
            return null;
        }

        public Iterator getPrefixes(String namespaceURI) {
            if (namespaceURI.equals(nsUri)) {
                return nsIterator;
            }
            return Collections.emptyList().iterator();
        }
    }
}
