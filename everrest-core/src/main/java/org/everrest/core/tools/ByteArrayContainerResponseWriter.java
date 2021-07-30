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
package org.everrest.core.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import org.everrest.core.ContainerResponseWriter;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.util.CaselessMultivaluedMap;

/**
 * Mock object that can be used for any tests.
 *
 * @author andrew00x
 */
public class ByteArrayContainerResponseWriter implements ContainerResponseWriter {
  private byte[] body;
  private MultivaluedMap<String, Object> headers;

  private boolean committed;

  @Override
  @SuppressWarnings({"unchecked"})
  public void writeBody(GenericContainerResponse response, MessageBodyWriter entityWriter)
      throws IOException {
    if (committed) {
      return;
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Object entity = response.getEntity();
    if (entity != null) {
      entityWriter.writeTo(
          entity,
          entity.getClass(),
          response.getEntityType(),
          null,
          response.getContentType(),
          response.getHttpHeaders(),
          out);
      body = out.toByteArray();
    }
  }

  @Override
  public void writeHeaders(GenericContainerResponse response) throws IOException {
    if (committed) {
      return;
    }
    headers = new CaselessMultivaluedMap<>(response.getHttpHeaders());
    committed = true;
  }

  public byte[] getBody() {
    return body;
  }

  public MultivaluedMap<String, Object> getHeaders() {
    return headers;
  }

  /** Clear message body and HTTP headers map. */
  public void reset() {
    body = null;
    headers = null;
    committed = false;
  }
}
