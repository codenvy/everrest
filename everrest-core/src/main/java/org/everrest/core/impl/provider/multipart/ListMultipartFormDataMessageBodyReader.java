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

    @Context
    private Providers providers;

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
        final Type genericSuperclass = ParameterizedTypeImpl.newParameterizedType(Iterator.class, FileItem.class);
        final MessageBodyReader<Iterator> multipartReader =
                providers.getMessageBodyReader(Iterator.class, genericSuperclass, annotations, mediaType);
        final Iterator iterator =
                multipartReader.readFrom(Iterator.class, genericSuperclass, annotations, mediaType, httpHeaders, entityStream);
        final List<InputItem> result = new LinkedList<>();
        while (iterator.hasNext()) {
            result.add(new InputItemImpl((FileItem)iterator.next(), providers));
        }
        return result;
    }
}
