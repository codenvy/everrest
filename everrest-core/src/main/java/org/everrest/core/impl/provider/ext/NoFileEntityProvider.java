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
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import org.everrest.core.impl.provider.FileEntityProvider;

/**
 * This provider useful in environment where need disable access to file system.
 *
 * @author andrew00x
 */
@Provider
public class NoFileEntityProvider extends FileEntityProvider {

  @Override
  public long getSize(
      File file, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    throw new WebApplicationException(
        Response.status(BAD_REQUEST)
            .entity("File is not supported as method's parameter.")
            .type(TEXT_PLAIN)
            .build());
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
    throw new WebApplicationException(
        Response.status(BAD_REQUEST)
            .entity("File is not supported as method's parameter.")
            .type(TEXT_PLAIN)
            .build());
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
    throw new WebApplicationException(
        Response.status(BAD_REQUEST)
            .entity("File is not supported as method's parameter.")
            .type(TEXT_PLAIN)
            .build());
  }
}
