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
import org.everrest.core.util.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author andrew00x
 */
@Provider
@Consumes({MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML, "text/*+xml"})
@Produces({MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML, "text/*+xml"})
public class JAXBObjectEntityProvider implements EntityProvider<Object> {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(JAXBObjectEntityProvider.class);

    /** @see Providers */
    @Context
    private Providers providers;

    /** {@inheritDoc} */
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.getAnnotation(XmlRootElement.class) != null;
    }

    /** {@inheritDoc} */
    public Object readFrom(Class<Object> type,
                           Type genericType,
                           Annotation[] annotations,
                           MediaType mediaType,
                           MultivaluedMap<String, String> httpHeaders,
                           InputStream entityStream) throws IOException {
        try {
            JAXBContext jaxbContext = getJAXBContext(type, mediaType);
            return jaxbContext.createUnmarshaller().unmarshal(entityStream);
        } catch (UnmarshalException e) {
            // if can't read from stream (e.g. steam is empty)
            if (LOG.isDebugEnabled()) {
                LOG.error(e.getMessage(), e);
            }
            return null;
        } catch (JAXBException e) {
            throw new IOException("Can't read from input stream " + e);
        }
    }

    /** {@inheritDoc} */
    public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    /** {@inheritDoc} */
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.getAnnotation(XmlRootElement.class) != null;
    }

    /** {@inheritDoc} */
    public void writeTo(Object t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        try {
            JAXBContext jaxbContext = getJAXBContext(type, mediaType);
            Marshaller m = jaxbContext.createMarshaller();
            // Must respect application specified character set.
            String charset = mediaType == null ? null : mediaType.getParameters().get("charset");
            if (charset != null) {
                m.setProperty(Marshaller.JAXB_ENCODING, charset);
            }

            m.marshal(t, entityStream);
        } catch (JAXBException e) {
            throw new IOException("Can't write to output stream " + e);
        }
    }

    /**
     * @param type
     *         type
     * @param mediaType
     *         media type
     * @return JAXBContext JAXBContext
     * @throws JAXBException
     *         if JAXBContext creation failed
     */
    protected JAXBContext getJAXBContext(Class<?> type, MediaType mediaType) throws JAXBException {
        ContextResolver<JAXBContextResolver> resolver =
                providers.getContextResolver(JAXBContextResolver.class, mediaType);
        if (resolver == null) {
            throw new RuntimeException("Not found any JAXBContextResolver for media type " + mediaType);
        }
        JAXBContextResolver jaxbContextResolver = resolver.getContext(null);
        return jaxbContextResolver.getJAXBContext(type);
    }

}
