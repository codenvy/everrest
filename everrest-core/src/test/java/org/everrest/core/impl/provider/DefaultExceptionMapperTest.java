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

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.junit.Assert.assertEquals;

import jakarta.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;

public class DefaultExceptionMapperTest {

  private DefaultExceptionMapper exceptionMapper;

  @Before
  public void setUp() throws Exception {
    exceptionMapper = new DefaultExceptionMapper();
  }

  @Test
  public void createsResponseWithStatus500AndUsesExceptionMessageAsEntity() {
    Exception exception = new Exception("<error message>");

    Response response = exceptionMapper.toResponse(exception);

    assertEquals(500, response.getStatus());
    assertEquals(TEXT_PLAIN_TYPE, response.getMediaType());
    assertEquals(exception.getMessage(), response.getEntity());
  }

  @Test
  public void createsResponseWithStatus500AndUsesFullClassNameOfExceptionAsEntity() {
    Exception exception = new Exception();

    Response response = exceptionMapper.toResponse(exception);

    assertEquals(500, response.getStatus());
    assertEquals(TEXT_PLAIN_TYPE, response.getMediaType());
    assertEquals(exception.getClass().getName(), response.getEntity());
  }
}
