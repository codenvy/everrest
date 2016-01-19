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
package org.everrest.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Faster not synchronized version of ByteArrayOutputStream. Method
 * {@link #getBytes()} gives direct access to byte buffer.
 *
 * @author <a href="andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class NoSyncByteArrayOutputStream extends ByteArrayOutputStream {
    public NoSyncByteArrayOutputStream() {
        this(32);
    }

    public NoSyncByteArrayOutputStream(int size) {
        super(size);
    }

    /**
     * Get original byte buffer instead create copy of it as {@link #toByteArray()} does.
     *
     * @return original byte buffer
     */
    public byte[] getBytes() {
        return buf;
    }


    @Override
    public void reset() {
        count = 0;
    }


    @Override
    public int size() {
        return count;
    }


    @Override
    public byte[] toByteArray() {
        byte[] newBuf = new byte[count];
        System.arraycopy(buf, 0, newBuf, 0, count);
        return newBuf;
    }


    @Override
    public String toString() {
        return new String(buf, 0, count);
    }


    @Override
    public String toString(String charsetName) throws UnsupportedEncodingException {
        return new String(buf, 0, count, charsetName);
    }


    @Override
    public void write(byte[] b) {
        if (b.length == 0) {
            return;
        }
        int pos = count + b.length;
        if (pos > buf.length) {
            expand(Math.max(buf.length << 1, pos));
        }
        System.arraycopy(b, 0, buf, count, b.length);
        count = pos;
    }


    @Override
    public void write(byte[] b, int off, int len) {
        if (len == 0) {
            return;
        }
        if ((off < 0) || (len < 0) || (off > b.length) || ((off + len) > b.length)) {
            throw new IndexOutOfBoundsException();
        }
        int pos = count + len;
        if (pos > buf.length) {
            expand(Math.max(buf.length << 1, pos));
        }
        System.arraycopy(b, off, buf, count, len);
        count = pos;
    }


    @Override
    public void write(int b) {
        int pos = count + 1;
        if (count >= buf.length) {
            expand(Math.max(buf.length << 1, pos));
        }
        buf[count] = (byte)b;
        count = pos;
    }


    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(buf, 0, count);
    }

    /**
     * Expand buffer size to <code>newSize</code>.
     *
     * @param newSize
     *         new buffer size
     */
    private void expand(int newSize) {
        byte[] newBuf = new byte[newSize];
        System.arraycopy(buf, 0, newBuf, 0, count);
        buf = newBuf;
    }
}
