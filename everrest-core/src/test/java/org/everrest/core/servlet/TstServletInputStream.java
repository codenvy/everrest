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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

class TstServletInputStream extends ServletInputStream {
  private final ByteArrayInputStream data;

  TstServletInputStream(byte[] data) {
    this.data = new ByteArrayInputStream(data);
  }

  @Override
  public int read() throws IOException {
    return data.read();
  }

  @Override
  public boolean isFinished() {
    return data.available() == 0;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setReadListener(ReadListener readListener) {}
}
