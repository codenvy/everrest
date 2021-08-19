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
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.everrest.core.ExtHttpHeaders;

/** Transforms {@link java.lang.Exception} to JAX-RS response. */
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {

  @Override
  public Response toResponse(Exception exception) {
    String message = exception.getMessage();
    if (message == null) {
      message = exception.getClass().getName();
    }
    return Response.status(500)
        .entity(message)
        .type(MediaType.TEXT_PLAIN)
        .header(ExtHttpHeaders.JAXRS_BODY_PROVIDED, "Error-Message")
        .build();
  }
}
