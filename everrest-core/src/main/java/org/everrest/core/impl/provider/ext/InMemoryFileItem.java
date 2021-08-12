/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl.provider.ext;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;

/**
 * Implementation of {@link FileItem} which allow store data in memory only without access to file
 * system. If size of item exceeds limit (initial allocated buffer size) then {@link
 * WebApplicationException} will be thrown.
 *
 * @author andrew00x
 */
class InMemoryFileItem implements FileItem {

  private static final byte[] EMPTY_DATA = new byte[0];

  private final String fileName;
  private final int maxSize;

  private ByteArrayOutputStream byteArrayOutputStream;
  private FilterOutputStream countingOutputStream;
  private String contentType;
  private String fieldName;
  private boolean isFormField;
  private FileItemHeaders headers;

  InMemoryFileItem(
      String contentType, String fieldName, boolean isFormField, String fileName, int maxSize) {
    this.contentType = contentType;
    this.fieldName = fieldName;
    this.isFormField = isFormField;
    this.fileName = fileName;
    this.maxSize = maxSize;
  }

  @Override
  public void delete() {
    byteArrayOutputStream = null;
    countingOutputStream = null;
  }

  @Override
  public byte[] get() {
    if (byteArrayOutputStream == null) {
      return EMPTY_DATA;
    }
    return byteArrayOutputStream.toByteArray();
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public String getFieldName() {
    return fieldName;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new ByteArrayInputStream(get());
  }

  @Override
  public String getName() {
    return fileName;
  }

  @Override
  public OutputStream getOutputStream() {
    if (byteArrayOutputStream == null) {
      byteArrayOutputStream = new ByteArrayOutputStream(maxSize);
      countingOutputStream =
          new FilterOutputStream(byteArrayOutputStream) {
            private int bytesCounter = 0;

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
              ensureDoNotExceedMaxSize(len);
              super.write(b, off, len);
              bytesCounter += len;
            }

            @Override
            public void write(int b) throws IOException {
              ensureDoNotExceedMaxSize(1);
              super.write(b);
              bytesCounter++;
            }

            private void ensureDoNotExceedMaxSize(int numBytesToWrite) {
              int newSize = bytesCounter + numBytesToWrite;
              if (newSize > maxSize) {
                throw new WebApplicationException(
                    Response.status(413)
                        .entity(
                            String.format("Item size is too large. Must not be over %d", maxSize))
                        .type(TEXT_PLAIN)
                        .build());
              }
            }
          };
    }
    return countingOutputStream;
  }

  @Override
  public long getSize() {
    return get().length;
  }

  @Override
  public String getString() {
    return new String(get());
  }

  @Override
  public String getString(String encoding) throws UnsupportedEncodingException {
    return new String(get(), encoding);
  }

  @Override
  public boolean isFormField() {
    return isFormField;
  }

  @Override
  public boolean isInMemory() {
    return true;
  }

  @Override
  public void setFieldName(String name) {
    this.fieldName = name;
  }

  @Override
  public void setFormField(boolean state) {
    isFormField = state;
  }

  @Override
  public void write(File file) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public FileItemHeaders getHeaders() {
    return headers;
  }

  @Override
  public void setHeaders(FileItemHeaders headers) {
    this.headers = headers;
  }
}
