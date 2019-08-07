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
package org.everrest.websockets;

import org.everrest.core.ContainerResponseWriter;
import org.everrest.core.GenericContainerResponse;
import org.everrest.websockets.message.Pair;
import org.everrest.websockets.message.RestOutputMessage;

import javax.ws.rs.ext.MessageBodyWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Fill RestOutputMessage by result of calling REST method.
 *
 * @author andrew00x
 */
class EverrestResponseWriter implements ContainerResponseWriter {
    private final RestOutputMessage output;

    private boolean committed;

    EverrestResponseWriter(RestOutputMessage output) {
        this.output = output;
    }

    @Override
    public void writeHeaders(GenericContainerResponse response) throws IOException {
        if (committed) {
            return;
        }
        output.setResponseCode(response.getStatus());
        output.setHeaders(Pair.fromMap(response.getHttpHeaders()));
        committed = true;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void writeBody(GenericContainerResponse response, MessageBodyWriter entityWriter) throws IOException {
        if (committed) {
            return;
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Object entity = response.getEntity();
        if (entity != null) {
            entityWriter.writeTo(entity, entity.getClass(), response.getEntityType(), null, response.getContentType(),
                                 response.getHttpHeaders(), out);
            byte[] body = out.toByteArray();
            output.setBody(new String(body));
        }
    }
}
