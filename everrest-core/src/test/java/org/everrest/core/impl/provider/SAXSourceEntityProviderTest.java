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

import com.google.common.collect.ImmutableMap;

import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

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

public class SAXSourceEntityProviderTest {
    private static final String TEST_XML_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                                                   + "<book send-by-post=\"true\">"
                                                   + "  <title>Java and XML Data Binding</title>"
                                                   + "  <author>Brett McLaughlin</author>"
                                                   + "  <price currency=\"USD\">34.95</price>"
                                                   + "</book>";

    private static final byte[]              TEST_XML_CONTENT_BYTES = TEST_XML_CONTENT.getBytes();
    private static final Map<String, String> XPATH_EXPR_TO_VALUE    = ImmutableMap.of(
            "/book/@send-by-post", "true",
            "/book/title", "Java and XML Data Binding",
            "/book/author", "Brett McLaughlin",
            "/book/price/@currency", "USD",
            "/book/price","34.95");

    private SAXSourceEntityProvider saxSourceEntityProvider;

    @Before
    public void setUp() throws Exception {
        saxSourceEntityProvider = new SAXSourceEntityProvider();
    }

    @Test
    public void isReadableForSAXSource() throws Exception {
        assertTrue(saxSourceEntityProvider.isReadable(SAXSource.class, null, null, APPLICATION_XML_TYPE));
    }

    @Test
    public void isNotReadableForTypeOtherThanSAXSource() throws Exception {
        assertFalse(saxSourceEntityProvider.isReadable(DOMSource.class, null, null, APPLICATION_XML_TYPE));
    }

    @Test
    public void isWritableForSAXSource() throws Exception {
        assertTrue(saxSourceEntityProvider.isWriteable(SAXSource.class, null, null, APPLICATION_XML_TYPE));
    }

    @Test
    public void isNotWritableForTypeOtherThanSAXSource() throws Exception {
        assertFalse(saxSourceEntityProvider.isWriteable(DOMSource.class, null, null, APPLICATION_XML_TYPE));
    }

    @Test
    public void readsContentOfEntityStreamAsSAXSource() throws Exception {
        SAXSource saxSource = saxSourceEntityProvider.readFrom(SAXSource.class, null, null, APPLICATION_XML_TYPE, new MultivaluedMapImpl(),
                                                               new ByteArrayInputStream(TEST_XML_CONTENT_BYTES));

        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(saxSource.getInputSource());
        assertThatXmlDocumentContainsAllExpectedNodes(document);
    }

    @Test
    public void writesSAXSourceToOutputStream() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        saxSourceEntityProvider.writeTo(new SAXSource(new InputSource(new ByteArrayInputStream(TEST_XML_CONTENT_BYTES))), SAXSource.class, null, null, APPLICATION_XML_TYPE, null, out);

        Document serializedDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
        assertThatXmlDocumentContainsAllExpectedNodes(serializedDocument);
    }

    private void assertThatXmlDocumentContainsAllExpectedNodes(Document document) throws XPathExpressionException {
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