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

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.util.List;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import org.everrest.core.ApplicationContext;
import org.everrest.core.DependencySupplier;
import org.everrest.core.Parameter;
import org.everrest.core.impl.method.ParameterResolver;
import org.everrest.core.impl.method.ParameterResolverFactory;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatcher;

public class ConstructorDescriptorImplTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private ParameterResolverFactory parameterResolverFactory;
  private ParameterResolver<PathParam> pathParameterResolver;
  private ParameterResolver<QueryParam> queryParameterResolver;
  private ParameterResolver<MatrixParam> matrixParameterResolver;
  private ParameterResolver<CookieParam> cookieParameterResolver;
  private ParameterResolver<HeaderParam> headerParameterResolver;
  private ApplicationContext applicationContext;
  private DependencySupplier dependencySupplier;

  @Before
  public void setUp() throws Exception {
    mockParameterResolverFactory();
    mockApplicationContext();
  }

  @After
  public void tearDown() throws Exception {
    Resource.thrownByDefaultConstructorIfSet = null;
  }

  private void mockApplicationContext() {
    dependencySupplier = mock(DependencySupplier.class);
    applicationContext = mock(ApplicationContext.class);
    when(applicationContext.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
    when(applicationContext.getDependencySupplier()).thenReturn(dependencySupplier);
    ApplicationContext.setCurrent(applicationContext);
  }

  @SuppressWarnings("unchecked")
  private void mockParameterResolverFactory() {
    parameterResolverFactory = mock(ParameterResolverFactory.class);
    pathParameterResolver = mock(ParameterResolver.class);
    queryParameterResolver = mock(ParameterResolver.class);
    matrixParameterResolver = mock(ParameterResolver.class);
    cookieParameterResolver = mock(ParameterResolver.class);
    headerParameterResolver = mock(ParameterResolver.class);

    when(parameterResolverFactory.createParameterResolver(isA(PathParam.class)))
        .thenReturn(pathParameterResolver);
    when(parameterResolverFactory.createParameterResolver(isA(QueryParam.class)))
        .thenReturn(queryParameterResolver);
    when(parameterResolverFactory.createParameterResolver(isA(MatrixParam.class)))
        .thenReturn(matrixParameterResolver);
    when(parameterResolverFactory.createParameterResolver(isA(CookieParam.class)))
        .thenReturn(cookieParameterResolver);
    when(parameterResolverFactory.createParameterResolver(isA(HeaderParam.class)))
        .thenReturn(headerParameterResolver);
  }

  @Test
  public void createsConstructorDescriptorForSimpleConstructor() throws Exception {
    Constructor<Resource> constructor = Resource.class.getConstructor();
    ConstructorDescriptorImpl constructorDescriptor =
        new ConstructorDescriptorImpl(constructor, parameterResolverFactory);

    assertSame(constructor, constructorDescriptor.getConstructor());
    assertTrue(constructorDescriptor.getParameters().isEmpty());
  }

  @Test
  public void createsInstanceOfClassFromConstructorDescriptorForSimpleConstructor()
      throws Exception {
    Constructor<Resource> constructor = Resource.class.getConstructor();
    ConstructorDescriptorImpl constructorDescriptor =
        new ConstructorDescriptorImpl(constructor, parameterResolverFactory);

    assertTrue(constructorDescriptor.createInstance(applicationContext) instanceof Resource);
  }

  @Test
  public void createsConstructorDescriptorForConstructorWithRequestParameters() throws Exception {
    Constructor<Resource> constructor =
        Resource.class.getConstructor(
            String.class, String.class, String.class, String.class, String.class);
    ConstructorDescriptorImpl constructorDescriptor =
        new ConstructorDescriptorImpl(constructor, parameterResolverFactory);

    assertSame(constructor, constructorDescriptor.getConstructor());
    List<Parameter> parameters = constructorDescriptor.getParameters();
    assertEquals(5, parameters.size());

    Parameter pathParameter = parameters.get(0);
    assertEquals(PathParam.class, pathParameter.getAnnotation().annotationType());
    assertEquals(String.class, pathParameter.getParameterClass());
    assertEquals(1, pathParameter.getAnnotations().length);
    assertNull(pathParameter.getDefaultValue());
    assertFalse(pathParameter.isEncoded());

    Parameter queryParameter = parameters.get(1);
    assertEquals(QueryParam.class, queryParameter.getAnnotation().annotationType());
    assertEquals(String.class, queryParameter.getParameterClass());
    assertEquals(2, queryParameter.getAnnotations().length);
    assertNull(queryParameter.getDefaultValue());
    assertTrue(queryParameter.isEncoded());

    Parameter matrixParameter = parameters.get(2);
    assertEquals(MatrixParam.class, matrixParameter.getAnnotation().annotationType());
    assertEquals(String.class, matrixParameter.getParameterClass());
    assertEquals(1, matrixParameter.getAnnotations().length);
    assertNull(matrixParameter.getDefaultValue());
    assertFalse(matrixParameter.isEncoded());

    Parameter cookieParameter = parameters.get(3);
    assertEquals(CookieParam.class, cookieParameter.getAnnotation().annotationType());
    assertEquals(String.class, cookieParameter.getParameterClass());
    assertEquals(1, cookieParameter.getAnnotations().length);
    assertNull(cookieParameter.getDefaultValue());
    assertFalse(cookieParameter.isEncoded());

    Parameter headerParameter = parameters.get(4);
    assertEquals(HeaderParam.class, headerParameter.getAnnotation().annotationType());
    assertEquals(String.class, headerParameter.getParameterClass());
    assertEquals(2, headerParameter.getAnnotations().length);
    assertEquals("default", headerParameter.getDefaultValue());
    assertFalse(headerParameter.isEncoded());
  }

  @Test
  public void createsInstanceOfClassFromConstructorDescriptorForConstructorWithRequestParameters()
      throws Exception {
    when(pathParameterResolver.resolve(isA(Parameter.class), eq(applicationContext)))
        .thenReturn("path parameter");
    when(queryParameterResolver.resolve(isA(Parameter.class), eq(applicationContext)))
        .thenReturn("query parameter");
    when(matrixParameterResolver.resolve(isA(Parameter.class), eq(applicationContext)))
        .thenReturn("matrix parameter");
    when(cookieParameterResolver.resolve(isA(Parameter.class), eq(applicationContext)))
        .thenReturn("cookie parameter");
    when(headerParameterResolver.resolve(isA(Parameter.class), eq(applicationContext)))
        .thenReturn("header parameter");

    Constructor<Resource> constructor =
        Resource.class.getConstructor(
            String.class, String.class, String.class, String.class, String.class);
    ConstructorDescriptorImpl constructorDescriptor =
        new ConstructorDescriptorImpl(constructor, parameterResolverFactory);

    Object instance = constructorDescriptor.createInstance(applicationContext);
    assertTrue(instance instanceof Resource);
    Resource resource = (Resource) instance;
    assertEquals("path parameter", resource.pathParam);
    assertEquals("query parameter", resource.queryParam);
    assertEquals("matrix parameter", resource.matrixParam);
    assertEquals("cookie parameter", resource.cookieParam);
    assertEquals("header parameter", resource.headerParam);
  }

  @Test
  public void createsConstructorDescriptorForConstructorWithExternalDependency() throws Exception {
    Constructor<Resource> constructor = Resource.class.getConstructor(Dependency.class);
    ConstructorDescriptorImpl constructorDescriptor =
        new ConstructorDescriptorImpl(constructor, parameterResolverFactory);

    assertSame(constructor, constructorDescriptor.getConstructor());
    assertEquals(1, constructorDescriptor.getParameters().size());

    Parameter parameter = constructorDescriptor.getParameters().get(0);
    assertEquals(Dependency.class, parameter.getParameterClass());
    assertNull(parameter.getAnnotation());
    assertEquals(0, parameter.getAnnotations().length);
    assertNull(parameter.getDefaultValue());
  }

  @Test
  public void createsInstanceOfClassFromConstructorDescriptorForConstructorWithExternalDependency()
      throws Exception {
    Dependency dependency = new Dependency();
    when(dependencySupplier.getInstance(
            argThat(
                (ArgumentMatcher<ConstructorParameter>)
                    argument ->
                        ((ConstructorParameter) argument).getParameterClass() == Dependency.class)))
        .thenReturn(dependency);

    Constructor<Resource> constructor = Resource.class.getConstructor(Dependency.class);
    ConstructorDescriptorImpl constructorDescriptor =
        new ConstructorDescriptorImpl(constructor, parameterResolverFactory);

    Object instance = constructorDescriptor.createInstance(applicationContext);
    assertTrue(instance instanceof Resource);
    Resource resource = (Resource) instance;
    assertSame(dependency, resource.dependency);
  }

  @Test
  public void
      failsCreateInstanceOfClassFromConstructorDescriptorForConstructorWithExternalDependencyWhenDependencySupplierIsNotSetInApplicationContext()
          throws Exception {
    when(applicationContext.getDependencySupplier()).thenReturn(null);

    Constructor<Resource> constructor = Resource.class.getConstructor(Dependency.class);
    ConstructorDescriptorImpl constructorDescriptor =
        new ConstructorDescriptorImpl(constructor, parameterResolverFactory);

    thrown.expect(RuntimeException.class);
    constructorDescriptor.createInstance(applicationContext);
  }

  @Test
  public void
      failsCreateInstanceOfClassFromConstructorDescriptorForConstructorWithExternalDependencyWhenDependencySupplierCanNotResolveRequiredDependency()
          throws Exception {
    when(dependencySupplier.getInstance(
            argThat(
                (ArgumentMatcher<ConstructorParameter>)
                    argument ->
                        ((ConstructorParameter) argument).getParameterClass() == Dependency.class)))
        .thenReturn(null);

    Constructor<Resource> constructor = Resource.class.getConstructor(Dependency.class);
    ConstructorDescriptorImpl constructorDescriptor =
        new ConstructorDescriptorImpl(constructor, parameterResolverFactory);

    thrown.expect(RuntimeException.class);
    constructorDescriptor.createInstance(applicationContext);
  }

  @Test
  public void wrapsNonWebApplicationExceptionThrownByConstructorWithInternalException()
      throws Exception {
    Exception thrownByConstructor = new Exception();
    Resource.thrownByDefaultConstructorIfSet = thrownByConstructor;

    Constructor<Resource> constructor = Resource.class.getConstructor();
    ConstructorDescriptorImpl constructorDescriptor =
        new ConstructorDescriptorImpl(constructor, parameterResolverFactory);

    thrown.expect(InternalException.class);
    thrown.expectCause(exceptionSameInstanceMatcher(thrownByConstructor));
    constructorDescriptor.createInstance(applicationContext);
  }

  @Test
  public void rethrowsInternalExceptionThrownByConstructor() throws Exception {
    InternalException thrownByConstructor = new InternalException(new Exception());
    Resource.thrownByDefaultConstructorIfSet = thrownByConstructor;

    Constructor<Resource> constructor = Resource.class.getConstructor();
    ConstructorDescriptorImpl constructorDescriptor =
        new ConstructorDescriptorImpl(constructor, parameterResolverFactory);

    thrown.expect(exceptionSameInstanceMatcher(thrownByConstructor));
    constructorDescriptor.createInstance(applicationContext);
  }

  @Test
  public void rethrowsWebApplicationExceptionThrownByConstructor() throws Exception {
    WebApplicationException thrownByConstructor = new WebApplicationException();
    Resource.thrownByDefaultConstructorIfSet = thrownByConstructor;

    Constructor<Resource> constructor = Resource.class.getConstructor();
    ConstructorDescriptorImpl constructorDescriptor =
        new ConstructorDescriptorImpl(constructor, parameterResolverFactory);

    thrown.expect(exceptionSameInstanceMatcher(thrownByConstructor));
    constructorDescriptor.createInstance(applicationContext);
  }

  @Test
  public void
      throwsWebApplicationExceptionWithStatus_NOT_FOUND_WhenParameterAnnotatedWithPathParamAnnotationCanNotBeResolved()
          throws Exception {
    when(pathParameterResolver.resolve(isA(Parameter.class), eq(applicationContext)))
        .thenThrow(new Exception());

    Constructor<Resource> constructor =
        Resource.class.getConstructor(
            String.class, String.class, String.class, String.class, String.class);
    ConstructorDescriptorImpl constructorDescriptor =
        new ConstructorDescriptorImpl(constructor, parameterResolverFactory);

    thrown.expect(webApplicationExceptionWithStatusMatcher(NOT_FOUND));
    constructorDescriptor.createInstance(applicationContext);
  }

  @Test
  public void
      throwsWebApplicationExceptionWithStatus_NOT_FOUND_WhenParameterAnnotatedWithQueryParamAnnotationCanNotBeResolved()
          throws Exception {
    when(queryParameterResolver.resolve(isA(Parameter.class), eq(applicationContext)))
        .thenThrow(new Exception());

    Constructor<Resource> constructor =
        Resource.class.getConstructor(
            String.class, String.class, String.class, String.class, String.class);
    ConstructorDescriptorImpl constructorDescriptor =
        new ConstructorDescriptorImpl(constructor, parameterResolverFactory);

    thrown.expect(webApplicationExceptionWithStatusMatcher(NOT_FOUND));
    constructorDescriptor.createInstance(applicationContext);
  }

  @Test
  public void
      throwsWebApplicationExceptionWithStatus_NOT_FOUND_WhenParameterAnnotatedWithMatrixParamAnnotationCanNotBeResolved()
          throws Exception {
    when(matrixParameterResolver.resolve(isA(Parameter.class), eq(applicationContext)))
        .thenThrow(new Exception());

    Constructor<Resource> constructor =
        Resource.class.getConstructor(
            String.class, String.class, String.class, String.class, String.class);
    ConstructorDescriptorImpl constructorDescriptor =
        new ConstructorDescriptorImpl(constructor, parameterResolverFactory);

    thrown.expect(webApplicationExceptionWithStatusMatcher(NOT_FOUND));
    constructorDescriptor.createInstance(applicationContext);
  }

  @Test
  public void
      throwsWebApplicationExceptionWithStatus_BAD_REQUEST_WhenParameterAnnotatedWithHeaderParamAnnotationCanNotBeResolved()
          throws Exception {
    when(headerParameterResolver.resolve(isA(Parameter.class), eq(applicationContext)))
        .thenThrow(new Exception());

    Constructor<Resource> constructor =
        Resource.class.getConstructor(
            String.class, String.class, String.class, String.class, String.class);
    ConstructorDescriptorImpl constructorDescriptor =
        new ConstructorDescriptorImpl(constructor, parameterResolverFactory);

    thrown.expect(webApplicationExceptionWithStatusMatcher(BAD_REQUEST));
    constructorDescriptor.createInstance(applicationContext);
  }

  @Test
  public void
      throwsWebApplicationExceptionWithStatus_BAD_REQUEST_WhenParameterAnnotatedWithCookieParamAnnotationCanNotBeResolved()
          throws Exception {
    when(cookieParameterResolver.resolve(isA(Parameter.class), eq(applicationContext)))
        .thenThrow(new Exception());

    Constructor<Resource> constructor =
        Resource.class.getConstructor(
            String.class, String.class, String.class, String.class, String.class);
    ConstructorDescriptorImpl constructorDescriptor =
        new ConstructorDescriptorImpl(constructor, parameterResolverFactory);

    thrown.expect(webApplicationExceptionWithStatusMatcher(BAD_REQUEST));
    constructorDescriptor.createInstance(applicationContext);
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

  private BaseMatcher<Throwable> webApplicationExceptionWithStatusMatcher(Response.Status status) {
    return new BaseMatcher<Throwable>() {
      @Override
      public boolean matches(Object item) {
        return item instanceof WebApplicationException
            && status.equals(((WebApplicationException) item).getResponse().getStatusInfo());
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(
            String.format(
                "WebApplicationException with status %d \"%s\"",
                status.getStatusCode(), status.getReasonPhrase()));
      }
    };
  }

  @Path("/a/{x}")
  public static class Resource {
    private static Exception thrownByDefaultConstructorIfSet;

    private String pathParam;
    private String queryParam;
    private String matrixParam;
    private String cookieParam;
    private String headerParam;

    private Dependency dependency;

    public Resource() throws Exception {
      if (thrownByDefaultConstructorIfSet != null) {
        throw thrownByDefaultConstructorIfSet;
      }
    }

    public Resource(
        @PathParam("x") String pathParam,
        @Encoded @QueryParam("q") String queryParam,
        @MatrixParam("m") String matrixParam,
        @CookieParam("c") String cookieParam,
        @DefaultValue("default") @HeaderParam("h") String headerParam) {
      this.pathParam = pathParam;
      this.queryParam = queryParam;
      this.matrixParam = matrixParam;
      this.cookieParam = cookieParam;
      this.headerParam = headerParam;
    }

    public Resource(Dependency dependency) {
      this.dependency = dependency;
    }
  }

  public static class Dependency {}
}
