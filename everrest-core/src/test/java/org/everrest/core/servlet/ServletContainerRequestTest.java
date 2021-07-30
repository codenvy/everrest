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

import static java.util.Collections.emptyEnumeration;
import static java.util.Collections.enumeration;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.everrest.core.tools.SimplePrincipal;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ServletContainerRequestTest {

  private HttpServletRequest httpServletRequest;

  @Before
  public void setUp() throws Exception {
    httpServletRequest = mock(HttpServletRequest.class);
    when(httpServletRequest.getHeaderNames()).thenReturn(emptyEnumeration());
  }

  @Test
  public void getsHttpMethodFromServletRequest() throws Exception {
    when(httpServletRequest.getMethod()).thenReturn("POST");
    assertEquals("POST", ServletContainerRequest.create(httpServletRequest).getMethod());
  }

  @Test
  public void getsHttpHeadersFromServletRequest() throws Exception {
    Map<String, List<String>> httpHeaders = new HashMap<>();
    httpHeaders.put("content-type", Arrays.asList("text/plain"));
    httpHeaders.put("content-length", Arrays.asList("100"));
    configureHttpHeadersInServletRequest(httpHeaders);

    ServletContainerRequest servletContainerRequest =
        ServletContainerRequest.create(httpServletRequest);

    assertEquals(
        "text/plain", servletContainerRequest.getRequestHeaders().getFirst("content-type"));
    assertEquals("100", servletContainerRequest.getRequestHeaders().getFirst("content-length"));
  }

  @Test
  public void getsUrisFromServletRequest() throws Exception {
    when(httpServletRequest.getScheme()).thenReturn("http");
    when(httpServletRequest.getServerName()).thenReturn("some.host.com");
    when(httpServletRequest.getServerPort()).thenReturn(8080);
    when(httpServletRequest.getContextPath()).thenReturn("/my-app");
    when(httpServletRequest.getServletPath()).thenReturn("/my-servlet");
    when(httpServletRequest.getPathInfo()).thenReturn("/my-resource");
    when(httpServletRequest.getRequestURI()).thenReturn("/my-app/my-servlet/my-resource");
    when(httpServletRequest.getQueryString()).thenReturn("a=b&c=d");

    ServletContainerRequest servletContainerRequest =
        ServletContainerRequest.create(httpServletRequest);

    assertEquals(
        "http://some.host.com:8080/my-app/my-servlet",
        servletContainerRequest.getBaseUri().toString());
    assertEquals(
        "http://some.host.com:8080/my-app/my-servlet/my-resource?a=b&c=d",
        servletContainerRequest.getRequestUri().toString());
  }

  @Test
  public void getsEntityStreamFromServletRequest() throws Exception {
    byte[] inData = "hello world".getBytes();
    when(httpServletRequest.getInputStream()).thenReturn(new TstServletInputStream(inData));

    ServletContainerRequest servletContainerRequest =
        ServletContainerRequest.create(httpServletRequest);

    byte[] data = new byte[inData.length];
    servletContainerRequest.getEntityStream().read(data);
    assertArrayEquals(inData, data);
  }

  @Test
  public void getsSecurityContextFromServletRequest() throws Exception {
    when(httpServletRequest.getUserPrincipal()).thenReturn(new SimplePrincipal("andrew"));
    when(httpServletRequest.isUserInRole("user")).thenReturn(true);
    when(httpServletRequest.isSecure()).thenReturn(false);
    when(httpServletRequest.getAuthType()).thenReturn("BASIC_AUTH");

    ServletContainerRequest servletContainerRequest =
        ServletContainerRequest.create(httpServletRequest);

    assertEquals("BASIC_AUTH", servletContainerRequest.getAuthenticationScheme());
    assertEquals("andrew", servletContainerRequest.getUserPrincipal().getName());
    assertFalse(servletContainerRequest.isSecure());
    assertTrue(servletContainerRequest.isUserInRole("user"));
  }

  private void configureHttpHeadersInServletRequest(Map<String, List<String>> httpHeaders) {
    when(httpServletRequest.getHeaderNames()).thenReturn(enumeration(httpHeaders.keySet()));

    when(httpServletRequest.getHeaders(any(String.class)))
        .thenAnswer(
            new Answer<Enumeration<String>>() {
              @Override
              public Enumeration<String> answer(InvocationOnMock invocation) throws Throwable {
                String headerName = (String) invocation.getArguments()[0];
                List<String> values = httpHeaders.get(headerName);
                return values == null ? emptyEnumeration() : enumeration(values);
              }
            });

    when(httpServletRequest.getHeader(any(String.class)))
        .thenAnswer(
            new Answer<String>() {
              @Override
              public String answer(InvocationOnMock invocation) throws Throwable {
                String headerName = (String) invocation.getArguments()[0];
                List<String> values = httpHeaders.get(headerName);
                return values == null || values.isEmpty() ? null : values.get(0);
              }
            });
  }
}
