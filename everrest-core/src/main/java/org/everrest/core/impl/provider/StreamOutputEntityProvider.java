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

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import org.everrest.core.provider.EntityProvider;

@Provider
public class StreamOutputEntityProvider implements EntityProvider<StreamingOutput> {

  @Override
  public boolean isReadable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return false;
  }

  @Override
  public StreamingOutput readFrom(
      Class<StreamingOutput> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getSize(
      StreamingOutput streamingOutput,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public boolean isWriteable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return StreamingOutput.class.isAssignableFrom(type);
  }

  @Override
  public void writeTo(
      StreamingOutput streamingOutput,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream)
      throws IOException {
    streamingOutput.write(entityStream);
  }
}
