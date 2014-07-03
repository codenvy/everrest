/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author andrew00x
 */
@Provider
@Consumes({MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML, "text/*+xml"})
@Produces({MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML, "text/*+xml"})
public class JAXBElementEntityProvider implements EntityProvider<JAXBElement<?>> {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(JAXBElementEntityProvider.class);

    /** @see Providers */
    @Context
    private Providers providers;

    /** {@inheritDoc} */
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == JAXBElement.class && genericType instanceof ParameterizedType;
    }

    /** {@inheritDoc} */
    public JAXBElement<?> readFrom(Class<JAXBElement<?>> type,
                                   Type genericType,
                                   Annotation[] annotations,
                                   MediaType mediaType,
                                   MultivaluedMap<String, String> httpHeaders,
                                   InputStream entityStream) throws IOException {
        ParameterizedType pt = (ParameterizedType)genericType;
        Class<?> c = (Class<?>)pt.getActualTypeArguments()[0];
        try {
            JAXBContext jaxbContext = getJAXBContext(c, mediaType);
            return jaxbContext.createUnmarshaller().unmarshal(new StreamSource(entityStream), c);
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
    public long getSize(JAXBElement<?> t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    /** {@inheritDoc} */
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return JAXBElement.class.isAssignableFrom(type);
    }

    /** {@inheritDoc} */
    public void writeTo(JAXBElement<?> t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        Class<?> c = t.getDeclaredType();
        try {
            JAXBContext jaxbContext = getJAXBContext(c, mediaType);
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
