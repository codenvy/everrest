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
package org.everrest.core.impl.provider.multipart;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;
import org.apache.commons.fileupload.FileItem;
import org.everrest.core.impl.MultivaluedMapImpl;

/** @author andrew00x */
class DefaultInputItem implements InputItem {
  private static final Annotation[] EMPTY = new Annotation[0];

  private final FileItem fileItem;
  private final MultivaluedMap<String, String> headers;
  private final Providers providers;

  DefaultInputItem(FileItem fileItem, Providers providers) {
    this.fileItem = fileItem;
    this.providers = providers;
    headers = new MultivaluedMapImpl();
  }

  @Override
  public String getName() {
    return fileItem.getFieldName();
  }

  @Override
  public String getFilename() {
    return fileItem.getName();
  }

  @Override
  public MediaType getMediaType() {
    final String contentType = fileItem.getContentType();
    return contentType == null ? null : MediaType.valueOf(contentType);
  }

  @Override
  public MultivaluedMap<String, String> getHeaders() {
    return headers;
  }

  @Override
  public InputStream getBody() throws IOException {
    return fileItem.getInputStream();
  }

  @Override
  public <T> T getBody(Class<T> type, Type genericType) throws IOException {
    final MediaType mediaType = getMediaType();
    final MessageBodyReader<T> reader =
        providers.getMessageBodyReader(type, genericType, EMPTY, mediaType);
    if (reader == null) {
      throw new RuntimeException(
          String.format(
              "Unable to find a MessageBodyReader for media type '%s' and class '%s'",
              mediaType, type.getName()));
    }
    return reader.readFrom(type, genericType, EMPTY, mediaType, headers, getBody());
  }

  @Override
  public String getBodyAsString() throws IOException {
    return fileItem.getString();
  }
}
