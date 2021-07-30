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
package org.everrest.core.impl.async;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Provider
@Produces(MediaType.TEXT_PLAIN)
public class AsynchronousProcessListWriter implements MessageBodyWriter<Iterable<AsynchronousProcess>> {
    private static final String OUTPUT_FORMAT = "%-30s%-10s%-10s%s%n";

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (Iterable.class.isAssignableFrom(type) && (genericType instanceof ParameterizedType)) {
            Type[] types = ((ParameterizedType)genericType).getActualTypeArguments();
            return types.length == 1 && types[0] == AsynchronousProcess.class;
        }
        return false;
    }

    @Override
    public long getSize(Iterable<AsynchronousProcess> asynchronousProcesses,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Iterable<AsynchronousProcess> asynchronousProcesses,
                        Class<?> type, Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(entityStream));
        try {
            writer.format(OUTPUT_FORMAT, "USER", "ID", "STAT", "PATH");
            for (AsynchronousProcess process : asynchronousProcesses) {
                writer.format(OUTPUT_FORMAT, process.getOwner() == null ? "unknown" : process.getOwner(), process.getId(), process.getStatus(), process.getPath());
            }
        } finally {
            writer.flush();
        }
    }
}
