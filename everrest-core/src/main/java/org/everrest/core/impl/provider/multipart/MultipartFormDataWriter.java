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

import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.header.HeaderHelper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author andrew00x
 */
public class MultipartFormDataWriter {
    private static final Annotation[] EMPTY             = new Annotation[0];
    private static final byte[]       NEW_LINE          = "\r\n".getBytes();
    private static final byte[]       HEADER_LINE_DELIM = ": ".getBytes();
    private static final byte[]       HYPHENS           = "--".getBytes();

    private Providers providers;

    public MultipartFormDataWriter(Providers providers) {
        this.providers = providers;
    }

    public void writeItems(Collection<OutputItem> items, OutputStream output, byte[] boundary) throws IOException {
        for (OutputItem item : items) {
            writeItem(item, output, boundary);
        }
        output.write(HYPHENS);
        output.write(boundary);
        output.write(HYPHENS);
        output.write(NEW_LINE);
    }

    @SuppressWarnings("unchecked")
    protected void writeItem(OutputItem item, OutputStream output, byte[] boundary) throws IOException {
        output.write(HYPHENS);
        output.write(boundary);
        output.write(NEW_LINE);
        final MediaType mediaType = item.getMediaType();
        final Class<?> type = item.getType();
        final Type genericType = item.getGenericType();
        final MessageBodyWriter writer = providers.getMessageBodyWriter(type, genericType, EMPTY, mediaType);
        if (writer == null) {
            throw new RuntimeException(
                    String.format("Unable to find a MessageBodyWriter for media type '%s' and class '%s'", mediaType, type.getName()));
        }
        final MultivaluedMap<String, String> myHeaders = new MultivaluedMapImpl();
        String contentDispositionHeader = "form-data; name=\"" + item.getName() + '"';
        final String filename = item.getFilename();
        if (filename != null) {
            contentDispositionHeader += ("; filename=\"" + item.getFilename() + "\"");
        }
        myHeaders.putSingle("Content-Disposition", contentDispositionHeader);
        if (mediaType != null) {
            myHeaders.putSingle("Content-Type", mediaType.toString());
        }
        myHeaders.putAll(item.getHeaders());
        writeHeaders(myHeaders, output);
        writer.writeTo(item.getEntity(), type, genericType, EMPTY, mediaType, myHeaders, output);
        output.write(NEW_LINE);
    }

    private void writeHeaders(MultivaluedMap<String, String> headers, OutputStream output) throws IOException {
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            String name = e.getKey();
            for (Object o : e.getValue()) {
                String value;
                if (o != null && (value = HeaderHelper.getHeaderAsString(o)) != null) {
                    output.write(name.getBytes());
                    output.write(HEADER_LINE_DELIM);
                    output.write(value.getBytes());
                    output.write(NEW_LINE);
                }
            }
        }
        output.write(NEW_LINE);
    }
}
