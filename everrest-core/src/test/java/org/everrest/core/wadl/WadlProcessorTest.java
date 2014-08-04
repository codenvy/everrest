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
package org.everrest.core.wadl;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.resource.AbstractResourceDescriptorImpl;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.wadl.research.Application;
import org.junit.Assert;
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
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author andrew00x
 */
public class WadlProcessorTest extends BaseTest {

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
        AbstractResourceDescriptor ard = new AbstractResourceDescriptorImpl(Resource1.class);
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
        String str = (String)xp.evaluate("count(//wadl:resource)", doc, XPathConstants.STRING);
        Assert.assertEquals("4", str);
        str = (String)xp.evaluate("count(//wadl:resource[@path='a/{b}'])", doc, XPathConstants.STRING);
        Assert.assertEquals("1", str);
        str = (String)xp.evaluate("count(//wadl:resource[@path='{c}/{d}'])", doc, XPathConstants.STRING);
        Assert.assertEquals("1", str);
        str = (String)xp.evaluate("count(//wadl:resource[@path='{c}/{d}/{e}'])", doc, XPathConstants.STRING);
        Assert.assertEquals("1", str);
        str = (String)xp.evaluate("count(//wadl:resource[@path='sub/{x}'])", doc, XPathConstants.STRING);
        Assert.assertEquals("1", str);

        // discover resource methods
        str = (String)xp.evaluate("count(//wadl:resource[@path='a/{b}']/wadl:method)", doc, XPathConstants.STRING);
        // OPTIONS added automatically by JAX-RS implementation
        //    Assert.assertEquals("6", str);
        Assert.assertEquals("5", str);
        NodeList nl = (NodeList)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:method[@id='m1']/@name", doc, XPathConstants.NODESET);
        // Assert.assertEquals(2, nl.getLength());
        Assert.assertEquals(1, nl.getLength());
        boolean get = false;
        // boolean head = false;
        for (int i = 0; i < nl.getLength(); i++) {
            String t = nl.item(i).getTextContent();
            // if (t.equals("HEAD"))
            // head = true;
            if (t.equals("GET")) {
                get = true;
            }
        }
        // Assert.assertTrue(head && get);
        Assert.assertTrue(get);
        for (int i = 0; i < nl.getLength(); i++) {
            System.out.println(">>>>> resource method : " + nl.item(i).getTextContent());
        }
        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:method[@id='m2']/@name", doc, XPathConstants.STRING);
        Assert.assertEquals("POST", str);
        str = (String)xp
                .evaluate("//wadl:resource[@path='a/{b}']/wadl:method[@id='m2']/wadl:request/wadl:param[@style='header']/@name", doc,
                          XPathConstants.STRING);
        Assert.assertEquals("content-type", str);
        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:method[@id='m3']/@name", doc, XPathConstants.STRING);
        Assert.assertEquals("DELETE", str);
        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:method[@id='m4']/@name", doc, XPathConstants.STRING);
        Assert.assertEquals("PUT", str);

        // discover sub-resource methods
        nl = (NodeList)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:resource[@path='{c}/{d}']/wadl:method/@name", doc,
                                   XPathConstants.NODESET);
        // Assert.assertEquals(2, nl.getLength());
        Assert.assertEquals(1, nl.getLength());
        boolean subget = false;
        // boolean subhead = false;
        for (int i = 0; i < nl.getLength(); i++) {
            String t = nl.item(i).getTextContent();
            // if (t.equals("HEAD"))
            // subhead = true;
            if (t.equals("GET")) {
                subget = true;
            }
        }
        // Assert.assertTrue(subhead && subget);
        Assert.assertTrue(subget);
        for (int i = 0; i < nl.getLength(); i++) {
            System.out.println(">>>>> sub-resource method : " + nl.item(i).getTextContent());
        }
        str = (String)xp.evaluate("count(//wadl:resource[@path='a/{b}']/wadl:resource[@path='{c}/{d}/{e}']/wadl:method)",
                                  doc, XPathConstants.STRING);
        Assert.assertEquals("1", str);

        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:resource[@path='{c}/{d}']/wadl:param[@name='c']/@style", doc,
                                  XPathConstants.STRING);
        Assert.assertEquals("template", str);
        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:resource[@path='{c}/{d}']/wadl:param[@name='b']/@style", doc,
                                  XPathConstants.STRING);
        Assert.assertEquals("template", str);
        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:resource[@path='{c}/{d}/{e}']/wadl:param[@name='c']/@style", doc,
                                  XPathConstants.STRING);
        Assert.assertEquals("template", str);
        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:resource[@path='{c}/{d}/{e}']/wadl:param[@name='e']/@style", doc,
                                  XPathConstants.STRING);
        Assert.assertEquals("template", str);
        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:resource[@path='{c}/{d}/{e}']/wadl:method[@id='m6']/@name", doc,
                                  XPathConstants.STRING);
        Assert.assertEquals("POST", str);

        // discover sub-resource locators
        nl = (NodeList)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:resource[@path='sub/{x}']/wadl:method/@name", doc,
                                   XPathConstants.NODESET);
        // Assert.assertEquals(3, nl.getLength());
        Assert.assertEquals(2, nl.getLength());
        boolean childget = false;
        // boolean childhead = false;
        boolean childopt = false;
        for (int i = 0; i < nl.getLength(); i++) {
            String t = nl.item(i).getTextContent();
            // if (t.equals("HEAD"))
            // childhead = true;
            if (t.equals("GET")) {
                childget = true;
            }
            if (t.equals("OPTIONS")) {
                childopt = true;
            }
        }
        // Assert.assertTrue(childhead && childget && childopt);
        Assert.assertTrue(childget && childopt);
        for (int i = 0; i < nl.getLength(); i++) {
            System.out.println(">>>>> child resource method : " + nl.item(i).getTextContent());
        }

        str = (String)xp.evaluate("count(//wadl:resource[@path='a/{b}']/wadl:resource[@path='sub/{x}']/wadl:method)", doc,
                                  XPathConstants.STRING);
        // Assert.assertEquals("3", str);
        Assert.assertEquals("2", str);
        str = (String)xp.evaluate("//wadl:resource[@path='a/{b}']/wadl:resource[@path='sub/{x}']/wadl:param[@name='x']/@style", doc,
                                  XPathConstants.STRING);
        Assert.assertEquals("template", str);
        str = (String)xp.evaluate(
                "//wadl:resource[@path='a/{b}']/wadl:resource[@path='sub/{x}']/wadl:method[@id='m0']/wadl:response/wadl:representation/@mediaType",
                doc, XPathConstants.STRING);
        Assert.assertEquals("text/plain", str);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static class DummyNamespaceContext implements NamespaceContext {
        private final String   nsPrefix;
        private final String   nsUri;
        private final Iterator nsIter;

        public DummyNamespaceContext() {
            nsPrefix = "wadl";
            nsUri = "http://research.sun.com/wadl/2006/10";
            List l = new ArrayList(1);
            l.add(nsPrefix);
            nsIter = l.iterator();
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
                return nsIter;
            }
            return Collections.emptyList().iterator();
        }
    }
}
