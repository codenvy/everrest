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

import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.EverrestConfiguration;
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

/**
 * @author andrew00x
 */
@Provider
public class DataSourceEntityProvider implements EntityProvider<DataSource> {

    /** {@inheritDoc} */
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == DataSource.class;
    }

    /** {@inheritDoc} */
    public DataSource readFrom(Class<DataSource> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders,
                               InputStream entityStream) throws IOException {
        String m = mediaType == null ? null : mediaType.toString();

        return createDataSource(entityStream, m);
    }

    /** {@inheritDoc} */
    public long getSize(DataSource t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    /** {@inheritDoc} */
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return DataSource.class.isAssignableFrom(type);
    }

    /** {@inheritDoc} */
    public void writeTo(DataSource t, Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        InputStream in = t.getInputStream();
        try {
            IOHelper.write(in, entityStream);
        } finally {
            in.close();
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
    private static DataSource createDataSource(InputStream entityStream, String mimeType) throws IOException {

        boolean overflow = false;
        byte[] buffer = new byte[8192];

        ApplicationContext context = ApplicationContextImpl.getCurrent();
        Integer bufferSize = (Integer)context.getAttributes().get(EverrestConfiguration.EVERREST_MAX_BUFFER_SIZE);
        ByteArrayOutputStream bout = new ByteArrayOutputStream(bufferSize);

        int bytes;
        while (!overflow && ((bytes = entityStream.read(buffer)) != -1)) {
            bout.write(buffer, 0, bytes);
            if (bout.size() > bufferSize) {
                overflow = true;
            }
        }

        if (!overflow) {
            // small data , use bytes
            return new ByteArrayDataSource(bout.toByteArray(), mimeType);
        }

        // large data, use file
        final File file = FileCollector.getInstance().createFile();
        OutputStream fos = new FileOutputStream(file);

        // copy data from byte array in file
        bout.writeTo(fos);

        while ((bytes = entityStream.read(buffer)) != -1) {
            fos.write(buffer, 0, bytes);
        }

        fos.close();

        return new MimeFileDataSource(file, mimeType);
    }

    /** FileDataSource with preset media type. */
    static class MimeFileDataSource extends FileDataSource {

        /** Media type of the data. */
        private final String mimeType;

        /**
         * @param file
         *         file
         * @param mimeType
         *         media type
         */
        public MimeFileDataSource(File file, String mimeType) {
            super(file);
            this.mimeType = mimeType;
        }

        /** Try remove file when object destroyed. {@inheritDoc} */
        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            File file = getFile();
            if (file.exists()) {
                file.delete();
            }
        }

        /** {@inheritDoc} */
        @Override
        public String getContentType() {
            return mimeType;
        }
    }
}
