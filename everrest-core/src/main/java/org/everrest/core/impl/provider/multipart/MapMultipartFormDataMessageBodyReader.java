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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author andrew00x
 */
@Provider
@Consumes({"multipart/*"})
public class MapMultipartFormDataMessageBodyReader implements MessageBodyReader<Map<String, InputItem>> {

    @Context
    private Providers providers;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (type == Map.class && genericType instanceof ParameterizedType) {
            ParameterizedType t = (ParameterizedType)genericType;
            Type[] ta = t.getActualTypeArguments();
            return ta.length == 2 && ta[0] == String.class && ta[1] == InputItem.class;
        }
        return false;
    }

    @Override
    public Map<String, InputItem> readFrom(Class<Map<String, InputItem>> type, Type genericType, Annotation[] annotations,
                                           MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {
        final Type genericSuperclass = ParameterizedTypeImpl.newParameterizedType(Iterator.class, FileItem.class);
        final MessageBodyReader<Iterator> multipartReader =
                providers.getMessageBodyReader(Iterator.class, genericSuperclass, annotations, mediaType);
        final Iterator iterator =
                multipartReader.readFrom(Iterator.class, genericSuperclass, annotations, mediaType, httpHeaders, entityStream);
        final Map<String, InputItem> result = new LinkedHashMap<>();
        while (iterator.hasNext()) {
            final InputItemImpl item = new InputItemImpl((FileItem)iterator.next(), providers);
            result.put(item.getName(), item);
        }
        return result;
    }
}
