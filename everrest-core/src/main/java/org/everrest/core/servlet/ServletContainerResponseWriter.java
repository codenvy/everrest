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
package org.everrest.core.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ext.MessageBodyWriter;
import org.everrest.core.ContainerResponseWriter;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.impl.header.HeaderHelper;

public class ServletContainerResponseWriter implements ContainerResponseWriter {
  /** See {@link HttpServletResponse}. */
  private final HttpServletResponse servletResponse;

  /** @param response HttpServletResponse */
  public ServletContainerResponseWriter(HttpServletResponse response) {
    this.servletResponse = response;
  }

  @Override
  @SuppressWarnings({"unchecked"})
  public void writeBody(GenericContainerResponse response, MessageBodyWriter entityWriter)
      throws IOException {
    if (servletResponse.isCommitted()) {
      return;
    }
    Object entity = response.getEntity();
    if (entity != null) {
      OutputStream out = servletResponse.getOutputStream();
      entityWriter.writeTo(
          entity,
          entity.getClass(),
          response.getEntityType(),
          null,
          response.getContentType(),
          response.getHttpHeaders(),
          out);
      out.flush();
    }
  }

  @Override
  public void writeHeaders(GenericContainerResponse response) throws IOException {
    if (servletResponse.isCommitted()) {
      return;
    }

    servletResponse.setStatus(response.getStatus());

    if (response.getHttpHeaders() != null) {
      // content-type and content-length should be preset in headers
      for (Map.Entry<String, List<Object>> e : response.getHttpHeaders().entrySet()) {
        String name = e.getKey();
        for (Object o : e.getValue()) {
          String value;
          if (o != null && (value = HeaderHelper.getHeaderAsString(o)) != null) {
            servletResponse.addHeader(name, value);
          }
        }
      }
    }
  }
}
