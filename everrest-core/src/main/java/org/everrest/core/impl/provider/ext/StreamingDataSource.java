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

import jakarta.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implementation of DataSource which simply wrap stream. Note this implementation is not completely
 * conform with DataSource contract. It does not return new <code>InputStream</code> for each call
 * of method {@link #getInputStream()}.
 *
 * @author andrew00x
 */
public class StreamingDataSource implements DataSource {
  private final InputStream stream;
  private final String contentType;

  public StreamingDataSource(InputStream stream, String contentType) {
    this.stream = stream;
    this.contentType = contentType;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return stream;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    throw new UnsupportedOperationException();
  }
}
