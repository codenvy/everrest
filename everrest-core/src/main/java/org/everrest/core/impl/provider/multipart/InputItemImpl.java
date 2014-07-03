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

import org.apache.commons.fileupload.FileItem;
import org.everrest.core.impl.MultivaluedMapImpl;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author andrew00x
 */
class InputItemImpl implements InputItem {
    private static final Annotation[] EMPTY = new Annotation[0];

    final FileItem                       fileItem;
    final MultivaluedMap<String, String> headers;
    final Providers                      providers;

    InputItemImpl(FileItem fileItem, Providers providers) {
        this.fileItem = fileItem;
        this.providers = providers;
        headers = new MultivaluedMapImpl();
    }

    @Override
    public String getName() {
        return fileItem.getFieldName();
    }

    @Override
    public String getFilename() {
        return fileItem.getName();
    }

    @Override
    public MediaType getMediaType() {
        final String contentType = fileItem.getContentType();
        return contentType == null ? null : MediaType.valueOf(contentType);
    }

    @Override
    public MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

    @Override
    public InputStream getBody() throws IOException {
        return fileItem.getInputStream();
    }

    @Override
    public <T> T getBody(Class<T> type, Type genericType) throws IOException {
        final MediaType mediaType = getMediaType();
        final MessageBodyReader<T> reader = providers.getMessageBodyReader(type, genericType, EMPTY, mediaType);
        if (reader == null) {
            throw new RuntimeException(
                    String.format("Unable to find a MessageBodyReader for media type '%s' and class '%s'", mediaType, type.getName()));
        }
        return reader.readFrom(type, genericType, EMPTY, mediaType, headers, getBody());
    }

    @Override
    public String getBodyAsString() throws IOException {
        return fileItem.getString();
    }
}
