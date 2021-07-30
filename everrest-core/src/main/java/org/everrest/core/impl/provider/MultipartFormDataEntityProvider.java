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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import org.apache.commons.fileupload.DefaultFileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.FileCollector;
import org.everrest.core.provider.EntityProvider;

/**
 * Processing multipart data based on apache file-upload.
 *
 * @author andrew00x
 */
@Provider
@Consumes({"multipart/*"})
public class MultipartFormDataEntityProvider implements EntityProvider<Iterator<FileItem>> {

  protected HttpServletRequest httpRequest;

  public MultipartFormDataEntityProvider(@Context HttpServletRequest httpRequest) {
    this.httpRequest = httpRequest;
  }

  @Override
  public boolean isReadable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    if (type == Iterator.class) {
      if (genericType instanceof ParameterizedType) {
        ParameterizedType parameterizedType = (ParameterizedType) genericType;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return actualTypeArguments.length == 1 && actualTypeArguments[0] == FileItem.class;
      }
    }
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Iterator<FileItem> readFrom(
      Class<Iterator<FileItem>> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream)
      throws IOException {
    try {
      ApplicationContext context = ApplicationContext.getCurrent();
      int bufferSize = context.getEverrestConfiguration().getMaxBufferSize();
      DefaultFileItemFactory factory =
          new DefaultFileItemFactory(bufferSize, FileCollector.getInstance().getStore());
      FileUpload upload = new FileUpload(factory);
      return upload.parseRequest(httpRequest).iterator();
    } catch (FileUploadException e) {
      throw new IOException(String.format("Can't process multipart data item, %s", e));
    }
  }

  @Override
  public long getSize(
      Iterator<FileItem> t,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public boolean isWriteable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return false;
  }

  @Override
  public void writeTo(
      Iterator<FileItem> t,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream)
      throws IOException {
    throw new UnsupportedOperationException();
  }
}
