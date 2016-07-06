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

import org.everrest.core.impl.provider.FileEntityProvider;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

/**
 * This provider useful in environment where need disable access to file system.
 *
 * @author andrew00x
 */
@Provider
public class NoFileEntityProvider extends FileEntityProvider {

    @Override
    public long getSize(File file, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        throw new WebApplicationException(Response.status(BAD_REQUEST).entity(
                "File is not supported as method's parameter.").type(TEXT_PLAIN).build());
    }


    @Override
    public File readFrom(Class<File> type,
                         Type genericType,
                         Annotation[] annotations,
                         MediaType mediaType,
                         MultivaluedMap<String, String> httpHeaders,
                         InputStream entityStream) throws IOException {
        throw new WebApplicationException(Response.status(BAD_REQUEST).entity(
                "File is not supported as method's parameter.").type(TEXT_PLAIN).build());
    }


    @Override
    public void writeTo(File file,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        throw new WebApplicationException(Response.status(BAD_REQUEST).entity(
                "File is not supported as method's parameter.").type(TEXT_PLAIN).build());
    }
}
