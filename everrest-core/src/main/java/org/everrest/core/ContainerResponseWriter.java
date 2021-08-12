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
package org.everrest.core;

import jakarta.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;

/**
 * All implementation of this interface should be able to write data in container response, e. g.
 * servlet response.
 *
 * @author andrew00x
 */
public interface ContainerResponseWriter {

  /**
   * Write HTTP status and headers in HTTP response.
   *
   * @param response container response
   * @throws IOException if any i/o error occurs
   */
  void writeHeaders(GenericContainerResponse response) throws IOException;

  /**
   * Write entity body in output stream.
   *
   * @param response container response
   * @param entityWriter See {@link MessageBodyWriter}
   * @throws IOException if any i/o error occurs
   */
  void writeBody(GenericContainerResponse response, MessageBodyWriter entityWriter)
      throws IOException;
}
