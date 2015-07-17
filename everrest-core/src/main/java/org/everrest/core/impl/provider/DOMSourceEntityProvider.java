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
import javax.xml.transform.TransformerConfigurationException;
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

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: DOMSourceEntityProvider.java 285 2009-10-15 16:21:30Z aparfonov
 *          $
 */
@Provider
@Consumes({MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML, "text/*+xml"})
@Produces({MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML, "text/*+xml"})
public class DOMSourceEntityProvider implements EntityProvider<DOMSource> {
    /** Logger. */
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DOMSourceEntityProvider.class);


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
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document d = factory.newDocumentBuilder().parse(entityStream);
            return new DOMSource(d);
        } catch (SAXParseException saxpe) {
            // if can't read from stream (e.g. steam is empty)
            if (LOG.isDebugEnabled()) {
                LOG.error(saxpe.getMessage(), saxpe);
            }
            return null;
        } catch (SAXException saxe) {
            throw new IOException("Can't read from input stream " + saxe);
        } catch (ParserConfigurationException pce) {
            throw new IOException("Can't read from input stream " + pce);
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
    public void writeTo(DOMSource t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        StreamResult out = new StreamResult(entityStream);
        try {
            TransformerFactory.newInstance().newTransformer().transform(t, out);
        } catch (TransformerConfigurationException e) {
            throw new IOException("Can't write to output stream " + e);
        } catch (TransformerException e) {
            throw new IOException("Can't write to output stream " + e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new IOException("Can't write to output stream " + e);
        }
    }
}
