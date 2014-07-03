/*
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.everrest.core.impl.provider.multipart;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * @author andrew00x
 */
@Provider
@Produces({"multipart/*"})
public class CollectionMultipartFormDataMessageBodyWriter extends BaseMultipartFormDataWriter
        implements MessageBodyWriter<Collection<OutputItem>> {
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
        httpHeaders.putSingle("Content-type", mediaType.toString() + "; boundary=" + boundary);
        final byte[] boundaryBytes = boundary.getBytes();
        writeItems(items, entityStream, boundaryBytes);
    }
}
