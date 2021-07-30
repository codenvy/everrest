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
package org.everrest.core.impl.provider.multipart;

import org.apache.commons.fileupload.FileItem;
import org.everrest.core.util.ParameterizedTypeImpl;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author andrew00x
 */
@Provider
@Consumes({"multipart/*"})
public class ListMultipartFormDataMessageBodyReader implements MessageBodyReader<List<InputItem>> {

    private final Providers providers;

    public ListMultipartFormDataMessageBodyReader(@Context Providers providers) {
        this.providers = providers;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (type == List.class && genericType instanceof ParameterizedType) {
            ParameterizedType t = (ParameterizedType)genericType;
            Type[] ta = t.getActualTypeArguments();
            return ta.length == 1 && ta[0] == InputItem.class;
        }
        return false;
    }

    @Override
    public List<InputItem> readFrom(Class<List<InputItem>> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                                    MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {
        final Type fileItemIteratorGenericType = ParameterizedTypeImpl.newParameterizedType(Iterator.class, FileItem.class);
        final MessageBodyReader<Iterator> multipartReader =
                providers.getMessageBodyReader(Iterator.class, fileItemIteratorGenericType, annotations, mediaType);
        final Iterator iterator =
                multipartReader.readFrom(Iterator.class, fileItemIteratorGenericType, annotations, mediaType, httpHeaders, entityStream);
        final List<InputItem> result = new LinkedList<>();
        while (iterator.hasNext()) {
            result.add(new DefaultInputItem((FileItem)iterator.next(), providers));
        }
        return result;
    }
}
