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

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;

/**
 * Provide cache for transformation templates.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: XSLTTemplatesContextResolver.java 63 2010-10-15 14:31:56Z
 *          andrew00x $
 */
@Provider
@Consumes({MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML, "text/*+xml"})
public class XSLTTemplatesContextResolver implements ContextResolver<XSLTTemplatesContextResolver> {
    /** All registered templates. */
    private final Map<String, Templates> templates = new HashMap<String, Templates>();

    /** XML entity resolver. */
    private EntityResolver resolver;


    @Override
    public XSLTTemplatesContextResolver getContext(Class<?> type) {
        return this;
    }

    /**
     * Add entity resolver.
     *
     * @param resolver
     *         entity resolver
     */
    public void setXmlResolver(EntityResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Parse and add given source as templates.
     *
     * @param name
     *         name to which templates will be mapped
     * @param source
     *         templates' source
     * @throws IOException
     *         if any i/o errors occurs
     * @throws SAXException
     *         if given source can not be parsed
     * @throws TransformerConfigurationException
     *         if templates handler can't be
     *         initialized
     * @see Templates
     * @see TransformerConfigurationException
     */
    public void addAsTemplate(String name, Source source) throws IOException, SAXException,
                                                                 TransformerConfigurationException {
        if (templates.get(name) != null) {
            throw new IllegalArgumentException("Template with name '" + name + "' already registered. ");
        }
        synchronized (templates) {
            SAXTransformerFactory factory = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
            factory.setFeature(FEATURE_SECURE_PROCESSING, true);
            TemplatesHandler templateHandler = factory.newTemplatesHandler();
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            if (resolver != null) {
                xmlReader.setEntityResolver(resolver);
            }
            xmlReader.setContentHandler(templateHandler);

            InputSource inputSource = SAXSource.sourceToInputSource(source);
            if (inputSource == null) {
                throw new RuntimeException("Unable convert to Input Source.");
            }

            xmlReader.parse(inputSource);

            Templates t = templateHandler.getTemplates();
            if (t == null) {
                throw new RuntimeException("Unable create templates from given source. ");
            }

            templates.put(name, t);
        }
    }

    /**
     * Get templates with given name.
     *
     * @param name
     *         templates' name
     * @return templates or <code>null</code> if no templates mapped to given
     * name
     */
    public Templates getTemplates(String name) {
        return templates.get(name);
    }
}
