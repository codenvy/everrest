/*
 * Copyright (c) 2012-2022 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl.resource;

import static com.google.common.collect.Lists.newArrayList;
import static jakarta.ws.rs.core.MediaType.WILDCARD_TYPE;
import static java.util.stream.Collectors.toList;
import static org.everrest.core.util.ParameterizedTypeImpl.newParameterizedType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;
import org.everrest.core.BaseObjectModel;
import org.everrest.core.ConstructorDescriptor;
import org.everrest.core.FieldInjector;
import org.everrest.core.Parameter;
import org.everrest.core.resource.ResourceDescriptor;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.resource.SubResourceLocatorDescriptor;
import org.everrest.core.resource.SubResourceMethodDescriptor;
import org.everrest.core.uri.UriPattern;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

/** @author andrew00x */
public class AbstractResourceDescriptorTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private Appender<ILoggingEvent> mockLogbackAppender;

  @Before
  public void setUp() throws Exception {
    setUpLogbackAppender();
  }

  private void setUpLogbackAppender() {
    ch.qos.logback.classic.Logger resourceDescriptorLogger =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(AbstractResourceDescriptor.class);
    ch.qos.logback.classic.Logger baseObjectModelLogger =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(BaseObjectModel.class);
    mockLogbackAppender = mockLogbackAppender();
    resourceDescriptorLogger.addAppender(mockLogbackAppender);
    baseObjectModelLogger.addAppender(mockLogbackAppender);
  }

  private Appender mockLogbackAppender() {
    Appender mockAppender = mock(Appender.class);
    when(mockAppender.getName()).thenReturn("MockAppender");
    return mockAppender;
  }

  @After
  public void tearDown() {
    ch.qos.logback.classic.Logger resourceDescriptorLogger =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(AbstractResourceDescriptor.class);
    ch.qos.logback.classic.Logger baseObjectModelLogger =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(BaseObjectModel.class);
    resourceDescriptorLogger.detachAppender(mockLogbackAppender);
    baseObjectModelLogger.detachAppender(mockLogbackAppender);
  }

  @Test
  public void failsWhenResourceDoesNotHavePublicConstructor() {
    thrown.expect(RuntimeException.class);
    new AbstractResourceDescriptor(NoPublicConstructorResource.class);
  }

  @Path("a")
  public static class NoPublicConstructorResource {
    NoPublicConstructorResource() {}

    @GET
    public void m1() {}
  }

  @Test
  public void warnWhenResourceDoesNotHaveAnyJaxRsMethods() {
    AbstractResourceDescriptor resourceDescriptor =
        new AbstractResourceDescriptor(NoJaxRsMethodResourceResource.class);
    assertEquals(1, resourceDescriptor.getResourceMethods().size());
    assertNotNull(resourceDescriptor.getResourceMethods().get("OPTIONS"));
    assertTrue(resourceDescriptor.getSubResourceMethods().isEmpty());
    assertTrue(resourceDescriptor.getSubResourceLocators().isEmpty());
    assertTrue(
        retrieveLoggingEvents().stream()
            .anyMatch(
                loggingEvent ->
                    loggingEvent.getLevel() == Level.WARN
                        && loggingEvent
                            .getMessage()
                            .equals(
                                "Not found any resource methods, sub-resource methods or sub-resource locators in {}")));
  }

  @Path("a")
  public static class NoJaxRsMethodResourceResource {
    public void m1() {}
  }

  private List<ILoggingEvent> retrieveLoggingEvents() {
    ArgumentCaptor<ILoggingEvent> logEventCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
    verify(mockLogbackAppender, atLeastOnce()).doAppend(logEventCaptor.capture());
    return logEventCaptor.getAllValues();
  }

  @Test
  public void failsWhenResourceClassHasTwoMethodsWithSameHttpMethodAnnotations() {
    thrown.expect(RuntimeException.class);
    new AbstractResourceDescriptor(ResourceWithTwoMethodsWithSameHttpMethodAnnotations.class);
  }

  @Path("a")
  public static class ResourceWithTwoMethodsWithSameHttpMethodAnnotations {
    @GET
    public void m1() {}

    @GET
    public void m2() {}
  }

  @Test
  public void
      failsWhenResourceClassHasTwoMethodsWithSameHttpMethodConsumesAndProducesAnnotations() {
    thrown.expect(RuntimeException.class);
    new AbstractResourceDescriptor(
        ResourceWithTwoMethodsWithSameHttpMethodConsumesAndProducesAnnotations.class);
  }

  @Path("a")
  public static class ResourceWithTwoMethodsWithSameHttpMethodConsumesAndProducesAnnotations {
    @GET
    @Consumes({"text/xml", "application/xml", "application/xml+xhtml"})
    @Produces("text/plain")
    public void m1() {}

    @GET
    @Consumes({"application/xml", "text/xml", "application/xml+xhtml"})
    @Produces("text/plain")
    public void m2() {}
  }

  @Test
  public void failsWhenResourceClassHasTwoMethodsWithSameHttpMethodAndPathAnnotations() {
    thrown.expect(RuntimeException.class);
    new AbstractResourceDescriptor(
        ResourceWithTwoMethodsWithSameHttpMethodAndPathAnnotations.class);
  }

  @Path("a")
  public static class ResourceWithTwoMethodsWithSameHttpMethodAndPathAnnotations {
    @GET
    @Path("b")
    public void m1() {}

    @GET
    @Path("/b")
    public void m2() {}
  }

  @Test
  public void
      failsWhenResourceClassHasTwoMethodsWithSameHttpMethodConsumesProducesAndPathAnnotations() {
    thrown.expect(RuntimeException.class);
    new AbstractResourceDescriptor(
        ResourceWithTwoMethodsWithSameHttpMethodConsumesProducesAndPathAnnotations.class);
  }

  @Path("a")
  public static class ResourceWithTwoMethodsWithSameHttpMethodConsumesProducesAndPathAnnotations {
    @GET
    @Consumes({"text/xml", "application/xml", "application/xml+xhtml"})
    @Produces("text/plain")
    @Path("b")
    public void m1() {}

    @GET
    @Consumes({"application/xml", "text/xml", "application/xml+xhtml"})
    @Produces("text/plain")
    @Path("b")
    public void m2() {}
  }

  @Test
  public void failsWhenResourceClassHasTwoMethodsWithSamePathAnnotations() {
    thrown.expect(RuntimeException.class);
    new AbstractResourceDescriptor(ResourceWithTwoMethodsWithSamePathAnnotations.class);
  }

  @Path("/a")
  public static class ResourceWithTwoMethodsWithSamePathAnnotations {
    @Path("b")
    public Object m1() {
      return new Object();
    }

    @Path("/b")
    public Object m2() {
      return new Object();
    }
  }

  @Test
  public void failsWhenResourceHasMethodWithTwoJaxRsAnnotationsOnSameParameter() {
    thrown.expect(RuntimeException.class);
    new AbstractResourceDescriptor(ResourceWithMethodWithTwoJaxRsAnnotationsOnSameParameter.class);
  }

  @Path("/a")
  public static class ResourceWithMethodWithTwoJaxRsAnnotationsOnSameParameter {
    @GET
    @Path("b")
    public void m1(@PathParam("b") @HeaderParam("h1") String b) {}
  }

  @Test
  public void failsWhenResourceHasConstructorWithTwoJaxRsAnnotationsOnSameParameter() {
    thrown.expect(RuntimeException.class);
    new AbstractResourceDescriptor(
        ResourceWithConstructorWithTwoJaxRsAnnotationsOnSameParameter.class);
  }

  @Path("/a")
  public static class ResourceWithConstructorWithTwoJaxRsAnnotationsOnSameParameter {
    public ResourceWithConstructorWithTwoJaxRsAnnotationsOnSameParameter(
        @PathParam("b") @HeaderParam("h1") String b) {}

    @GET
    public void m1() {}
  }

  @Test
  public void failsWhenResourceHasFieldWithTwoJaxRsAnnotations() {
    thrown.expect(RuntimeException.class);
    new AbstractResourceDescriptor(ResourceWithFieldThatHasTwoJaxRsAnnotations.class);
  }

  @Path("/a")
  public static class ResourceWithFieldThatHasTwoJaxRsAnnotations {
    @PathParam("b")
    @QueryParam("query")
    String s;

    @GET
    public void m1() {}
  }

  @Test
  public void failsWhenResourceMethodHasMoreThanOneEntityParameter() {
    thrown.expect(RuntimeException.class);
    new AbstractResourceDescriptor(ResourceWithMethodThatHasMoreThanOneEntityParameter.class);
  }

  @Path("a")
  public static class ResourceWithMethodThatHasMoreThanOneEntityParameter {
    @POST
    public void m1(String entityOne, String entityTwo) {}
  }

  @Test
  public void
      failsWhenResourceMethodHasAtLeastOneFormParamAndEntityParameterOtherThanMultivaluedMap() {
    thrown.expect(RuntimeException.class);
    new AbstractResourceDescriptor(
        ResourceWithMethodThatHasAtLeastOneFormParamAndEntityParameterOtherThanMultivaluedMap
            .class);
  }

  @Path("a")
  public static
  class ResourceWithMethodThatHasAtLeastOneFormParamAndEntityParameterOtherThanMultivaluedMap {
    @POST
    public void m1(@FormParam("x") String formParam, String entity) {}
  }

  @Test
  public void failsWhenResourceLocatorHasEntityParameter() {
    thrown.expect(RuntimeException.class);
    new AbstractResourceDescriptor(ResourceWithResourceLocatorThatHasEntityParameter.class);
  }

  @Path("a")
  public static class ResourceWithResourceLocatorThatHasEntityParameter {
    @Path("b")
    public Object m1(String entity) {
      return null;
    }
  }

  @Test
  public void warnWhenResourceHasTwoConstructorsWithTheSameNumberOfParameters() {
    new AbstractResourceDescriptor(ResourceWithTwoConstructorsWithTheSameNumberOfParameters.class);
    assertTrue(
        retrieveLoggingEvents().stream()
            .anyMatch(
                loggingEvent ->
                    loggingEvent.getLevel() == Level.WARN
                        && loggingEvent
                            .getMessage()
                            .equals(
                                "Two constructors with the same number of parameter found {} and {}")));
  }

  @Path("/a")
  public static class ResourceWithTwoConstructorsWithTheSameNumberOfParameters {
    public ResourceWithTwoConstructorsWithTheSameNumberOfParameters(
        @PathParam("b") String b, @QueryParam("c") int c) {}

    public ResourceWithTwoConstructorsWithTheSameNumberOfParameters(
        @HeaderParam("a") int a, @PathParam("d") String d) {}

    @GET
    public void m1() {}
  }

  @Test
  public void warnWhenResourceHasNonPublicJaxRsMethods() {
    new AbstractResourceDescriptor(ResourceWithNonPublicJaxRsMethod.class);
    assertTrue(
        retrieveLoggingEvents().stream()
            .anyMatch(
                loggingEvent ->
                    loggingEvent.getLevel() == Level.WARN
                        && loggingEvent
                            .getMessage()
                            .equals(
                                "Non-public method {} in {} annotated with @Path of HTTP method annotation, it's ignored")));
  }

  @Path("/")
  public static class ResourceWithNonPublicJaxRsMethod {
    @GET
    void nonPublicGET_method() {}
  }

  @Test
  public void processesRootResource() {
    ResourceDescriptor rootResource = new AbstractResourceDescriptor(SampleRootResource.class);
    assertEquals(SampleRootResource.class, rootResource.getObjectClass());
    assertTrue(rootResource.isRootResource());
    assertEquals("/a/{b}/", rootResource.getPathValue().getPath());
  }

  @Test
  public void processesNonRootResource() {
    ResourceDescriptor nonRootResource =
        new AbstractResourceDescriptor(SampleNonRootResource.class);
    assertEquals(SampleNonRootResource.class, nonRootResource.getObjectClass());
    assertFalse(nonRootResource.isRootResource());
    assertNull(nonRootResource.getPathValue());
  }

  @Test
  public void processesFields() {
    ResourceDescriptor resource = new AbstractResourceDescriptor(SampleRootResource.class);
    List<FieldInjector> fieldInjectors =
        filterFieldsInsertedByJacocoFrameworkDuringInstrumentation(resource.getFieldInjectors());
    assertEquals(1, fieldInjectors.size());
    FieldInjector fieldInjector = fieldInjectors.get(0);
    assertEquals(String.class, fieldInjector.getParameterClass());
    assertEquals(String.class, fieldInjector.getGenericType());
    assertEquals("default", fieldInjector.getDefaultValue());
    assertEquals(PathParam.class, fieldInjector.getAnnotation().annotationType());
    assertEquals("b", ((PathParam) fieldInjector.getAnnotation()).value());
    assertTrue(fieldInjector.isEncoded());
  }

  @Test
  public void processesFieldsFromSuperClasses() {
    ResourceDescriptor resource = new AbstractResourceDescriptor(EndResource.class);
    List<FieldInjector> fieldInjectors =
        filterFieldsInsertedByJacocoFrameworkDuringInstrumentation(resource.getFieldInjectors());
    List<String> fieldNames = fieldInjectors.stream().map(FieldInjector::getName).collect(toList());
    assertEquals(4, fieldNames.size());
    assertTrue(
        fieldNames.containsAll(newArrayList("uriInfo", "request", "securityContext", "headers")));
  }

  public abstract static class AbstractResource {
    @Context protected UriInfo uriInfo;
    @Context protected Request request;
  }

  public abstract static class ExtResource extends AbstractResource {
    @Context protected SecurityContext securityContext;
  }

  public static class EndResource extends ExtResource {
    @Context private HttpHeaders headers;

    @GET
    public void m1() {}
  }

  @Test
  public void processesConstructors() {
    ResourceDescriptor resource = new AbstractResourceDescriptor(SampleRootResource.class);
    assertEquals(3, resource.getConstructorDescriptors().size());
    List<ConstructorDescriptor> constructors = resource.getConstructorDescriptors();
    assertEquals(2, constructors.get(0).getParameters().size());
    assertEquals(1, constructors.get(1).getParameters().size());
    assertEquals(0, constructors.get(2).getParameters().size());

    ConstructorDescriptor constructorWithTheMostParameters = constructors.get(0);
    List<Parameter> constructorParameters = constructorWithTheMostParameters.getParameters();
    assertEquals(int.class, constructorParameters.get(0).getParameterClass());
    assertEquals(String.class, constructorParameters.get(1).getParameterClass());
    assertEquals(QueryParam.class, constructorParameters.get(0).getAnnotation().annotationType());
    assertEquals(PathParam.class, constructorParameters.get(1).getAnnotation().annotationType());
    assertEquals("test", ((QueryParam) constructorParameters.get(0).getAnnotation()).value());
    assertEquals("b", ((PathParam) constructorParameters.get(1).getAnnotation()).value());
    assertFalse(constructorParameters.get(0).isEncoded());
    assertTrue(constructorParameters.get(1).isEncoded());
  }

  @Test
  public void processesAllMethods() {
    ResourceDescriptor resource = new AbstractResourceDescriptor(SampleRootResource.class);
    assertEquals(3, resource.getResourceMethods().size());
    assertEquals(1, resource.getSubResourceMethods().size());
    assertEquals(1, resource.getSubResourceLocators().size());
  }

  @Test
  public void adds_OPTIONS_HttpMethodThatGenerated_WADL_Response() {
    ResourceDescriptor resource = new AbstractResourceDescriptor(SampleRootResource.class);

    assertNotNull(resource.getResourceMethods().get("OPTIONS"));
    assertEquals(1, resource.getResourceMethods().get("OPTIONS").size());

    ResourceMethodDescriptor methodDescriptor = resource.getResourceMethods().get("OPTIONS").get(0);
    assertNull(methodDescriptor.getMethod());
    assertEquals("OPTIONS", methodDescriptor.getHttpMethod());
    assertEquals(newArrayList(WILDCARD_TYPE), methodDescriptor.consumes());
    assertEquals(
        newArrayList(new MediaType("application", "vnd.sun.wadl+xml")),
        methodDescriptor.produces());
    assertEquals(SampleRootResource.class, methodDescriptor.getParentResource().getObjectClass());
    assertTrue(methodDescriptor.getMethodParameters().isEmpty());
  }

  @Test
  public void processesResourceMethods() {
    ResourceDescriptor resource = new AbstractResourceDescriptor(SampleRootResource.class);

    assertNotNull(resource.getResourceMethods().get("GET"));
    assertEquals(1, resource.getResourceMethods().get("GET").size());

    ResourceMethodDescriptor methodDescriptor = resource.getResourceMethods().get("GET").get(0);
    assertEquals("resourceMethod", methodDescriptor.getMethod().getName());
    assertEquals("GET", methodDescriptor.getHttpMethod());
    assertEquals(newArrayList(WILDCARD_TYPE), methodDescriptor.consumes());
    assertEquals(newArrayList(new MediaType("application", "xml")), methodDescriptor.produces());
    assertEquals(SampleRootResource.class, methodDescriptor.getParentResource().getObjectClass());
    assertEquals(1, methodDescriptor.getMethodParameters().size());

    Parameter methodParameter = methodDescriptor.getMethodParameters().get(0);
    assertEquals(String.class, methodParameter.getParameterClass());
    assertEquals(PathParam.class, methodParameter.getAnnotations()[0].annotationType());
    assertEquals(DefaultValue.class, methodParameter.getAnnotations()[1].annotationType());
    assertEquals("hello", methodParameter.getDefaultValue());
    assertEquals(PathParam.class, methodParameter.getAnnotation().annotationType());
    assertEquals("b", ((PathParam) methodParameter.getAnnotation()).value());
  }

  @Test
  public void adds_HEAD_HttpMethodFor_GET_ResourceMethod() {
    ResourceDescriptor resource = new AbstractResourceDescriptor(SampleRootResource.class);

    assertNotNull(resource.getResourceMethods().get("HEAD"));
    assertEquals(1, resource.getResourceMethods().get("HEAD").size());

    ResourceMethodDescriptor methodDescriptor = resource.getResourceMethods().get("HEAD").get(0);
    assertEquals("resourceMethod", methodDescriptor.getMethod().getName());
    assertEquals("HEAD", methodDescriptor.getHttpMethod());
    assertEquals(newArrayList(WILDCARD_TYPE), methodDescriptor.consumes());
    assertEquals(newArrayList(new MediaType("application", "xml")), methodDescriptor.produces());
    assertEquals(SampleRootResource.class, methodDescriptor.getParentResource().getObjectClass());
    assertEquals(1, methodDescriptor.getMethodParameters().size());

    Parameter methodParameter = methodDescriptor.getMethodParameters().get(0);
    assertEquals(String.class, methodParameter.getParameterClass());
    assertEquals(PathParam.class, methodParameter.getAnnotations()[0].annotationType());
    assertEquals(DefaultValue.class, methodParameter.getAnnotations()[1].annotationType());
    assertEquals("hello", methodParameter.getDefaultValue());
    assertEquals(PathParam.class, methodParameter.getAnnotation().annotationType());
    assertEquals("b", ((PathParam) methodParameter.getAnnotation()).value());
  }

  @Test
  public void processesSubResourceMethods() {
    ResourceDescriptor resource = new AbstractResourceDescriptor(SampleRootResource.class);
    Map<String, List<SubResourceMethodDescriptor>> methods =
        resource.getSubResourceMethods().get(new UriPattern("{any}"));

    assertNotNull(methods.get("POST"));
    assertEquals(1, methods.get("POST").size());

    SubResourceMethodDescriptor subResourceMethod = methods.get("POST").get(0);
    assertEquals("subResourceMethod", subResourceMethod.getMethod().getName());
    assertEquals("POST", subResourceMethod.getHttpMethod());
    assertEquals("{c}", subResourceMethod.getPathValue().getPath());
    assertEquals(
        newArrayList(new MediaType("text", "plain"), new MediaType("text", "xml")),
        subResourceMethod.consumes());
    assertEquals(newArrayList(new MediaType("text", "html")), subResourceMethod.produces());
    assertEquals(SampleRootResource.class, subResourceMethod.getParentResource().getObjectClass());
    assertEquals(1, subResourceMethod.getMethodParameters().size());

    Parameter methodParameter = subResourceMethod.getMethodParameters().get(0);
    assertEquals(String.class, methodParameter.getParameterClass());
    assertEquals(1, methodParameter.getAnnotations().length);
    assertEquals(PathParam.class, methodParameter.getAnnotations()[0].annotationType());
    assertEquals(null, methodParameter.getDefaultValue());
    assertEquals(PathParam.class, methodParameter.getAnnotation().annotationType());

    assertNotNull(methods.get("GET"));
    assertEquals(1, methods.get("GET").size());

    subResourceMethod = methods.get("GET").get(0);
    assertEquals("subResourceMethod", subResourceMethod.getMethod().getName());
    assertEquals("GET", subResourceMethod.getHttpMethod());
    assertEquals("{d}", subResourceMethod.getPathValue().getPath());
    assertEquals(newArrayList(WILDCARD_TYPE), subResourceMethod.consumes());
    assertEquals(newArrayList(WILDCARD_TYPE), subResourceMethod.produces());
    assertEquals(SampleRootResource.class, subResourceMethod.getParentResource().getObjectClass());
    assertEquals(1, subResourceMethod.getMethodParameters().size());

    methodParameter = subResourceMethod.getMethodParameters().get(0);
    assertEquals(List.class, methodParameter.getParameterClass());
    assertEquals(newParameterizedType(List.class, String.class), methodParameter.getGenericType());
    assertEquals(1, methodParameter.getAnnotations().length);
    assertEquals(PathParam.class, methodParameter.getAnnotations()[0].annotationType());
    assertEquals(null, methodParameter.getDefaultValue());
    assertEquals(PathParam.class, methodParameter.getAnnotation().annotationType());
  }

  @Test
  public void adds_HEAD_HttpMethodFor_GET_SubResourceMethod() {
    ResourceDescriptor resource = new AbstractResourceDescriptor(SampleRootResource.class);
    Map<String, List<SubResourceMethodDescriptor>> methods =
        resource.getSubResourceMethods().get(new UriPattern("{any}"));

    assertNotNull(methods.get("HEAD"));
    assertEquals(1, methods.get("HEAD").size());

    SubResourceMethodDescriptor subResourceMethod = methods.get("HEAD").get(0);
    assertEquals("subResourceMethod", subResourceMethod.getMethod().getName());
    assertEquals("HEAD", subResourceMethod.getHttpMethod());
    assertEquals("{d}", subResourceMethod.getPathValue().getPath());
    assertEquals(newArrayList(WILDCARD_TYPE), subResourceMethod.consumes());
    assertEquals(newArrayList(WILDCARD_TYPE), subResourceMethod.produces());
    assertEquals(SampleRootResource.class, subResourceMethod.getParentResource().getObjectClass());
    assertEquals(1, subResourceMethod.getMethodParameters().size());

    Parameter methodParameter = subResourceMethod.getMethodParameters().get(0);
    assertEquals(List.class, methodParameter.getParameterClass());
    assertEquals(newParameterizedType(List.class, String.class), methodParameter.getGenericType());
    assertEquals(1, methodParameter.getAnnotations().length);
    assertEquals(PathParam.class, methodParameter.getAnnotations()[0].annotationType());
    assertEquals(null, methodParameter.getDefaultValue());
    assertEquals(PathParam.class, methodParameter.getAnnotation().annotationType());
  }

  @Test
  public void processesSubResourceLocators() {
    ResourceDescriptor resource = new AbstractResourceDescriptor(SampleRootResource.class);

    SubResourceLocatorDescriptor subResourceLocator =
        resource.getSubResourceLocators().get(new UriPattern("{any}/d"));
    assertEquals("subResourceLocator", subResourceLocator.getMethod().getName());
    assertEquals("{c}/d", subResourceLocator.getPathValue().getPath());
    assertEquals(SampleRootResource.class, subResourceLocator.getParentResource().getObjectClass());
    assertEquals(1, subResourceLocator.getMethodParameters().size());

    Parameter methodParameter = subResourceLocator.getMethodParameters().get(0);
    assertEquals(String.class, methodParameter.getParameterClass());
    assertEquals(2, methodParameter.getAnnotations().length);
    assertEquals(PathParam.class, methodParameter.getAnnotations()[0].annotationType());
    assertEquals(Encoded.class, methodParameter.getAnnotations()[1].annotationType());
    assertTrue(methodParameter.isEncoded());
    assertEquals(null, methodParameter.getDefaultValue());
    assertEquals(PathParam.class, methodParameter.getAnnotation().annotationType());
  }

  @SuppressWarnings("unused")
  @Path("/a/{b}/")
  public static class SampleRootResource {

    @DefaultValue("default")
    @PathParam("b")
    @Encoded
    private String field;

    public SampleRootResource(@PathParam("b") String str) {}

    public SampleRootResource() {}

    public SampleRootResource(@QueryParam("test") int i, @Encoded @PathParam("b") String str) {}

    @POST
    @Path("{c}")
    @Consumes({"text/plain", "text/xml"})
    @Produces({"text/html"})
    public void subResourceMethod(@PathParam("b") String str) {}

    @GET
    @Path("{d}")
    public void subResourceMethod(@PathParam("b") List<String> list) {}

    @Path("{c}/d")
    public void subResourceLocator(@PathParam("b") @Encoded String str) {}

    @GET
    @Produces({"application/xml"})
    public void resourceMethod(@PathParam("b") @DefaultValue("hello") String str) {}
  }

  public static class SampleNonRootResource {
    @GET
    public void resourceMethod() {}
  }

  @Test
  public void sortsResourceMethodsByConsumesAndProducesTypes() {
    ResourceDescriptor resource =
        new AbstractResourceDescriptor(ResourceForTestSortingOfResourceMethods.class);
    List<String> methodNames =
        resource.getResourceMethods().get("GET").stream()
            .map(resourceMethod -> resourceMethod.getMethod().getName())
            .collect(toList());
    assertEquals(newArrayList("first", "second", "third", "fourth", "fifth", "last"), methodNames);
  }

  @Path("a")
  public static class ResourceForTestSortingOfResourceMethods {
    @Consumes({"application/*", "application/xml", "text/*"})
    @Produces({"text/plain", "text/html", "text/*"})
    @GET
    public void fifth() {}

    @GET
    public void last() {}

    @Consumes({"application/*", "text/*"})
    @Produces({"text/plain", "text/html"})
    @GET
    public void third() {}

    @Consumes({"application/xml", "text/plain"})
    @GET
    public void second() {}

    @Consumes({"application/xml"})
    @GET
    public void first() {}

    @Consumes({"text/*"})
    @Produces({"text/html", "text/*"})
    @GET
    public void fourth() {}
  }

  @Test
  public void sortsSubResourceMethodsByUriPatterAndConsumesAndProducesTypes() {
    ResourceDescriptor resource =
        new AbstractResourceDescriptor(ResourceForTestSortingOfSubResourceMethods.class);
    Map<UriPattern, Map<String, List<SubResourceMethodDescriptor>>> subResourceMethods =
        resource.getSubResourceMethods();

    assertEquals(
        newArrayList("/b/c/d", "/b/c", "/b/{c}", "/b"),
        subResourceMethods.keySet().stream().map(UriPattern::getTemplate).collect(toList()));

    assertEquals(1, subResourceMethods.get(new UriPattern("/b/c/d")).get("GET").size());
    assertEquals(1, subResourceMethods.get(new UriPattern("/b/c")).get("GET").size());
    assertEquals(1, subResourceMethods.get(new UriPattern("/b/{c}")).get("GET").size());
    assertEquals(3, subResourceMethods.get(new UriPattern("/b")).get("GET").size());

    List<String> methodNames =
        subResourceMethods.get(new UriPattern("/b")).get("GET").stream()
            .map(subResourceMethod -> subResourceMethod.getMethod().getName())
            .collect(toList());
    assertEquals(newArrayList("m2", "m5", "m0"), methodNames);
  }

  @Path("a")
  public static class ResourceForTestSortingOfSubResourceMethods {
    @Consumes({"application/*", "application/xml", "text/*"})
    @Produces({"text/plain", "text/html", "text/*"})
    @GET
    @Path("b")
    public void m0() {}

    @GET
    @Path("b/c")
    public void m1() {}

    @Consumes({"application/*", "text/*"})
    @Produces({"text/plain", "text/html"})
    @GET
    @Path("b")
    public void m2() {}

    @Consumes({"application/xml", "text/plain"})
    @GET
    @Path("b/{c}")
    public void m3() {}

    @Consumes({"application/xml"})
    @GET
    @Path("b/c/d")
    public void m4() {}

    @Consumes({"text/*"})
    @Produces({"text/html", "text/*"})
    @GET
    @Path("b")
    public void m5() {}
  }

  @Test
  public void sortsSubResourceLocatorsByUriPatter() {
    ResourceDescriptor resource =
        new AbstractResourceDescriptor(ResourceForTestSortingOfSubResourceLocators.class);
    Map<UriPattern, SubResourceLocatorDescriptor> resourceLocators =
        resource.getSubResourceLocators();

    assertEquals(
        newArrayList("/b/c/d", "/b/c/z", "/b/c", "/b/{c}", "/b"),
        resourceLocators.keySet().stream().map(UriPattern::getTemplate).collect(toList()));
  }

  @Path("a")
  public static class ResourceForTestSortingOfSubResourceLocators {
    @Path("b")
    public void m0() {}

    @Path("b/c/z")
    public void m1() {}

    @Path("b/{c}")
    public void m2() {}

    @Path("b/c/d")
    public void m3() {}

    @Path("b/c")
    public void m4() {}
  }

  @Test
  public void inheritsJaxRsAnnotationFromImplementedInterfaces() {
    ResourceDescriptor resource = new AbstractResourceDescriptor(ResourceImpl.class);
    Map<String, List<SubResourceMethodDescriptor>> methods =
        resource.getSubResourceMethods().get(new UriPattern("b"));

    assertNotNull(methods.get("GET"));
    assertEquals(1, methods.get("GET").size());

    SubResourceMethodDescriptor subResourceMethod = methods.get("GET").get(0);
    assertEquals("m1", subResourceMethod.getMethod().getName());
    assertEquals("GET", subResourceMethod.getHttpMethod());
    assertEquals("b", subResourceMethod.getPathValue().getPath());
    assertEquals(newArrayList(new MediaType("text", "plain")), subResourceMethod.consumes());
    assertEquals(newArrayList(WILDCARD_TYPE), subResourceMethod.produces());
    assertEquals(ResourceImpl.class, subResourceMethod.getParentResource().getObjectClass());
    assertEquals(1, subResourceMethod.getMethodParameters().size());

    Parameter methodParameter = subResourceMethod.getMethodParameters().get(0);
    assertEquals(String.class, methodParameter.getParameterClass());
    assertEquals(0, methodParameter.getAnnotations().length);
    assertEquals(null, methodParameter.getDefaultValue());
  }

  public interface Resource {
    @Consumes({"text/plain"})
    @GET
    void m1(String entity);
  }

  @Path("a")
  public static class ResourceImpl implements Resource {
    @Path("b")
    @Override
    public void m1(String entity) {}
  }

  @Test
  public void processesSecurityAnnotationFromMethod() {
    ResourceDescriptor resource =
        new AbstractResourceDescriptor(ResourceWithMethodWithSecurityAnnotation.class);
    List<ResourceMethodDescriptor> resourceMethods = resource.getResourceMethods().get("GET");

    assertEquals(1, resourceMethods.get(0).getAnnotations().length);
    RolesAllowed rolesAllowed = (RolesAllowed) resourceMethods.get(0).getAnnotations()[0];
    assertNotNull(rolesAllowed);
  }

  @Path("a")
  public static class ResourceWithMethodWithSecurityAnnotation {
    @RolesAllowed("user")
    @GET
    public void m1() {}
  }

  @Test
  public void processesSecurityAnnotationFromClass() {
    ResourceDescriptor resource =
        new AbstractResourceDescriptor(ResourceWithSecurityAnnotation.class);
    List<ResourceMethodDescriptor> resourceMethods = resource.getResourceMethods().get("GET");

    assertEquals(1, resourceMethods.get(0).getAnnotations().length);
    RolesAllowed rolesAllowed = (RolesAllowed) resourceMethods.get(0).getAnnotations()[0];
    assertNotNull(rolesAllowed);
  }

  @RolesAllowed("user")
  @Path("a")
  public static class ResourceWithSecurityAnnotation {
    @GET
    public void m1() {}
  }

  @Test
  public void ignoresSecurityAnnotationFromClassWhenMethodHasOwn() {
    ResourceDescriptor resource =
        new AbstractResourceDescriptor(ResourceWithSecurityAnnotationOnClassAndMethod.class);
    List<ResourceMethodDescriptor> resourceMethods = resource.getResourceMethods().get("GET");

    assertEquals(1, resourceMethods.get(0).getAnnotations().length);
    PermitAll permitAll = (PermitAll) resourceMethods.get(0).getAnnotations()[0];
    assertNotNull(permitAll);
  }

  @RolesAllowed("user")
  @Path("a")
  public static class ResourceWithSecurityAnnotationOnClassAndMethod {
    @PermitAll
    @GET
    public void m1() {}
  }

  @Test
  public void inheritsSecurityAnnotationFromMethodOnParentInterface() {
    ResourceDescriptor resource =
        new AbstractResourceDescriptor(
            ResourceWithSecurityAnnotationOnMethodInParentInterface.class);
    List<ResourceMethodDescriptor> resourceMethods = resource.getResourceMethods().get("GET");

    assertEquals(1, resourceMethods.get(0).getAnnotations().length);
    RolesAllowed rolesAllowed = (RolesAllowed) resourceMethods.get(0).getAnnotations()[0];
    assertNotNull(rolesAllowed);
  }

  public interface InterfaceWithSecurityAnnotationOnMethod {
    @RolesAllowed("user")
    void m1();
  }

  @Path("a")
  public static class ResourceWithSecurityAnnotationOnMethodInParentInterface
      implements InterfaceWithSecurityAnnotationOnMethod {
    @GET
    public void m1() {}
  }

  @Test
  public void inheritsSecurityAnnotationFromParentInterface() {
    ResourceDescriptor resource =
        new AbstractResourceDescriptor(ResourceWithSecurityAnnotationOnParentInterface.class);
    List<ResourceMethodDescriptor> resourceMethods = resource.getResourceMethods().get("GET");

    assertEquals(1, resourceMethods.get(0).getAnnotations().length);
    RolesAllowed rolesAllowed = (RolesAllowed) resourceMethods.get(0).getAnnotations()[0];
    assertNotNull(rolesAllowed);
  }

  @RolesAllowed("user")
  public interface InterfaceWithSecurityAnnotation {
    void m1();
  }

  @Path("a")
  public static class ResourceWithSecurityAnnotationOnParentInterface
      implements InterfaceWithSecurityAnnotation {
    @GET
    public void m1() {}
  }

  @Test
  public void inheritsSecurityAnnotationFromMethodOnParentClass() {
    ResourceDescriptor resource =
        new AbstractResourceDescriptor(ResourceWithSecurityAnnotationOnMethodInParentClass.class);
    List<ResourceMethodDescriptor> resourceMethods = resource.getResourceMethods().get("GET");

    assertEquals(1, resourceMethods.get(0).getAnnotations().length);
    RolesAllowed rolesAllowed = (RolesAllowed) resourceMethods.get(0).getAnnotations()[0];
    assertNotNull(rolesAllowed);
  }

  public abstract static class ClassWithSecurityAnnotationOnMethod {
    @RolesAllowed("user")
    public abstract void m1();
  }

  @Path("a")
  public static class ResourceWithSecurityAnnotationOnMethodInParentClass
      extends ClassWithSecurityAnnotationOnMethod {
    @GET
    public void m1() {}
  }

  @Test
  public void inheritsSecurityAnnotationFromParentClass() {
    ResourceDescriptor resource =
        new AbstractResourceDescriptor(ResourceWithSecurityAnnotationOnParentClass.class);
    List<ResourceMethodDescriptor> resourceMethods = resource.getResourceMethods().get("GET");

    assertEquals(1, resourceMethods.get(0).getAnnotations().length);
    RolesAllowed rolesAllowed = (RolesAllowed) resourceMethods.get(0).getAnnotations()[0];
    assertNotNull(rolesAllowed);
  }

  @RolesAllowed("user")
  public abstract static class ClassWithSecurityAnnotation {
    public abstract void m1();
  }

  @Path("a")
  public static class ResourceWithSecurityAnnotationOnParentClass
      extends ClassWithSecurityAnnotation {
    @GET
    public void m1() {}
  }

  private List<FieldInjector> filterFieldsInsertedByJacocoFrameworkDuringInstrumentation(
      List<FieldInjector> initialList) {
    return initialList.stream()
        .filter(fieldInjector -> !fieldInjector.getName().startsWith("$jacocoData"))
        .collect(toList());
  }

  @Test
  public void processesResourceWithOverriddenMethods() throws Exception {
    ResourceDescriptor resource = new AbstractResourceDescriptor(Resource2.class);
    assertEquals(1, resource.getResourceMethods().get("GET").size());
    assertEquals(1, resource.getResourceMethods().get("POST").size());
  }

  @Path("/a")
  public static class Resource1 {
    @GET
    public void m1() {}

    @POST
    @Consumes("text/plain")
    public void m2(String s) {}
  }

  @Path("/a")
  public static class Resource2 extends Resource1 {
    @Override
    public void m1() {}
  }
}
