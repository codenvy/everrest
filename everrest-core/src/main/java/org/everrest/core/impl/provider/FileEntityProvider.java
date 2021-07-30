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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import org.everrest.core.impl.FileCollector;
import org.everrest.core.provider.EntityProvider;

@Provider
public class FileEntityProvider implements EntityProvider<File> {

  @Override
  public boolean isReadable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type == File.class;
  }

  @Override
  public File readFrom(
      Class<File> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream)
      throws IOException {
    File f = FileCollector.getInstance().createFile();
    try (OutputStream out = new FileOutputStream(f)) {
      ByteStreams.copy(entityStream, out);
    }
    return f;
  }

  @Override
  public long getSize(
      File file, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return file.length();
  }

  @Override
  public boolean isWriteable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return File.class.isAssignableFrom(type);
  }

  @Override
  public void writeTo(
      File file,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream)
      throws IOException {
    try (InputStream in = new FileInputStream(file)) {
      ByteStreams.copy(in, entityStream);
    }
  }
}
