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
package org.everrest.core.impl.provider;

import org.apache.commons.fileupload.DefaultFileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.FileCollector;
import org.everrest.core.provider.EntityProvider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Processing multipart data based on apache file-upload.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: MultipartFormDataEntityProvider.java 497 2009-11-08 13:19:25Z
 *          aparfonov $
 */
@Provider
@Consumes({"multipart/*"})
public class MultipartFormDataEntityProvider implements EntityProvider<Iterator<FileItem>> {

    /** @see HttpServletRequest */
    @Context
    private HttpServletRequest httpRequest;

    /** {@inheritDoc} */
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (type == Iterator.class) {
            try {
                ParameterizedType t = (ParameterizedType)genericType;
                Type[] ta = t.getActualTypeArguments();
                return ta.length == 1 && ta[0] == FileItem.class;
            } catch (ClassCastException e) {
                return false;
            }
        }

        return false;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Iterator<FileItem> readFrom(Class<Iterator<FileItem>> type,
                                       Type genericType,
                                       Annotation[] annotations,
                                       MediaType mediaType,
                                       MultivaluedMap<String, String> httpHeaders,
                                       InputStream entityStream) throws IOException {
        try {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            Integer bufferSize = (Integer)context.getAttributes().get(EverrestConfiguration.EVERREST_MAX_BUFFER_SIZE);
            DefaultFileItemFactory factory =
                    new DefaultFileItemFactory(bufferSize, FileCollector.getInstance().getStore());
            FileUpload upload = new FileUpload(factory);
            return upload.parseRequest(httpRequest).iterator();
        } catch (FileUploadException e) {
            throw new IOException("Can't process multipart data item " + e);
        }
    }

    /** {@inheritDoc} */
    public long getSize(Iterator<FileItem> t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    /** {@inheritDoc} */
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        // output is not supported
        return false;
    }

    /** {@inheritDoc} */
    public void writeTo(Iterator<FileItem> t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        throw new UnsupportedOperationException();
    }
}
