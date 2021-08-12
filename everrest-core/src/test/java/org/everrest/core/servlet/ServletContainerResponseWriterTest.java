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

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.impl.provider.StringEntityProvider;
import org.junit.Before;
import org.junit.Test;

public class ServletContainerResponseWriterTest {
  private HttpServletResponse httpServletResponse;
  private GenericContainerResponse containerResponse;

  private ServletContainerResponseWriter servletContainerResponseWriter;

  @Before
  public void setUp() throws Exception {
    httpServletResponse = mock(HttpServletResponse.class);
    containerResponse = mock(GenericContainerResponse.class);
    servletContainerResponseWriter = new ServletContainerResponseWriter(httpServletResponse);
  }

  @Test
  public void writesBody() throws Exception {
    TstServletOutputStream output = new TstServletOutputStream();
    when(httpServletResponse.getOutputStream()).thenReturn(output);
    when(containerResponse.getEntity()).thenReturn("hello world");
    when(containerResponse.getHttpHeaders()).thenReturn(new MultivaluedHashMap<>());

    servletContainerResponseWriter.writeBody(containerResponse, new StringEntityProvider());

    assertArrayEquals("hello world".getBytes(), output.getData());
  }

  @Test
  public void writesStatusAndHeaders() throws Exception {
    MultivaluedMap<String, Object> responseHeaders = new MultivaluedHashMap<>();
    responseHeaders.putSingle("content-type", "text/plain");
    when(containerResponse.getStatus()).thenReturn(200);
    when(containerResponse.getHttpHeaders()).thenReturn(responseHeaders);

    servletContainerResponseWriter.writeHeaders(containerResponse);

    verify(httpServletResponse).setStatus(eq(200));
    verify(httpServletResponse).addHeader(eq("content-type"), eq("text/plain"));
  }
}
