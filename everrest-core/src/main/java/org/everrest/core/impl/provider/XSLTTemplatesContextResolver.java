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

import org.xml.sax.SAXException;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Provide cache for transformation templates.
 *
 * @author andrew00x
 */
@Provider
@Produces({MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML, "text/*+xml"})
public class XSLTTemplatesContextResolver implements ContextResolver<XSLTTemplatesContextResolver> {
    /** All registered templates. */
    private final ConcurrentMap<String, Templates> templatesMap = new ConcurrentHashMap<String, Templates>();
    private final TemplatesParser templatesParser;

    public XSLTTemplatesContextResolver(TemplatesParser templatesParser) {
        this.templatesParser = templatesParser;
    }

    @Override
    public XSLTTemplatesContextResolver getContext(Class<?> type) {
        return this;
    }

    /**
     * Parse and add given source as templates.
     *
     * @param name
     *         name to which templates will be mapped
     * @param source
     *         templates' source
     * @throws IllegalArgumentException
     *         if templates with specified {@code name} already registered
     * @throws IOException
     *         if any i/o errors occurs
     * @throws SAXException
     *         if given source can not be parsed
     * @throws TransformerConfigurationException
     *         if templates handler can't be initialized
     * @see Templates
     * @see TransformerConfigurationException
     */
    public void addAsTemplate(String name, Source source) throws IOException, SAXException, TransformerConfigurationException {
        addTemplates(name, templatesParser.parseTemplates(source));
    }

    /**
     * @throws IllegalArgumentException
     *         if templates with specified {@code name} already registered
     */
    public void addTemplates(String name, Templates templates) {
        if (templatesMap.putIfAbsent(name, templates) != null) {
            throw new IllegalArgumentException(String.format("Template with name '%s' already registered", name));
        }
    }

    public void removeTemplates(String name) {
        templatesMap.remove(name);
    }

    /**
     * Get templates with given name.
     *
     * @param name
     *         templates' name
     * @return templates or <code>null</code> if no templates mapped to given name
     */
    public Templates getTemplates(String name) {
        return templatesMap.get(name);
    }
}
