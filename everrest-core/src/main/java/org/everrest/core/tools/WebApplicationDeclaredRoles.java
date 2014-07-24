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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Describes roles declared for web application in web.xml file.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class WebApplicationDeclaredRoles {
    private final Set<String> declaredRoles;

    public WebApplicationDeclaredRoles(ServletContext servletContext) {
        declaredRoles = Collections.unmodifiableSet(loadRoles(servletContext));
    }

    protected Set<String> loadRoles(ServletContext servletContext) throws UnhandledException {
        InputStream input = servletContext.getResourceAsStream("/WEB-INF/web.xml");
        if (input == null) {
            return Collections.emptySet();
        }
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document dom = documentBuilder.parse(input);
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            NodeList all = (NodeList)xpath.evaluate("/web-app/security-role/role-name", dom, XPathConstants.NODESET);
            int length = all.getLength();
            Set<String> roles = new LinkedHashSet<String>(length);
            for (int i = 0; i < length; i++) {
                roles.add(all.item(i).getTextContent());
            }
            return roles;
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

    public Set<String> getDeclaredRoles() {
        return declaredRoles;
    }
}
