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
package org.everrest.core.impl.provider.ext;

import org.apache.commons.fileupload.FileItem;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Implementation of {@link FileItem} which allow store data in memory only
 * without access to file system. If size of item exceeds limit (initial
 * allocated buffer size) then {@link WebApplicationException} will be thrown.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
class InMemoryFileItem implements FileItem {
    class _ByteArrayOutputStream extends ByteArrayOutputStream {
        public _ByteArrayOutputStream(int size) {
            super(size);
        }

        public void write(byte b[], int off, int len) {
            if (len == 0) {
                return;
            }
            if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length)) {
                throw new IndexOutOfBoundsException();
            }
            int newCount = count + len;
            if (newCount > buf.length) {
                throw new WebApplicationException(Response.status(413).entity(
                        "Item size is to large. Must not be over " + buf.length).type(MediaType.TEXT_PLAIN).build());
            }
            System.arraycopy(b, off, buf, count, len);
            count = newCount;
        }

        public void write(int b) {
            int newCount = count + 1;
            if (newCount > buf.length) {
                throw new WebApplicationException(Response.status(413).entity(
                        "Item size is to large. Must not be over " + buf.length).type(MediaType.TEXT_PLAIN).build());
            }
            buf[count] = (byte)b;
            count = newCount;
        }

        void delete() {
            this.buf = null;
        }

        byte[] getByteArray() {
            byte[] copy = new byte[count];
            System.arraycopy(buf, 0, copy, 0, count);
            return copy;
        }

        int getCount() {
            return this.count;
        }
    }

    private _ByteArrayOutputStream bout;

    private String contentType;

    private String fieldName;

    private boolean isFormField;

    private final String fileName;

    private final int maxSize;

    private static final byte[] EMPTY_DATA = new byte[0];

    InMemoryFileItem(String contentType, String fieldName, boolean isFormField, String fileName, int maxSize) {
        this.contentType = contentType;
        this.fieldName = fieldName;
        this.isFormField = isFormField;
        this.fileName = fileName;
        this.maxSize = maxSize;
    }

    /** {@inheritDoc} */
    public void delete() {
        if (bout != null) {
            bout.delete();
        }
    }

    /** {@inheritDoc} */
    public byte[] get() {
        if (bout == null) {
            return EMPTY_DATA;
        }
        return bout.getByteArray();
    }

    /** {@inheritDoc} */
    public String getContentType() {
        return contentType;
    }

    /** {@inheritDoc} */
    public String getFieldName() {
        return fieldName;
    }

    /** {@inheritDoc} */
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(get());
    }

    /** {@inheritDoc} */
    public String getName() {
        return fileName;
    }

    /** {@inheritDoc} */
    public OutputStream getOutputStream() {
        if (bout == null) {
            bout = new _ByteArrayOutputStream(maxSize);
        }
        return bout;
    }

    /** {@inheritDoc} */
    public long getSize() {
        return get().length;
    }

    /** {@inheritDoc} */
    public String getString() {
        return new String(get());
    }

    /** {@inheritDoc} */
    public String getString(String encoding) throws UnsupportedEncodingException {
        return new String(get(), encoding);
    }

    /** {@inheritDoc} */
    public boolean isFormField() {
        return isFormField;
    }

    /** {@inheritDoc} */
    public boolean isInMemory() {
        return true;
    }

    /** {@inheritDoc} */
    public void setFieldName(String name) {
        this.fieldName = name;
    }

    /** {@inheritDoc} */
    public void setFormField(boolean state) {
        isFormField = state;
    }

    /** {@inheritDoc} */
    public void write(File file) throws Exception {
        throw new UnsupportedOperationException();
    }
}
