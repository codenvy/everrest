/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
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

import org.everrest.core.provider.EntityProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author andrew00x
 */
@Provider
@Consumes({MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML, "text/*+xml"})
@Produces({MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML, "text/*+xml"})
public class JAXBObjectEntityProvider implements EntityProvider<Object> {
    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(JAXBObjectEntityProvider.class);

    /** @see Providers */
    private Providers providers;

    public JAXBObjectEntityProvider(@Context Providers providers) {
        this.providers = providers;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAnnotationPresent(XmlRootElement.class);
    }

    @Override
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
            LOG.debug(e.getMessage(), e);
            return null;
        } catch (JAXBException e) {
            throw new IOException(String.format("Can't read from input stream, %s", e));
        }
    }

    @Override
    public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAnnotationPresent(XmlRootElement.class);
    }

    @Override
    public void writeTo(Object t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        try {
            JAXBContext jaxbContext = getJAXBContext(type, mediaType);
            Marshaller marshaller = jaxbContext.createMarshaller();
            String charset = getCharset(mediaType);
            if (!isNullOrEmpty(charset)) {
                marshaller.setProperty(Marshaller.JAXB_ENCODING, charset);
            }

            marshaller.marshal(t, entityStream);
        } catch (JAXBException e) {
            throw new IOException(String.format("Can't write to output stream, %s", e));
        }
    }

    private String getCharset(MediaType mediaType) {
        return mediaType == null ? null : mediaType.getParameters().get("charset");
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
        ContextResolver<JAXBContextResolver> resolver = providers.getContextResolver(JAXBContextResolver.class, mediaType);
        if (resolver == null) {
            throw new RuntimeException(String.format("Not found any JAXBContextResolver for media type %s", mediaType));
        }
        JAXBContextResolver jaxbContextResolver = resolver.getContext(null);
        return jaxbContextResolver.getJAXBContext(type);
    }
}
