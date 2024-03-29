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
package org.everrest.core.impl.provider;

import static jakarta.ws.rs.core.HttpHeaders.CONTENT_LENGTH;

import com.google.common.io.ByteStreams;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import org.everrest.core.provider.EntityProvider;
import org.everrest.core.util.NoSyncByteArrayOutputStream;

/** @author andrew00x */
@Provider
public class ByteEntityProvider implements EntityProvider<byte[]> {

  @Override
  public boolean isReadable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type == byte[].class;
  }

  @Override
  public byte[] readFrom(
      Class<byte[]> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream)
      throws IOException {
    int length = getContentLength(httpHeaders);
    ByteArrayOutputStream out =
        length > 0 ? new NoSyncByteArrayOutputStream(length) : new NoSyncByteArrayOutputStream();
    ByteStreams.copy(entityStream, out);
    return out.toByteArray();
  }

  private int getContentLength(MultivaluedMap<String, String> httpHeaders) {
    String contentLength = httpHeaders.getFirst(CONTENT_LENGTH);
    int length = 0;
    if (contentLength != null) {
      try {
        length = Integer.parseInt(contentLength);
      } catch (NumberFormatException ignored) {
      }
    }
    return length;
  }

  @Override
  public long getSize(
      byte[] bytes,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType) {
    return bytes.length;
  }

  @Override
  public boolean isWriteable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type == byte[].class;
  }

  @Override
  public void writeTo(
      byte[] bytes,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream)
      throws IOException {
    entityStream.write(bytes);
  }
}
