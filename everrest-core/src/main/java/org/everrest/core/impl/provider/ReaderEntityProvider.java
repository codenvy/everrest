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

import com.google.common.io.CharStreams;

import org.everrest.core.ApplicationContext;
import org.everrest.core.provider.EntityProvider;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author andrew00x
 */
@Provider
public class ReaderEntityProvider implements EntityProvider<Reader> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == Reader.class;
    }

    @Override
    public Reader readFrom(Class<Reader> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                           MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {
        ApplicationContext context = ApplicationContext.getCurrent();
        if (context.isAsynchronous()) {
            int bufferSize = context.getEverrestConfiguration().getMaxBufferSize();
            return new InputStreamReader(IOHelper.bufferStream(entityStream, bufferSize), getCharsetOrUtf8(mediaType));
        }

        return new InputStreamReader(entityStream, getCharsetOrUtf8(mediaType));
    }

    @Override
    public long getSize(Reader t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Reader.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(Reader reader, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
        Writer out = new OutputStreamWriter(entityStream, getCharsetOrUtf8(mediaType));
        try {
            CharStreams.copy(reader, out);
        } finally {
            out.flush();
            reader.close();
        }
    }

    private String getCharsetOrUtf8(MediaType mediaType) {
        String charset = mediaType == null ? null : mediaType.getParameters().get("charset");
        if (isNullOrEmpty(charset)) {
            charset = "UTF-8";
        }
        return charset;
    }
}
