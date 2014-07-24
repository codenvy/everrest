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
