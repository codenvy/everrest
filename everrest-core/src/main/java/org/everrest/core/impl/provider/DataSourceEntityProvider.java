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
package org.everrest.core.impl.provider;

import com.google.common.io.ByteStreams;

import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.FileCollector;
import org.everrest.core.provider.EntityProvider;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.util.ByteArrayDataSource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static com.google.common.base.Strings.isNullOrEmpty;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

/**
 * @author andrew00x
 */
@Provider
public class DataSourceEntityProvider implements EntityProvider<DataSource> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == DataSource.class;
    }

    @Override
    public DataSource readFrom(Class<DataSource> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders,
                               InputStream entityStream) throws IOException {
        return createDataSource(entityStream, mediaType == null ? null : mediaType.toString());
    }

    @Override
    public long getSize(DataSource dataSource, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return DataSource.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(DataSource dataSource,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        if (httpHeaders.getFirst(CONTENT_TYPE) == null && !isNullOrEmpty(dataSource.getContentType())) {
            httpHeaders.putSingle(CONTENT_TYPE, dataSource.getContentType());
        }
        try (InputStream in = dataSource.getInputStream()) {
            ByteStreams.copy(in, entityStream);
        }
    }

    /**
     * Create DataSource instance dependent entity size. If entity has size less
     * then <tt>MAX_BUFFER_SIZE</tt> then {@link ByteArrayDataSource} will be
     * created otherwise {@link MimeFileDataSource} will be created.
     *
     * @param entityStream
     *         the {@link InputStream} of the HTTP entity
     * @param mimeType
     *         media type of data, HTTP header 'Content-type'
     * @return See {@link DataSource}
     * @throws IOException
     *         if any i/o errors occurs
     */
    private DataSource createDataSource(InputStream entityStream, String mimeType) throws IOException {

        boolean overflow = false;
        byte[] buffer = new byte[8192];

        ApplicationContext context = ApplicationContext.getCurrent();
        int bufferSize = context.getEverrestConfiguration().getMaxBufferSize();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(bufferSize);

        int bytesNum;
        while (!overflow && ((bytesNum = entityStream.read(buffer)) != -1)) {
            bos.write(buffer, 0, bytesNum);
            if (bos.size() > bufferSize) {
                overflow = true;
            }
        }

        if (overflow) {
            File file = FileCollector.getInstance().createFile();
            try (OutputStream fos = new FileOutputStream(file)) {
                bos.writeTo(fos);
                while ((bytesNum = entityStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesNum);
                }
            }
            return new MimeFileDataSource(file, mimeType);
        }

        return new ByteArrayDataSource(bos.toByteArray(), mimeType);
    }

    /** FileDataSource with preset media type. */
    static class MimeFileDataSource extends FileDataSource {
        /** Media type of the data. */
        private final String mimeType;

        public MimeFileDataSource(File file, String mimeType) {
            super(file);
            this.mimeType = mimeType;
        }

        /** Try remove file when object destroyed. {@inheritDoc} */
        @Override
        protected void finalize() throws Throwable {
            File file = getFile();
            if (file.exists()) {
                file.delete();
            }
            super.finalize();
        }

        @Override
        public String getContentType() {
            return mimeType;
        }
    }
}
