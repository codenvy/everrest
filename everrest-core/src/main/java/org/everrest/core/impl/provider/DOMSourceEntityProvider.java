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

import org.everrest.core.provider.EntityProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Consumes({MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML, "text/*+xml"})
@Produces({MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML, "text/*+xml"})
public class DOMSourceEntityProvider implements EntityProvider<DOMSource> {
    private static final Logger LOG = LoggerFactory.getLogger(DOMSourceEntityProvider.class);

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == DOMSource.class;
    }

    @Override
    public DOMSource readFrom(Class<DOMSource> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType,
                              MultivaluedMap<String, String> httpHeaders,
                              InputStream entityStream) throws IOException {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            Document document = documentBuilderFactory.newDocumentBuilder().parse(entityStream);
            return new DOMSource(document);
        } catch (SAXParseException saxpe) {
            if (LOG.isDebugEnabled()) {
                LOG.error(saxpe.getMessage(), saxpe);
            }
            return null;
        } catch (SAXException | ParserConfigurationException saxe) {
            throw new IOException(String.format("Can't read from input stream, %s", saxe));
        }
    }

    @Override
    public long getSize(DOMSource t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return DOMSource.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(DOMSource domSource,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        StreamResult streamResult = new StreamResult(entityStream);
        try {
            TransformerFactory.newInstance().newTransformer().transform(domSource, streamResult);
        } catch (TransformerException | TransformerFactoryConfigurationError e) {
            throw new IOException(String.format("Can't write to output stream, %s", e));
        }
    }
}
