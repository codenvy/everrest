/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl.provider;

import com.google.common.collect.ImmutableMap;

import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DOMSourceEntityProviderTest {
    private static final String TEST_XML_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                                                   + "<book send-by-post=\"true\">"
                                                   + "  <title>Java and XML Data Binding</title>"
                                                   + "  <author>Brett McLaughlin</author>"
                                                   + "  <price currency=\"USD\">34.95</price>"
                                                   + "</book>";

    private static final byte[] TEST_XML_CONTENT_BYTES = TEST_XML_CONTENT.getBytes();
    private static final Map<String, String> XPATH_EXPR_TO_VALUE = ImmutableMap.of(
            "/book/@send-by-post", "true",
            "/book/title", "Java and XML Data Binding",
            "/book/author", "Brett McLaughlin",
            "/book/price/@currency", "USD",
            "/book/price","34.95");

    private DOMSourceEntityProvider domSourceEntityProvider;

    @Before
    public void setUp() throws Exception {
        domSourceEntityProvider = new DOMSourceEntityProvider();
    }

    @Test
    public void isReadableForDOMSource() throws Exception {
        assertTrue(domSourceEntityProvider.isReadable(DOMSource.class, null, null, APPLICATION_XML_TYPE));
    }

    @Test
    public void isNotReadableForTypeOtherThanDOMSource() throws Exception {
        assertFalse(domSourceEntityProvider.isReadable(SAXSource.class, null, null, APPLICATION_XML_TYPE));
    }

    @Test
    public void isWritableForDOMSource() throws Exception {
        assertTrue(domSourceEntityProvider.isWriteable(DOMSource.class, null, null, APPLICATION_XML_TYPE));
    }

    @Test
    public void isNotWritableForTypeOtherThanDOMSource() throws Exception {
        assertFalse(domSourceEntityProvider.isWriteable(SAXSource.class, null, null, APPLICATION_XML_TYPE));
    }

    @Test
    public void readsContentOfEntityStreamAsDOMSource() throws Exception {
        DOMSource domSource = domSourceEntityProvider.readFrom(DOMSource.class, null, null, APPLICATION_XML_TYPE, new MultivaluedMapImpl(),
                                                               new ByteArrayInputStream(TEST_XML_CONTENT_BYTES));

        Node document = domSource.getNode();
        assertThatXmlDocumentContainsAllExpectedNodes(document);
    }

    @Test
    public void writesDOMSourceToOutputStream() throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(TEST_XML_CONTENT_BYTES));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        domSourceEntityProvider.writeTo(new DOMSource(document), DOMSource.class, null, null, APPLICATION_XML_TYPE, null, out);

        Document serializedDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
        assertThatXmlDocumentContainsAllExpectedNodes(serializedDocument);
    }

    private void assertThatXmlDocumentContainsAllExpectedNodes(Node document) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        for (Map.Entry<String, String> entry : XPATH_EXPR_TO_VALUE.entrySet()) {
            String xpathExpression = entry.getKey();
            String expectedResult = entry.getValue();

            String result = (String)xPath.evaluate(xpathExpression, document, XPathConstants.STRING);
            assertEquals(String.format("Unexpected result in XML Document for expression: %s", xpathExpression),
                         expectedResult, result);
        }
    }
}