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
package org.everrest.core.impl.method;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import org.everrest.core.ApplicationContext;
import org.everrest.core.InitialProperties;
import org.everrest.core.Parameter;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.ProviderBinder;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class ContextParameterResolverTest {
  private Parameter parameter;
  private ApplicationContext applicationContext;
  private EnvironmentContext environmentContext;
  private ContextParameterResolver contextParameterResolver;

  @Before
  public void setUp() throws Exception {
    parameter = mock(Parameter.class);
    applicationContext = mock(ApplicationContext.class);

    contextParameterResolver = new ContextParameterResolver();
    environmentContext = mock(EnvironmentContext.class);
    EnvironmentContext.setCurrent(environmentContext);
  }

  @Test
  public void resolvesHttpHeaders() throws Exception {
    HttpHeaders httpHeaders = mock(HttpHeaders.class);
    when(applicationContext.getHttpHeaders()).thenReturn(httpHeaders);
    when(parameter.getParameterClass()).thenReturn((Class) HttpHeaders.class);

    assertSame(httpHeaders, contextParameterResolver.resolve(parameter, applicationContext));
    verify(environmentContext, never()).get(any(Class.class));
  }

  @Test
  public void resolvesSecurityContext() throws Exception {
    SecurityContext securityContext = mock(SecurityContext.class);
    when(applicationContext.getSecurityContext()).thenReturn(securityContext);
    when(parameter.getParameterClass()).thenReturn((Class) SecurityContext.class);

    assertSame(securityContext, contextParameterResolver.resolve(parameter, applicationContext));
    verify(environmentContext, never()).get(any(Class.class));
  }

  @Test
  public void resolvesRequest() throws Exception {
    Request request = mock(Request.class);
    when(applicationContext.getRequest()).thenReturn(request);
    when(parameter.getParameterClass()).thenReturn((Class) Request.class);

    assertSame(request, contextParameterResolver.resolve(parameter, applicationContext));
    verify(environmentContext, never()).get(any(Class.class));
  }

  @Test
  public void resolvesUriInfo() throws Exception {
    UriInfo uriInfo = mock(UriInfo.class);
    when(applicationContext.getUriInfo()).thenReturn(uriInfo);
    when(parameter.getParameterClass()).thenReturn((Class) UriInfo.class);

    assertSame(uriInfo, contextParameterResolver.resolve(parameter, applicationContext));
    verify(environmentContext, never()).get(any(Class.class));
  }

  @Test
  public void resolvesProviders() throws Exception {
    ProviderBinder providers = mock(ProviderBinder.class);
    when(applicationContext.getProviders()).thenReturn(providers);
    when(parameter.getParameterClass()).thenReturn((Class) Providers.class);

    assertSame(providers, contextParameterResolver.resolve(parameter, applicationContext));
    verify(environmentContext, never()).get(any(Class.class));
  }

  @Test
  public void resolvesApplication() throws Exception {
    Application application = mock(Application.class);
    when(applicationContext.getApplication()).thenReturn(application);
    when(parameter.getParameterClass()).thenReturn((Class) Application.class);

    assertSame(application, contextParameterResolver.resolve(parameter, applicationContext));
    verify(environmentContext, never()).get(any(Class.class));
  }

  @Test
  public void resolvesInitialProperties() throws Exception {
    InitialProperties initialProperties = mock(InitialProperties.class);
    when(applicationContext.getInitialProperties()).thenReturn(initialProperties);
    when(parameter.getParameterClass()).thenReturn((Class) InitialProperties.class);

    assertSame(initialProperties, contextParameterResolver.resolve(parameter, applicationContext));
    verify(environmentContext, never()).get(any(Class.class));
  }

  @Test
  public void usesEnvironmentContextToResolveContextParameters() throws Exception {
    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    when(environmentContext.get(eq(HttpServletRequest.class))).thenReturn(servletRequest);
    when(parameter.getParameterClass()).thenReturn((Class) HttpServletRequest.class);

    assertSame(servletRequest, contextParameterResolver.resolve(parameter, applicationContext));

    verify(environmentContext, times(1)).get(any(Class.class));
  }
}
