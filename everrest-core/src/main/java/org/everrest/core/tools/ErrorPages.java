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
package org.everrest.core.tools;

import org.everrest.core.UnhandledException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;

/**
 * Describes error-page references for web application in web.xml file.
 *
 * @author Max Shaposhnik
 */
public class ErrorPages {
    private final Map<Integer, String> errorCodes     = new HashMap<>();
    private final Map<String, String>  exceptionTypes = new HashMap<>();

    public ErrorPages(ServletContext servletContext) {
        loadErrorPages(servletContext, errorCodes, exceptionTypes);
    }

    public boolean hasErrorPage(int errorCode) {
        return errorCodes.get(errorCode) != null;
    }

    public boolean hasErrorPage(String exceptionType) {
        return exceptionTypes.get(exceptionType) != null;
    }

    protected void loadErrorPages(ServletContext servletContext, Map<Integer, String> errorCodes, Map<String, String> exceptionTypes)
            throws UnhandledException {
        InputStream input = servletContext.getResourceAsStream("/WEB-INF/web.xml");
        if (input == null) {
            return;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                factory.setFeature(FEATURE_SECURE_PROCESSING, true);
            } catch (Throwable ignored) {}
            Document dom = factory.newDocumentBuilder().parse(input);
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            NodeList all = (NodeList)xpath.evaluate("/web-app/error-page", dom, XPathConstants.NODESET);
            int length = all.getLength();
            for (int i = 0; i < length; i++) {
                Node errorPage = all.item(i);
                if (errorPage.getNodeType() == Node.ELEMENT_NODE) {
                    Element errorElement = (Element)errorPage;
                    NodeList locationList = errorElement.getElementsByTagName("location");
                    if (locationList.getLength() == 0) {
                        continue;
                    }
                    String location = locationList.item(0).getTextContent();
                    NodeList errorCodeList = errorElement.getElementsByTagName("error-code");
                    if (errorCodeList.getLength() != 0) {
                        Integer errorCode;
                        try {
                            errorCode = Integer.valueOf(errorCodeList.item(0).getTextContent());
                        } catch (NumberFormatException e) {
                            continue;
                        }
                        errorCodes.put(errorCode, location);
                    } else {
                        NodeList exceptionTypeList = errorElement.getElementsByTagName("exception-type");
                        if (exceptionTypeList.getLength() != 0) {
                            exceptionTypes.put(exceptionTypeList.item(0).getTextContent(), location);
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException e) {
            throw new UnhandledException(e);
        } finally {
            try {
                input.close();
            } catch (IOException ignored) {
            }
        }
    }
}
