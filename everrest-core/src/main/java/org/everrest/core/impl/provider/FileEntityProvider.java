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

import com.google.common.io.ByteStreams;

import org.everrest.core.impl.FileCollector;
import org.everrest.core.provider.EntityProvider;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class FileEntityProvider implements EntityProvider<File> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == File.class;
    }

    @Override
    public File readFrom(Class<File> type,
                         Type genericType,
                         Annotation[] annotations,
                         MediaType mediaType,
                         MultivaluedMap<String, String> httpHeaders,
                         InputStream entityStream) throws IOException {
        File f = FileCollector.getInstance().createFile();
        try (OutputStream out = new FileOutputStream(f)) {
            ByteStreams.copy(entityStream, out);
        }
        return f;
    }

    @Override
    public long getSize(File file, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return file.length();
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return File.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(File file,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            ByteStreams.copy(in, entityStream);
        }
    }
}
