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

import com.google.common.io.ByteStreams;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import org.everrest.core.ApplicationContext;
import org.everrest.core.provider.EntityProvider;

/** @author andrew00x */
@Provider
public class InputStreamEntityProvider implements EntityProvider<InputStream> {

  @Override
  public boolean isReadable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type == InputStream.class;
  }

  @Override
  public InputStream readFrom(
      Class<InputStream> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream)
      throws IOException {
    ApplicationContext context = ApplicationContext.getCurrent();
    if (context.isAsynchronous()) {
      int bufferSize = context.getEverrestConfiguration().getMaxBufferSize();
      return IOHelper.bufferStream(entityStream, bufferSize);
    }
    return entityStream;
  }

  @Override
  public long getSize(
      InputStream t,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public boolean isWriteable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return InputStream.class.isAssignableFrom(type);
  }

  @Override
  public void writeTo(
      InputStream in,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream)
      throws IOException {
    try {
      ByteStreams.copy(in, entityStream);
    } finally {
      in.close();
    }
  }
}
