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

import org.everrest.core.provider.EntityProvider;
import org.junit.Test;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static com.google.common.collect.Lists.newArrayList;
import static javax.ws.rs.core.MediaType.TEXT_HTML_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_XML_TYPE;
import static org.junit.Assert.assertEquals;

public class ProviderDescriptorImplTest {

    @Provider
    @Consumes({"text/plain", "text/xml"})
    @Produces({"text/plain", "text/html"})
    public static class EntityProvider1 implements EntityProvider<String> {
        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public String readFrom(Class<String> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
                throws IOException, WebApplicationException {
            return null;
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public long getSize(String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return 0;
        }

        @Override
        public void writeTo(String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
                throws IOException, WebApplicationException {
        }
    }

    @Test
    public void providesListOfConsumedTypeForPerRequestProvider() {
        ProviderDescriptorImpl providerDescriptor = new ProviderDescriptorImpl(EntityProvider1.class);
        assertEquals(newArrayList(TEXT_PLAIN_TYPE, TEXT_XML_TYPE), providerDescriptor.consumes());
    }

    @Test
    public void providesListOfConsumedTypeForPerSingletonProvider() {
        ProviderDescriptorImpl providerDescriptor = new ProviderDescriptorImpl(new EntityProvider1());
        assertEquals(newArrayList(TEXT_PLAIN_TYPE, TEXT_XML_TYPE), providerDescriptor.consumes());
    }

    @Test
    public void providesListOfProducedTypeForPerRequestProvider() {
        ProviderDescriptorImpl providerDescriptor = new ProviderDescriptorImpl(EntityProvider1.class);
        assertEquals(newArrayList(TEXT_HTML_TYPE, TEXT_PLAIN_TYPE), providerDescriptor.produces());
    }

    @Test
    public void providesListOfProducedTypeForPerSingletonProvider() {
        ProviderDescriptorImpl providerDescriptor = new ProviderDescriptorImpl(new EntityProvider1());
        assertEquals(newArrayList(TEXT_HTML_TYPE, TEXT_PLAIN_TYPE), providerDescriptor.produces());
    }
}