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
import org.everrest.core.util.NoSyncByteArrayOutputStream;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author andrew00x
 */
@Provider
public class ByteEntityProvider implements EntityProvider<byte[]> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == byte[].class;
    }


    @Override
    public byte[] readFrom(Class<byte[]> type,
                           Type genericType,
                           Annotation[] annotations,
                           MediaType mediaType,
                           MultivaluedMap<String, String> httpHeaders,
                           InputStream entityStream) throws IOException {
        String contentLength = httpHeaders.getFirst(HttpHeaders.CONTENT_LENGTH);
        int length = 0;
        if (contentLength != null) {
            try {
                length = Integer.parseInt(contentLength);
            } catch (NumberFormatException ignored) {
            }
        }
        ByteArrayOutputStream out =
                length > 0 ? new NoSyncByteArrayOutputStream(length) : new NoSyncByteArrayOutputStream();
        IOHelper.write(entityStream, out);
        return out.toByteArray();
    }


    @Override
    public long getSize(byte[] t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return t.length;
    }


    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == byte[].class;
    }


    @Override
    public void writeTo(byte[] t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        entityStream.write(t);
    }
}
