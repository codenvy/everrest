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

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import java.io.IOException;

public class TemplatesParser {
    private EntityResolver resolver;

    public void setResolver(EntityResolver resolver) {
        this.resolver = resolver;
    }

    public Templates parseTemplates(Source source) throws TransformerConfigurationException, SAXException, IOException {
        InputSource inputSource = SAXSource.sourceToInputSource(source);
        if (inputSource == null) {
            throw new RuntimeException("Unable convert to Input Source");
        }

        SAXTransformerFactory saxTransformerFactory = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
        TemplatesHandler templateHandler = saxTransformerFactory.newTemplatesHandler();
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        if (resolver != null) {
            xmlReader.setEntityResolver(resolver);
        }
        xmlReader.setContentHandler(templateHandler);

        xmlReader.parse(inputSource);

        Templates templates = templateHandler.getTemplates();
        if (templates == null) {
            throw new RuntimeException("Unable create templates from given source");
        }
        return templates;
    }
}
