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

import static org.junit.Assert.assertArrayEquals;

import com.google.common.io.ByteStreams;
import javax.ws.rs.WebApplicationException;
import org.junit.Before;
import org.junit.Test;

public class InMemoryFileItemTest {
  private static final int MAX_ALLOWED_CONTENT_LENGTH = 256;

  private InMemoryFileItem inMemoryFileItem;

  @Before
  public void setUp() throws Exception {
    inMemoryFileItem =
        new InMemoryFileItem("text/plain", "filed", false, "file.txt", MAX_ALLOWED_CONTENT_LENGTH);
  }

  @Test
  public void acceptsContentWhenSizeDoesNotExceedMaxLimit() throws Exception {
    byte[] content = "__TEST__".getBytes();
    inMemoryFileItem.getOutputStream().write(content);
    byte[] readContent = ByteStreams.toByteArray(inMemoryFileItem.getInputStream());
    assertArrayEquals(content, readContent);
  }

  @Test(expected = WebApplicationException.class)
  public void doesNotAcceptContentWhenSizeExceedsMaxLimit() throws Exception {
    byte[] content = new byte[MAX_ALLOWED_CONTENT_LENGTH + 1];
    inMemoryFileItem.getOutputStream().write(content);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void doesNotSupportWritingInFile() throws Exception {
    inMemoryFileItem.write(null);
  }
}
