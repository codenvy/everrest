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

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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

/**
 * Describes roles declared for web application in web.xml file.
 *
 * @author andrew00x
 */
public class WebApplicationDeclaredRoles {
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
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document dom = documentBuilder.parse(input);
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
}
