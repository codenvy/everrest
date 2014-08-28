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
import java.util.*;

/**
 * Describes error-page references for web application in web.xml file.
 *
 * @author Max Shaposhnik
 */
public class WebApplicationErrorHandlers {

    private final Set<String> exceptionHandlers;
    private final Set<String> statusHandlers;

    public WebApplicationErrorHandlers(ServletContext servletContext) {
        Map<String, Set<String>> handlers = loadHandlers(servletContext);
        this.exceptionHandlers = Collections.unmodifiableSet(handlers.get("exceptions"));
        this.statusHandlers = Collections.unmodifiableSet(handlers.get("statuses"));
    }

    protected Map<String,Set<String>> loadHandlers(ServletContext servletContext) throws UnhandledException {
        InputStream input = servletContext.getResourceAsStream("/WEB-INF/web.xml");
        if (input == null) {
            return Collections.emptyMap();
        }
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document dom = documentBuilder.parse(input);
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            NodeList all = (NodeList)xpath.evaluate("/web-app/error-page", dom, XPathConstants.NODESET);
            int length = all.getLength();
            Set<String>  exceptionHandlers = new LinkedHashSet<>();
            Set<String>  statusHandlers = new LinkedHashSet<>();
            for(int i=0; i<length ; i++) {
                Node errorPage = all.item(i);
                if(errorPage.getNodeType() == Node.ELEMENT_NODE) {
                    Element errorElement = (Element)errorPage;
                    NodeList  exceptionTypeList = errorElement.getElementsByTagName("exception-type");
                    if (exceptionTypeList.getLength() != 0) {
                        exceptionHandlers.add(exceptionTypeList.item(0).getTextContent());
                    } else {
                        NodeList  statusTypeList = errorElement.getElementsByTagName("error-code");
                        if (statusTypeList.getLength() != 0) {
                            statusHandlers.add(statusTypeList.item(0).getTextContent());
                        }
                    }
                }
            }
            Map<String, Set<String>> result = new HashMap<>(2);
            result.put("exceptions", exceptionHandlers);
            result.put("statuses", statusHandlers);
            return  result;
        } catch (ParserConfigurationException e) {
            throw new UnhandledException(e);
        } catch (SAXException e) {
            throw new UnhandledException(e);
        } catch (XPathExpressionException e) {
            throw new UnhandledException(e);
        } catch (IOException e) {
            throw new UnhandledException(e);
        } finally {
            try {
                input.close();
            } catch (IOException ignored) {
            }
        }
    }

    public Set<String> getExceptionHandlers() {
        return exceptionHandlers;
    }

    public Set<String> getStatusHandlers() {
        return statusHandlers;
    }


}
