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
package org.everrest.core.impl.provider;

import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author andrew00x
 */
public class SourceEntityProviderTest {

    private byte[]    data;
    private MediaType mediaType;

    @Before
    public void setUp() throws Exception {
        mediaType = new MediaType("application", "xml");
        data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><message>to be or not to be</message></root>".getBytes("UTF-8");
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testReadStreamSourceEntityProvider() throws Exception {
        MessageBodyReader reader = new StreamSourceEntityProvider();
        Assert.assertTrue(reader.isReadable(StreamSource.class, StreamSource.class, null, mediaType));
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle(HttpHeaders.CONTENT_LENGTH, "" + data.length);
        StreamSource src =
                (StreamSource)reader.readFrom(StreamSource.class, StreamSource.class, null, mediaType, h, new ByteArrayInputStream(data));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TransformerFactory.newInstance().newTransformer().transform(src, new StreamResult(out));
        System.out.println(out.toString("UTF-8"));
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testWriteStreamSourceEntityProvider() throws Exception {
        StreamSource src = new StreamSource(new ByteArrayInputStream(data));
        MessageBodyWriter writer = new StreamSourceEntityProvider();
        Assert.assertTrue(writer.isWriteable(StreamSource.class, StreamSource.class, null, mediaType));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeTo(src, StreamSource.class, StreamSource.class, null, mediaType, null, out);
        System.out.println(out.toString("UTF-8"));
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testReadSAXSourceEntityProvider() throws Exception {
        MessageBodyReader reader = new SAXSourceEntityProvider();
        Assert.assertTrue(reader.isReadable(SAXSource.class, SAXSource.class, null, mediaType));
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle(HttpHeaders.CONTENT_LENGTH, "" + data.length);
        SAXSource src = (SAXSource)reader.readFrom(SAXSource.class, SAXSource.class, null, mediaType, h, new ByteArrayInputStream(data));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TransformerFactory.newInstance().newTransformer().transform(src, new StreamResult(out));
        System.out.println(out.toString("UTF-8"));
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testWriteSAXSourceEntityProvider() throws Exception {
        SAXSource src = new SAXSource(new InputSource(new ByteArrayInputStream(data)));
        MessageBodyWriter writer = new SAXSourceEntityProvider();
        Assert.assertTrue(writer.isWriteable(SAXSource.class, SAXSource.class, null, mediaType));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeTo(src, SAXSource.class, SAXSource.class, null, mediaType, null, out);
        System.out.println(out.toString("UTF-8"));
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testReadDOMSourceEntityProvider() throws Exception {
        MessageBodyReader reader = new DOMSourceEntityProvider();
        Assert.assertTrue(reader.isReadable(DOMSource.class, DOMSource.class, null, mediaType));
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle(HttpHeaders.CONTENT_LENGTH, "" + data.length);
        DOMSource src = (DOMSource)reader.readFrom(DOMSource.class, DOMSource.class, null, mediaType, h, new ByteArrayInputStream(data));
        Node root = src.getNode().getFirstChild();
        Assert.assertEquals("root", root.getNodeName());
        Assert.assertEquals("message", root.getFirstChild().getNodeName());
        Assert.assertEquals("to be or not to be", root.getFirstChild().getTextContent());
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testWriteDOMSourceEntityProvider() throws Exception {
        Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(data));
        MessageBodyWriter writer = new DOMSourceEntityProvider();
        Assert.assertTrue(writer.isWriteable(DOMSource.class, DOMSource.class, null, mediaType));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeTo(new DOMSource(d), DOMSource.class, DOMSource.class, null, mediaType, null, out);
        System.out.println(out.toString("UTF-8"));
    }
}
