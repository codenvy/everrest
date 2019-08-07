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
package org.everrest.core.impl.provider.multipart;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * @author andrew00x
 */
@Provider
@Produces({"multipart/*"})
public class CollectionMultipartFormDataMessageBodyWriter implements MessageBodyWriter<Collection<OutputItem>> {

    private final MultipartFormDataWriter multipartFormDataWriter;

    public CollectionMultipartFormDataMessageBodyWriter(@Context Providers providers) {
        this(new MultipartFormDataWriter(providers));
    }

    CollectionMultipartFormDataMessageBodyWriter(MultipartFormDataWriter multipartFormDataWriter) {
        this.multipartFormDataWriter = multipartFormDataWriter;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (Collection.class.isAssignableFrom(type) && genericType instanceof ParameterizedType) {
            ParameterizedType t = (ParameterizedType)genericType;
            Type[] ta = t.getActualTypeArguments();
            return ta.length == 1 && ta[0] instanceof Class && OutputItem.class.isAssignableFrom((Class<?>)ta[0]);
        }
        return false;
    }

    @Override
    public long getSize(Collection<OutputItem> items, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Collection<OutputItem> items, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        String boundary = mediaType.getParameters().get("boundary");
        if (boundary == null) {
            boundary = Long.toString(System.currentTimeMillis());
        }
        httpHeaders.putSingle("Content-type", createMediaTypeWithBoundary(mediaType, boundary));
        final byte[] boundaryBytes = boundary.getBytes();
        multipartFormDataWriter.writeItems(items, entityStream, boundaryBytes);
    }

    private MediaType createMediaTypeWithBoundary(MediaType mediaType, String boundary) {
        Map<String, String> parameters = newHashMap(mediaType.getParameters());
        parameters.put("boundary", boundary);
        return new MediaType(mediaType.getType(), mediaType.getSubtype(), parameters);
    }
}
