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

import static java.util.Collections.enumeration;
import static org.everrest.core.ExtHttpHeaders.FORWARDED_HOST;
import static org.everrest.core.ExtHttpHeaders.FORWARDED_PROTO;
import static org.junit.Assert.assertEquals;
import static org.junit.runners.Parameterized.Parameter;
import static org.junit.runners.Parameterized.Parameters;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test for {@link ServletContainerRequest}
 *
 * @author Tareq Sharafy <tareq.sharafy@sap.com>
 */
@RunWith(Parameterized.class)
public class ServletContainerRequestForwardedHeadersTest {

  private static final String TEST_HOST = "test.myhost.com";
  private static final int TEST_PORT = 8080;

  @Parameters(name = "{index} When X-Forwarded-Host header is {0} then Request URI is {2}")
  public static List<Object[]> testData() {
    return Arrays.asList(
        new Object[][] {
          // --- Invalid forwarded headers, ignored
          {"a b c", createBaseUri(TEST_HOST, TEST_PORT), createRequestUri(TEST_HOST, TEST_PORT)},
          {
            "myhost.com:8877:200",
            createBaseUri(TEST_HOST, TEST_PORT),
            createRequestUri(TEST_HOST, TEST_PORT)
          },
          {
            "myhost..com",
            createBaseUri(TEST_HOST, TEST_PORT),
            createRequestUri(TEST_HOST, TEST_PORT)
          },
          // ---
          {
            "other.myhost.com",
            createBaseUri("other.myhost.com"),
            createRequestUri("other.myhost.com")
          },
          {
            "other.myhost.com:777",
            createBaseUri("other.myhost.com", 777),
            createRequestUri("other.myhost.com", 777)
          }
        });
  }

  private static String createBaseUri(String host, int port) {
    return String.format("http://%s:%d/myapp/myservlet", host, port);
  }

  private static String createRequestUri(String host, int port) {
    return String.format("http://%s:%d/myapp/myservlet/datapath", host, port);
  }

  private static String createBaseUri(String host) {
    return String.format("http://%s/myapp/myservlet", host);
  }

  private static String createRequestUri(String host) {
    return String.format("http://%s/myapp/myservlet/datapath", host);
  }

  //

  @Parameter(0)
  public String forwardedHost;

  @Parameter(1)
  public String expectedBaseUri;

  @Parameter(2)
  public String expectedRequestUri;

  private HttpServletRequest httpServletRequest;
  private Map<String, List<String>> httpHeaders;

  @Before
  public void setUp() {
    httpServletRequest = mock(HttpServletRequest.class);
    httpHeaders = createForwardedHeaders(forwardedHost, null);
    when(httpServletRequest.getHeaderNames()).thenReturn(enumeration(httpHeaders.keySet()));
    when(httpServletRequest.getScheme()).thenReturn("http");
    when(httpServletRequest.getServerName()).thenReturn(TEST_HOST);
    when(httpServletRequest.getServerPort()).thenReturn(TEST_PORT);
    when(httpServletRequest.getContextPath()).thenReturn("/myapp");
    when(httpServletRequest.getServletPath()).thenReturn("/myservlet");
    when(httpServletRequest.getPathInfo()).thenReturn("/myapp");
    when(httpServletRequest.getRequestURI()).thenReturn("/myapp/myservlet/datapath");
    when(httpServletRequest.getHeaders(any(String.class))).thenAnswer(getHeadersByName());
    when(httpServletRequest.getHeader(any(String.class))).thenAnswer(getHeaderByName());
  }

  private Map<String, List<String>> createForwardedHeaders(
      String forwardedHost, String forwardedProto) {
    Map<String, List<String>> finalHeaders = new HashMap<>();
    if (forwardedHost != null) {
      finalHeaders.put(FORWARDED_HOST, Arrays.asList(forwardedHost));
    }
    if (forwardedProto != null) {
      finalHeaders.put(FORWARDED_PROTO, Arrays.asList(forwardedProto));
    }
    finalHeaders.put(HttpHeaders.HOST, Arrays.asList(TEST_HOST + ":" + TEST_PORT));
    return finalHeaders;
  }

  private Answer<Enumeration<String>> getHeadersByName() {
    return new Answer<Enumeration<String>>() {
      @Override
      public Enumeration<String> answer(InvocationOnMock invocation) throws Throwable {
        String name = (String) invocation.getArguments()[0];
        return enumeration(httpHeaders.get(name));
      }
    };
  }

  private Answer<String> getHeaderByName() {
    return new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        String name = (String) invocation.getArguments()[0];
        List<String> values = httpHeaders.get(name);
        return values == null || values.isEmpty() ? null : values.get(0);
      }
    };
  }

  @Test
  public void testForwarded() {
    ServletContainerRequest req = ServletContainerRequest.create(httpServletRequest);

    assertEquals(expectedBaseUri, req.getBaseUri().toString());
    assertEquals(expectedRequestUri, req.getRequestUri().toString());
  }
}
