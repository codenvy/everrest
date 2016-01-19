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
package org.everrest.core.tools;

import org.everrest.core.ContainerResponseWriter;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.impl.OutputHeadersMap;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Mock object that can be used for any tests.
 *
 * @author andrew00x
 */
public class ByteArrayContainerResponseWriter implements ContainerResponseWriter {
    /** Message body. */
    private byte[] body;

    /** HTTP headers. */
    private MultivaluedMap<String, Object> headers;

    private boolean committed;


    @Override
    @SuppressWarnings({"unchecked"})
    public void writeBody(GenericContainerResponse response, MessageBodyWriter entityWriter) throws IOException {
        if (committed) {
            return;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Object entity = response.getEntity();
        if (entity != null) {
            entityWriter.writeTo(entity, entity.getClass(), response.getEntityType(), null, response.getContentType(),
                                 response.getHttpHeaders(), out);
            body = out.toByteArray();
        }
    }


    @Override
    public void writeHeaders(GenericContainerResponse response) throws IOException {
        if (committed) {
            return;
        }
        headers = new OutputHeadersMap(response.getHttpHeaders());
        committed = true;
    }

    /** @return message body */
    public byte[] getBody() {
        return body;
    }

    /** @return HTTP headers */
    public MultivaluedMap<String, Object> getHeaders() {
        return headers;
    }

    /** Clear message body and HTTP headers map. */
    public void reset() {
        body = null;
        headers = null;
        committed = false;
    }
}
