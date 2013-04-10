/*
 * Copyright (C) 2012 eXo Platform SAS.
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

    private boolean commited;

    EverrestResponseWriter(RESTfulOutputMessage output) {
        this.output = output;
    }

    @Override
    public void writeHeaders(GenericContainerResponse response) throws IOException {
        if (commited) {
            throw new IllegalStateException("Response has been commited. Unable write headers. ");
        }
        output.setResponseCode(response.getStatus());
        output.setHeaders(Pair.fromMap(response.getHttpHeaders()));
        commited = true;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void writeBody(GenericContainerResponse response, MessageBodyWriter entityWriter) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Object entity = response.getEntity();
        if (entity != null) {
            entityWriter.writeTo(entity, entity.getClass(), response.getEntityType(), null, response.getContentType(),
                                 response.getHttpHeaders(), out);
            byte[] body = out.toByteArray();
            output.setBody(new String(body));
        }
    }
}
