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
package org.everrest.core;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.everrest.core.ApplicationContext.anApplicationContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.LifecycleComponent;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousMethodInvoker;
import org.everrest.core.impl.method.DefaultMethodInvoker;
import org.everrest.core.impl.method.MethodInvokerDecorator;
import org.everrest.core.impl.method.MethodInvokerDecoratorFactory;
import org.everrest.core.impl.method.OptionsRequestMethodInvoker;
import org.everrest.core.impl.uri.PathSegmentImpl;
import org.everrest.core.method.MethodInvoker;
import org.everrest.core.resource.GenericResourceMethod;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.servlet.ServletContainerRequest;
import org.everrest.core.tools.SimpleSecurityContext;
import org.everrest.core.tools.WebApplicationDeclaredRoles;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ApplicationContextTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private GenericContainerRequest request;
  private GenericContainerResponse response;
  private ProviderBinder providers;
  private EnvironmentContext environmentContext;

  private ApplicationContext applicationContext;

  @Before
  public void setUp() throws Exception {
    request = mock(ServletContainerRequest.class);
    response = mock(GenericContainerResponse.class);
    providers = mock(ProviderBinder.class);
    environmentContext = mock(EnvironmentContext.class);

    EnvironmentContext.setCurrent(environmentContext);

    applicationContext =
        anApplicationContext()
            .withRequest(request)
            .withResponse(response)
            .withProviders(providers)
            .build();
  }

  @Test
  public void addsMatchedResourceAtBeginningOfMatchedResourcesList() {
    Object resourceOne = new Object();
    Object resourceTwo = new Object();
    applicationContext.addMatchedResource(resourceOne);
    applicationContext.addMatchedResource(resourceTwo);

    assertEquals(newArrayList(resourceTwo, resourceOne), applicationContext.getMatchedResources());
  }

  @Test
  public void addsMatchedUriAtBeginningOfMatchedUrisList() {
    String uriOne = "a/%20b%20";
    String uriTwo = "c/d";
    applicationContext.addMatchedURI(uriOne);
    applicationContext.addMatchedURI(uriTwo);

    assertEquals(newArrayList("c/d", "a/ b "), applicationContext.getMatchedURIs());
    assertEquals(newArrayList(uriTwo, uriOne), applicationContext.getMatchedURIs(false));
  }

  @Test
  public void getsAbsolutePathAndRemovesQueryParametersAndFragment() {
    when(request.getRequestUri()).thenReturn(URI.create("http://localhost:8080/a/b/c?q#f"));

    assertEquals(URI.create("http://localhost:8080/a/b/c"), applicationContext.getAbsolutePath());
  }

  @Test
  public void getsAbsolutePathBuilderAndRemovesQueryParametersAndFragment() {
    when(request.getRequestUri()).thenReturn(URI.create("http://localhost:8080/a/b/c?q#f"));

    assertEquals(
        URI.create("http://localhost:8080/a/b/c"),
        applicationContext.getAbsolutePathBuilder().build());
  }

  @Test
  public void getsNonNullAttributesMap() {
    assertNotNull(applicationContext.getAttributes());
  }

  @Test
  public void getsBaseUri() {
    when(request.getBaseUri()).thenReturn(URI.create("http://localhost:8080/servlet"));

    assertEquals(URI.create("http://localhost:8080/servlet"), applicationContext.getBaseUri());
  }

  @Test
  public void getsBaseUriBuilder() {
    when(request.getBaseUri()).thenReturn(URI.create("http://localhost:8080/servlet/"));

    assertEquals(
        URI.create("http://localhost:8080/servlet/"),
        applicationContext.getBaseUriBuilder().build());
  }

  @Test
  public void getsContainerRequest() {
    assertSame(request, applicationContext.getContainerRequest());
  }

  @Test
  public void getsRequest() {
    assertSame(request, applicationContext.getRequest());
  }

  @Test
  public void getsContainerResponse() {
    assertSame(response, applicationContext.getContainerResponse());
  }

  @Test
  public void getsHttpHeaders() {
    assertSame(request, applicationContext.getHttpHeaders());
  }

  @Test
  public void resolvesUriAgainstBaseUri() {
    when(request.getBaseUri()).thenReturn(URI.create("http://localhost:8080/a/b/"));

    assertEquals(
        URI.create("http://localhost:8080/a/b/c"), applicationContext.resolve(URI.create("c")));
  }

  @Test
  public void relativizesUriAgainstRequestUri() {
    when(request.getBaseUri()).thenReturn(URI.create("http://localhost:8080/a/b/"));
    when(request.getRequestUri()).thenReturn(URI.create("http://localhost:8080/a/b/c/d"));

    assertEquals(URI.create("y"), applicationContext.relativize(URI.create("c/d/y")));
  }

  @Test
  public void getsMethodInvoker() {
    when(request.getRequestUri()).thenReturn(URI.create("http://localhost:8080/a/b"));
    when(request.getRequestHeaders()).thenReturn(new MultivaluedHashMap<>());
    when(request.getMethod()).thenReturn("GET");

    MethodInvoker methodInvoker =
        applicationContext.getMethodInvoker(mock(GenericResourceMethod.class));
    assertTrue(
        String.format("Expected instance of DefaultMethodInvoker but actual is %s", methodInvoker),
        methodInvoker instanceof DefaultMethodInvoker);
  }

  @Test
  public void getsDecoratedMethodInvoker() {
    MethodInvokerDecoratorFactory methodInvokerDecoratorFactory =
        mock(MethodInvokerDecoratorFactory.class);
    when(methodInvokerDecoratorFactory.makeDecorator(isA(MethodInvoker.class)))
        .thenAnswer(
            invocation -> new MethodInvokerDecorator((MethodInvoker) invocation.getArguments()[0]));
    applicationContext =
        anApplicationContext()
            .withRequest(request)
            .withResponse(response)
            .withProviders(providers)
            .withMethodInvokerDecoratorFactory(methodInvokerDecoratorFactory)
            .build();

    when(request.getRequestUri()).thenReturn(URI.create("http://localhost:8080/a/b"));
    when(request.getRequestHeaders()).thenReturn(new MultivaluedHashMap<>());
    when(request.getMethod()).thenReturn("GET");

    MethodInvoker methodInvoker =
        applicationContext.getMethodInvoker(mock(GenericResourceMethod.class));
    assertTrue(
        String.format(
            "Expected instance of MethodInvokerDecorator but actual is %s", methodInvoker),
        methodInvoker instanceof MethodInvokerDecorator);
  }

  @Test
  public void getsMethodInvokerFor_OPTIONS_Request() {
    when(request.getMethod()).thenReturn("OPTIONS");

    MethodInvoker methodInvoker =
        applicationContext.getMethodInvoker(mock(GenericResourceMethod.class));
    assertTrue(
        String.format(
            "Expected instance of OptionsRequestMethodInvoker but actual is %s", methodInvoker),
        methodInvoker instanceof OptionsRequestMethodInvoker);
  }

  @Test
  public void getsAsynchronousMethodInvokerWhenCallerProvidesAsyncQueryParameter() {
    when(request.getRequestUri()).thenReturn(URI.create("http://localhost:8080/a/b?async=true"));
    when(request.getRequestHeaders()).thenReturn(new MultivaluedHashMap<>());
    when(request.getMethod()).thenReturn("GET");

    when(providers.getContextResolver(AsynchronousJobPool.class, null))
        .thenReturn(mock(AsynchronousJobPool.class));

    MethodInvoker methodInvoker =
        applicationContext.getMethodInvoker(mock(ResourceMethodDescriptor.class));
    assertTrue(
        String.format(
            "Expected instance of AsynchronousMethodInvoker but actual is %s", methodInvoker),
        methodInvoker instanceof AsynchronousMethodInvoker);
  }

  @Test
  public void getsAsynchronousMethodInvokerWhenCallerProvidesAsyncHeaderParameter() {
    when(request.getRequestUri()).thenReturn(URI.create("http://localhost:8080/a/b"));
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.putSingle("x-everrest-async", "true");
    when(request.getRequestHeaders()).thenReturn(headers);
    when(request.getMethod()).thenReturn("GET");

    when(providers.getContextResolver(AsynchronousJobPool.class, null))
        .thenReturn(mock(AsynchronousJobPool.class));

    MethodInvoker methodInvoker =
        applicationContext.getMethodInvoker(mock(ResourceMethodDescriptor.class));
    assertTrue(
        String.format(
            "Expected instance of AsynchronousMethodInvoker but actual is %s", methodInvoker),
        methodInvoker instanceof AsynchronousMethodInvoker);
  }

  @Test
  public void
      throwsExceptionWhenAsynchronousFeatureIsNotConfiguredButCallerRequestsAsynchronousInvocation() {
    when(request.getRequestUri()).thenReturn(URI.create("http://localhost:8080/a/b?async=true"));
    when(request.getRequestHeaders()).thenReturn(new MultivaluedHashMap<>());
    when(request.getMethod()).thenReturn("GET");

    thrown.expect(IllegalStateException.class);
    applicationContext.getMethodInvoker(mock(ResourceMethodDescriptor.class));
  }

  @Test
  public void getsEncodedPath() {
    when(request.getBaseUri()).thenReturn(URI.create("http://localhost:8080/servlet"));
    when(request.getRequestUri()).thenReturn(URI.create("http://localhost:8080/servlet/a/%20b"));

    assertEquals("/a/%20b", applicationContext.getPath(false));
  }

  @Test
  public void getsDecodedPath() {
    when(request.getBaseUri()).thenReturn(URI.create("http://localhost:8080/servlet"));
    when(request.getRequestUri()).thenReturn(URI.create("http://localhost:8080/servlet/a/%20b"));

    assertEquals("/a/ b", applicationContext.getPath());
  }

  @Test
  public void getsEncodedPathParameters() {
    applicationContext.getParameterValues().addAll(newArrayList("x", "%20y%20"));
    applicationContext.setParameterNames(newArrayList("a", "b"));

    MultivaluedMap<String, String> expectedParameters = new MultivaluedHashMap<>();
    expectedParameters.putSingle("a", "x");
    expectedParameters.putSingle("b", "%20y%20");

    assertEquals(expectedParameters, applicationContext.getPathParameters(false));
  }

  @Test
  public void getsDecodedPathParameters() {
    applicationContext.getParameterValues().addAll(newArrayList("x", "%20y%20"));
    applicationContext.setParameterNames(newArrayList("a", "b"));

    MultivaluedMap<String, String> expectedParameters = new MultivaluedHashMap<>();
    expectedParameters.putSingle("a", "x");
    expectedParameters.putSingle("b", " y ");

    assertEquals(expectedParameters, applicationContext.getPathParameters());
  }

  @Test
  public void getsEncodedPathSegments() {
    when(request.getBaseUri()).thenReturn(URI.create("http://localhost:8080/servlet"));
    when(request.getRequestUri())
        .thenReturn(URI.create("http://localhost:8080/servlet/a/%20b;x=y"));

    List<PathSegment> expectedPathSegments =
        newArrayList(
            PathSegmentImpl.fromString("a", false), PathSegmentImpl.fromString("%20b;x=y", false));
    assertEquals(expectedPathSegments, applicationContext.getPathSegments(false));
  }

  @Test
  public void getsDecodedPathSegments() {
    when(request.getBaseUri()).thenReturn(URI.create("http://localhost:8080/servlet"));
    when(request.getRequestUri())
        .thenReturn(URI.create("http://localhost:8080/servlet/a/%20b;x=y"));

    List<PathSegment> expectedPathSegments =
        newArrayList(
            PathSegmentImpl.fromString("a", true), PathSegmentImpl.fromString("%20b;x=y", true));
    assertEquals(expectedPathSegments, applicationContext.getPathSegments());
  }

  @Test
  public void getsEncodedQueryParameters() {
    when(request.getRequestUri())
        .thenReturn(URI.create("http://localhost:8080/servlet/a?a=x&b=%20y%20"));

    MultivaluedMap<String, String> expectedQueryParameters = new MultivaluedHashMap<>();
    expectedQueryParameters.putSingle("a", "x");
    expectedQueryParameters.putSingle("b", "%20y%20");

    assertEquals(expectedQueryParameters, applicationContext.getQueryParameters(false));
  }

  @Test
  public void getsDecodedQueryParameters() {
    when(request.getRequestUri())
        .thenReturn(URI.create("http://localhost:8080/servlet/a?a=x&b=%20y%20"));

    MultivaluedMap<String, String> expectedQueryParameters = new MultivaluedHashMap<>();
    expectedQueryParameters.putSingle("a", "x");
    expectedQueryParameters.putSingle("b", " y ");

    assertEquals(expectedQueryParameters, applicationContext.getQueryParameters());
  }

  @Test
  public void getsNonPropertyMap() {
    assertNotNull(applicationContext.getProperties());
  }

  @Test
  public void consumesProviderBinderAndReturnsItOnNextCall() {
    ProviderBinder providers = mock(ProviderBinder.class);
    applicationContext.setProviders(providers);

    assertSame(providers, applicationContext.getProviders());
  }

  @Test
  public void getsSecurityContext() {
    when(request.getRequestUri()).thenReturn(URI.create("http://localhost:8080/a/b"));
    when(request.getRequestHeaders()).thenReturn(new MultivaluedHashMap<>());

    assertSame(request, applicationContext.getSecurityContext());
  }

  @Test
  public void getsSimpleSecurityContextWhenCallerRequestsAsynchronousInvocation() {
    when(request.getRequestUri()).thenReturn(URI.create("http://localhost:8080/a/b?async=true"));
    when(request.getRequestHeaders()).thenReturn(new MultivaluedHashMap<>());

    SimpleSecurityContext securityContext =
        (SimpleSecurityContext) applicationContext.getSecurityContext();
    assertNotNull(securityContext);
  }

  @Test
  public void
      getsSimpleSecurityContextWithPrincipalFromRequestWhenCallerRequestsAsynchronousInvocation() {
    when(request.getRequestUri()).thenReturn(URI.create("http://localhost:8080/a/b?async=true"));
    when(request.getRequestHeaders()).thenReturn(new MultivaluedHashMap<>());
    when(request.getAuthenticationScheme()).thenReturn("BASIC_AUTH");

    Principal principal = mockPrincipal("andrew");
    when(request.getUserPrincipal()).thenReturn(principal);

    SimpleSecurityContext securityContext =
        (SimpleSecurityContext) applicationContext.getSecurityContext();
    assertNotNull(securityContext);
    assertNotNull(securityContext.getUserPrincipal());
    assertEquals(principal.getName(), securityContext.getUserPrincipal().getName());
    assertEquals("BASIC_AUTH", securityContext.getAuthenticationScheme());
  }

  @Test
  public void
      getsSimpleSecurityContextWithPrincipalFromRequestAndRolesFromWebApplicationDeclaredRolesWhenCallerRequestsAsynchronousInvocation() {
    when(request.getRequestUri()).thenReturn(URI.create("http://localhost:8080/a/b?async=true"));
    when(request.getRequestHeaders()).thenReturn(new MultivaluedHashMap<>());
    when(request.getAuthenticationScheme()).thenReturn("BASIC_AUTH");
    when(request.isUserInRole("user")).thenReturn(true);

    Principal principal = mockPrincipal("andrew");
    when(request.getUserPrincipal()).thenReturn(principal);
    WebApplicationDeclaredRoles webApplicationDeclaredRoles =
        mockWebApplicationDeclaredRoles("user");
    when(environmentContext.get(WebApplicationDeclaredRoles.class))
        .thenReturn(webApplicationDeclaredRoles);

    SimpleSecurityContext securityContext =
        (SimpleSecurityContext) applicationContext.getSecurityContext();
    assertNotNull(securityContext);
    assertNotNull(securityContext.getUserPrincipal());
    assertEquals(principal.getName(), securityContext.getUserPrincipal().getName());
    assertEquals("BASIC_AUTH", securityContext.getAuthenticationScheme());
    assertTrue(securityContext.isUserInRole("user"));
    assertFalse(securityContext.isUserInRole("admin"));
  }

  private Principal mockPrincipal(String name) {
    Principal principal = mock(Principal.class);
    when(principal.getName()).thenReturn(name);
    return principal;
  }

  private WebApplicationDeclaredRoles mockWebApplicationDeclaredRoles(String... roles) {
    WebApplicationDeclaredRoles webApplicationDeclaredRoles =
        mock(WebApplicationDeclaredRoles.class);
    when(webApplicationDeclaredRoles.getDeclaredRoles()).thenReturn(newHashSet(roles));
    return webApplicationDeclaredRoles;
  }

  @Test
  public void getsNonNullEverrestConfiguration() {
    assertNotNull(applicationContext.getEverrestConfiguration());
  }

  @Test
  public void consumesEverrestConfigurationAndReturnsItOnNextCall() {
    EverrestConfiguration configuration = mock(EverrestConfiguration.class);
    applicationContext.setEverrestConfiguration(configuration);
    assertSame(configuration, applicationContext.getEverrestConfiguration());
  }

  @Test
  public void notifiesLifecycleComponentWhenStopped() {
    LifecycleComponent lifecycleComponent = mock(LifecycleComponent.class);
    applicationContext
        .getAttributes()
        .put("org.everrest.lifecycle.PerRequest", newArrayList(lifecycleComponent));

    applicationContext.stop();

    verify(lifecycleComponent).destroy();
  }

  @Test
  public void getsUriInfo() {
    assertSame(applicationContext, applicationContext.getUriInfo());
  }

  @Test
  public void consumesDependencySupplierAndReturnsItOnNextCall() {
    DependencySupplier dependencySupplier = mock(DependencySupplier.class);
    applicationContext.setDependencySupplier(dependencySupplier);
    assertSame(dependencySupplier, applicationContext.getDependencySupplier());
  }

  @Test
  public void consumesApplicationAndReturnsItOnNextCall() {
    Application application = mock(Application.class);
    applicationContext.setApplication(application);
    assertSame(application, applicationContext.getApplication());
  }
}
