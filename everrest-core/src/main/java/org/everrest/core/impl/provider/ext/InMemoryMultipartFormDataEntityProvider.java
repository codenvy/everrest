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
package org.everrest.core.impl.provider.ext;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.provider.MultipartFormDataEntityProvider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

@Provider
@Consumes({"multipart/*"})
public class InMemoryMultipartFormDataEntityProvider extends MultipartFormDataEntityProvider {

    public InMemoryMultipartFormDataEntityProvider(@Context HttpServletRequest httpRequest) {
        super(httpRequest);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<FileItem> readFrom(Class<Iterator<FileItem>> type,
                                       Type genericType,
                                       Annotation[] annotations,
                                       MediaType mediaType,
                                       MultivaluedMap<String, String> httpHeaders,
                                       InputStream entityStream) throws IOException {
        try {
            ApplicationContext context = ApplicationContext.getCurrent();
            int bufferSize = context.getEverrestConfiguration().getMaxBufferSize();
            FileItemFactory factory = new InMemoryItemFactory(bufferSize);
            FileUpload upload = new FileUpload(factory);
            return upload.parseRequest(httpRequest).iterator();
        } catch (FileUploadException e) {
            throw new IOException(String.format("Can't process multipart data item, %s", e));
        }
    }
}
