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
package org.everrest.websockets;

import org.everrest.core.ContainerResponseWriter;
import org.everrest.core.GenericContainerResponse;
import org.everrest.websockets.message.Pair;
import org.everrest.websockets.message.RESTfulOutputMessage;

import javax.ws.rs.ext.MessageBodyWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Fill RESTfulOutputMessage by result of calling RESTful method.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
class EverrestResponseWriter implements ContainerResponseWriter {
    private final RESTfulOutputMessage output;

    private boolean committed;

    EverrestResponseWriter(RESTfulOutputMessage output) {
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
    @SuppressWarnings({"unchecked", "rawtypes"})
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
