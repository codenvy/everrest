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
package org.everrest.core.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResourceBinder;
import org.everrest.core.ResponseFilter;
import org.everrest.core.impl.provider.StringEntityProvider;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.resource.GenericResourceMethod;
import org.junit.Before;
import org.junit.Test;

public class RestComponentResolverTest {
  private ResourceBinder resources;
  private ProviderBinder providers;

  private RestComponentResolver componentResolver;

  @Before
  public void setUp() throws Exception {
    resources = mock(ResourceBinder.class);
    providers = mock(ProviderBinder.class);

    componentResolver = new RestComponentResolver(resources, providers);
  }

  @Test
  public void resolvesPerRequestResource() {
    componentResolver.addPerRequest(Resource.class);
    verify(resources).addResource(Resource.class, null);
  }

  @Test
  public void resolvesSingletonResource() {
    Resource resource = new Resource();
    componentResolver.addSingleton(resource);
    verify(resources).addResource(resource, null);
  }

  @Path("a")
  public static class Resource {}

  @Test
  public void resolvesPerRequestExceptionMapper() {
    componentResolver.addPerRequest(RuntimeExceptionMapper.class);
    verify(providers).addExceptionMapper(RuntimeExceptionMapper.class);
  }

  @Test
  public void resolvesSingletonExceptionMapper() {
    ExceptionMapper exceptionMapper = new RuntimeExceptionMapper();
    componentResolver.addSingleton(exceptionMapper);
    verify(providers).addExceptionMapper(exceptionMapper);
  }

  @Provider
  public static class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
    @Override
    public Response toResponse(RuntimeException exception) {
      return null;
    }
  }

  @Test
  public void resolvesPerRequestContextResolver() {
    componentResolver.addPerRequest(ContextResolverText.class);
    verify(providers).addContextResolver(ContextResolverText.class);
  }

  @Test
  public void resolvesSingletonContextResolver() {
    ContextResolver contextResolver = new ContextResolverText();
    componentResolver.addSingleton(contextResolver);
    verify(providers).addContextResolver(contextResolver);
  }

  @Provider
  @Produces("text/plain")
  public static class ContextResolverText implements ContextResolver<String> {
    public String getContext(Class<?> type) {
      return null;
    }
  }

  @Test
  public void resolvesPerRequestMessageBodyReader() {
    componentResolver.addPerRequest(StringEntityProvider.class);
    verify(providers).addMessageBodyReader(StringEntityProvider.class);
  }

  @Test
  public void resolvesSingletonMessageBodyReader() {
    MessageBodyReader<String> messageBodyReader = new StringEntityProvider();

    componentResolver.addSingleton(messageBodyReader);

    verify(providers).addMessageBodyReader(messageBodyReader);
  }

  @Test
  public void resolvesPerRequestMessageBodyWriter() {
    componentResolver.addPerRequest(StringEntityProvider.class);
    verify(providers).addMessageBodyWriter(StringEntityProvider.class);
  }

  @Test
  public void resolvesSingletonMessageBodyWriter() {
    MessageBodyWriter<String> messageBodyWriter = new StringEntityProvider();
    componentResolver.addSingleton(messageBodyWriter);
    verify(providers).addMessageBodyWriter(messageBodyWriter);
  }

  @Test
  public void resolvesPerRequestMethodInvokerFilter() {
    componentResolver.addPerRequest(AllMatchesMethodInvokerFilter.class);
    verify(providers).addMethodInvokerFilter(AllMatchesMethodInvokerFilter.class);
  }

  @Test
  public void resolvesSingletonMethodInvokerFilter() {
    MethodInvokerFilter methodInvokerFilter = new AllMatchesMethodInvokerFilter();
    componentResolver.addSingleton(methodInvokerFilter);
    verify(providers).addMethodInvokerFilter(methodInvokerFilter);
  }

  @Filter
  public static class AllMatchesMethodInvokerFilter implements MethodInvokerFilter {
    @Override
    public void accept(GenericResourceMethod genericResourceMethod, Object[] params) {}
  }

  @Test
  public void resolvesPerRequestRequestFilter() {
    componentResolver.addPerRequest(AllMatchesRequestFilter.class);
    verify(providers).addRequestFilter(AllMatchesRequestFilter.class);
  }

  @Test
  public void resolvesSingletonRequestFilter() {
    RequestFilter requestFilter = new AllMatchesRequestFilter();
    componentResolver.addSingleton(requestFilter);
    verify(providers).addRequestFilter(requestFilter);
  }

  @Filter
  public static class AllMatchesRequestFilter implements RequestFilter {
    @Override
    public void doFilter(GenericContainerRequest request) {}
  }

  @Test
  public void resolvesPerResponseResponseFilter() {
    componentResolver.addPerRequest(AllMatchesResponseFilter.class);
    verify(providers).addResponseFilter(AllMatchesResponseFilter.class);
  }

  @Test
  public void resolvesSingletonResponseFilter() {
    ResponseFilter responseFilter = new AllMatchesResponseFilter();
    componentResolver.addSingleton(responseFilter);
    verify(providers).addResponseFilter(responseFilter);
  }

  @Filter
  public static class AllMatchesResponseFilter implements ResponseFilter {
    @Override
    public void doFilter(GenericContainerResponse response) {}
  }
}
