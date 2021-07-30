/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
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

public class StreamSourceEntityProviderTest {
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

    private StreamSourceEntityProvider streamSourceEntityProvider;

    @Before
    public void setUp() throws Exception {
        streamSourceEntityProvider = new StreamSourceEntityProvider();
    }

    @Test
    public void isReadableForStreamSource() throws Exception {
        assertTrue(streamSourceEntityProvider.isReadable(StreamSource.class, null, null, APPLICATION_XML_TYPE));
    }

    @Test
    public void isNotReadableForTypeOtherThanStreamSource() throws Exception {
        assertFalse(streamSourceEntityProvider.isReadable(DOMSource.class, null, null, APPLICATION_XML_TYPE));
    }

    @Test
    public void isWritableForStreamSource() throws Exception {
        assertTrue(streamSourceEntityProvider.isWriteable(StreamSource.class, null, null, APPLICATION_XML_TYPE));
    }

    @Test
    public void isNotWritableForTypeOtherThanStreamSource() throws Exception {
        assertFalse(streamSourceEntityProvider.isWriteable(DOMSource.class, null, null, APPLICATION_XML_TYPE));
    }

    @Test
    public void readsContentOfEntityStreamAsStreamSource() throws Exception {
        StreamSource streamSource = streamSourceEntityProvider.readFrom(StreamSource.class, null, null, APPLICATION_XML_TYPE, new MultivaluedMapImpl(),
                                                                  new ByteArrayInputStream(TEST_XML_CONTENT_BYTES));

        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(streamSource.getInputStream());
        assertThatXmlDocumentContainsAllExpectedNodes(document);
    }

    @Test
    public void writesStreamSourceToOutputStream() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamSourceEntityProvider.writeTo(new StreamSource(new ByteArrayInputStream(TEST_XML_CONTENT_BYTES)), StreamSource.class, null, null, APPLICATION_XML_TYPE, null, out);

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