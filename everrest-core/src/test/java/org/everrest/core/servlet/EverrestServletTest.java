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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.everrest.core.UnhandledException;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.tools.ErrorPages;
import org.everrest.core.tools.WebApplicationDeclaredRoles;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

public class EverrestServletTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private HttpServletRequest request;
  private HttpServletResponse response;
  private ServletConfig servletConfig;
  private ServletContext servletContext;
  private EverrestProcessor everrestProcessor;

  private EverrestServlet everrestServlet;

  @Before
  public void setUp() throws Exception {
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    servletConfig = mock(ServletConfig.class);
    servletContext = mock(ServletContext.class);
    everrestProcessor = mock(EverrestProcessor.class);

    when(servletConfig.getServletContext()).thenReturn(servletContext);
    when(servletContext.getAttribute(EverrestProcessor.class.getName()))
        .thenReturn(everrestProcessor);

    when(request.getHeaderNames()).thenReturn(emptyEnumeration());

    everrestServlet = new EverrestServlet();
    everrestServlet.init(servletConfig);
  }

  @Test
  public void setsUpEnvironmentContext() throws Exception {
    everrestServlet.service(request, response);

    ArgumentCaptor<EnvironmentContext> envCaptor =
        ArgumentCaptor.forClass(EnvironmentContext.class);
    verify(everrestProcessor)
        .process(
            isA(ServletContainerRequest.class), isA(ContainerResponse.class), envCaptor.capture());
    EnvironmentContext env = envCaptor.getValue();

    assertSame(request, env.get(HttpServletRequest.class));
    assertSame(response, env.get(HttpServletResponse.class));
    assertSame(servletConfig, env.get(ServletConfig.class));
    assertSame(servletContext, env.get(ServletContext.class));
    assertNotNull(env.get(WebApplicationDeclaredRoles.class));
    assertNotNull(env.get(ErrorPages.class));
  }

  @Test
  public void rethrowsIOExceptionThatThrownByEverrestProcessor() throws Exception {
    IOException ioException = new IOException();
    doThrow(ioException)
        .when(everrestProcessor)
        .process(
            isA(ServletContainerRequest.class),
            isA(ContainerResponse.class),
            isA(EnvironmentContext.class));

    thrown.expect(exceptionSameInstanceMatcher(ioException));
    everrestServlet.service(request, response);
  }

  @Test
  public void wrapsCauseOfUnhandledExceptionWithServletException() throws Exception {
    Exception exception = new Exception();
    UnhandledException unhandledException = new UnhandledException(exception);

    doThrow(unhandledException)
        .when(everrestProcessor)
        .process(
            isA(ServletContainerRequest.class),
            isA(ContainerResponse.class),
            isA(EnvironmentContext.class));

    thrown.expect(ServletException.class);
    thrown.expectCause(exceptionSameInstanceMatcher(exception));
    everrestServlet.service(request, response);
  }

  @Test
  public void sendsErrorStatusWhenStatusIsSetInUnhandledException() throws Exception {
    UnhandledException unhandledException = new UnhandledException(403);

    doThrow(unhandledException)
        .when(everrestProcessor)
        .process(
            isA(ServletContainerRequest.class),
            isA(ContainerResponse.class),
            isA(EnvironmentContext.class));

    everrestServlet.service(request, response);
    verify(response).sendError(403);
  }

  private BaseMatcher<Throwable> exceptionSameInstanceMatcher(Exception expectedException) {
    return new BaseMatcher<Throwable>() {
      @Override
      public boolean matches(Object item) {
        return item == expectedException;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(String.format("Expected exception: %s", expectedException));
      }
    };
  }
}
