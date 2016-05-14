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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.google.common.base.Throwables.propagate;
import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;

/**
 * Describes roles declared for web application in web.xml file.
 *
 * @author andrew00x
 */
public class WebApplicationDeclaredRoles {
    private static final Logger LOG = LoggerFactory.getLogger(WebApplicationDeclaredRoles.class);

    private final Set<String> declaredRoles;

    public WebApplicationDeclaredRoles(ServletContext servletContext) {
        Set<String> declaredRoles = new LinkedHashSet<>();
        loadRoles(servletContext, declaredRoles);
        this.declaredRoles = Collections.unmodifiableSet(declaredRoles);
    }

    protected void loadRoles(ServletContext servletContext, Collection<String> roles) {
        InputStream input = servletContext.getResourceAsStream("/WEB-INF/web.xml");
        if (input == null) {
            return;
        }
        try {
            DocumentBuilderFactory documentBuilderFactory = createFeaturedDocumentBuilderFactory();
            Document dom = documentBuilderFactory.newDocumentBuilder().parse(input);
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            NodeList all = (NodeList)xpath.evaluate("/web-app/security-role/role-name", dom, XPathConstants.NODESET);
            int length = all.getLength();
            for (int i = 0; i < length; i++) {
                roles.add(all.item(i).getTextContent());
            }
        } catch (Exception e) {
            propagate(e);
        } finally {
            try {
                input.close();
            } catch (IOException ignored) {
            }
        }
    }

    public Set<String> getDeclaredRoles() {
        return declaredRoles;
    }


    private DocumentBuilderFactory createFeaturedDocumentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature(FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException e) {
            LOG.debug(e.getMessage(), e);
        }
        return factory;
    }
}
