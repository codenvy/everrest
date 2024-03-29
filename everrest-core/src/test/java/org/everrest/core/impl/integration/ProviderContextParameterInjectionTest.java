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
package org.everrest.core.impl.integration;

import static com.google.common.collect.Sets.newHashSet;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import com.google.common.io.CharStreams;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.junit.Before;
import org.junit.Test;

public class ProviderContextParameterInjectionTest extends BaseTest {

  @Provider
  @Consumes("text/plain")
  public static class MessageBodyReaderChecker implements MessageBodyReader<String> {
    @Context private UriInfo uriInfo;
    @Context private Request request;
    @Context private HttpHeaders httpHeaders;
    @Context private Providers providers;
    @Context private HttpServletRequest httpRequest;

    @Override
    public boolean isReadable(
        Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
      return type == String.class;
    }

    @Override
    public String readFrom(
        Class<String> type,
        Type genericType,
        Annotation[] annotations,
        MediaType mediaType,
        MultivaluedMap<String, String> httpHeaders,
        InputStream entityStream)
        throws IOException, WebApplicationException {
      assertThatAllRequiredFieldsAreInjectedInMessageBodyReader(this);
      return "MessageBodyReaderChecker: "
          + CharStreams.toString(new InputStreamReader(entityStream));
    }
  }

  @Provider
  @Produces("text/plain")
  public static class MessageBodyWriterChecker implements MessageBodyWriter<String> {
    @Context private UriInfo uriInfo;
    @Context private Request request;
    @Context private HttpHeaders httpHeaders;
    @Context private Providers providers;
    @Context private HttpServletRequest httpRequest;

    @Override
    public boolean isWriteable(
        Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
      return type == String.class;
    }

    @Override
    public long getSize(
        String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
      return -1;
    }

    @Override
    public void writeTo(
        String string,
        Class<?> type,
        Type genericType,
        Annotation[] annotations,
        MediaType mediaType,
        MultivaluedMap<String, Object> httpHeaders,
        OutputStream entityStream)
        throws IOException, WebApplicationException {
      assertThatAllRequiredFieldsAreInjectedInMessageBodyWriter(this);

      Writer writer = new OutputStreamWriter(entityStream);
      writer.write("MessageBodyWriterChecker: ");
      writer.write(string);
      writer.flush();
    }
  }

  @Provider
  public static class ExceptionMapperChecker implements ExceptionMapper<RuntimeException> {
    @Context private UriInfo uriInfo;
    @Context private Request request;
    @Context private HttpHeaders httpHeaders;
    @Context private Providers providers;
    @Context private HttpServletRequest httpRequest;

    public Response toResponse(RuntimeException exception) {
      assertThatAllRequiredFieldsAreInjectedInExceptionMapper(this);
      return Response.ok()
          .entity("ExceptionMapperChecker: " + exception.getMessage())
          .type("text/plain")
          .build();
    }
  }

  @Provider
  @Produces("text/plain")
  public static class ContextResolverChecker implements ContextResolver<String> {
    @Context private UriInfo uriInfo;
    @Context private Request request;
    @Context private HttpHeaders httpHeaders;
    @Context private Providers providers;
    @Context private HttpServletRequest httpRequest;

    public String getContext(Class<?> type) {
      return null;
    }
  }

  @Path("a")
  public static class Resource1 {

    @Context private Providers providers;

    @GET
    @Path("1")
    public void m1(String string) {
      assertEquals("MessageBodyReaderChecker: all fields injected", string);
    }

    @GET
    @Path("2")
    public String m2() {
      return "all fields injected";
    }

    @GET
    @Path("3")
    public String m3() {
      ContextResolverChecker contextResolver =
          (ContextResolverChecker) providers.getContextResolver(String.class, TEXT_PLAIN_TYPE);
      assertThatAllRequiredFieldsAreInjectedInContextResolver(contextResolver);
      return "ContextResolverChecker: all fields injected";
    }

    @GET
    @Path("4")
    public void m4() {
      throw new RuntimeException("all fields injected");
    }
  }

  private EnvironmentContext env;

  @Before
  public void setUp() throws Exception {
    super.setUp();

    env = new EnvironmentContext();
    env.put(HttpServletRequest.class, mock(HttpServletRequest.class));

    processor.addApplication(
        new Application() {
          @Override
          public Set<Class<?>> getClasses() {
            return newHashSet(
                MessageBodyReaderChecker.class,
                MessageBodyWriterChecker.class,
                ExceptionMapperChecker.class,
                ContextResolverChecker.class,
                Resource1.class);
          }
        });
  }

  private static void assertThatAllRequiredFieldsAreInjectedInContextResolver(
      ContextResolverChecker contextResolver) {
    assertNotNull(contextResolver.uriInfo);
    assertNotNull(contextResolver.request);
    assertNotNull(contextResolver.httpHeaders);
    assertNotNull(contextResolver.providers);
    assertNotNull(contextResolver.httpRequest);
  }

  private static void assertThatAllRequiredFieldsAreInjectedInMessageBodyReader(
      MessageBodyReaderChecker messageBodyReader) {
    assertNotNull(messageBodyReader.uriInfo);
    assertNotNull(messageBodyReader.request);
    assertNotNull(messageBodyReader.httpHeaders);
    assertNotNull(messageBodyReader.providers);
    assertNotNull(messageBodyReader.httpRequest);
  }

  private static void assertThatAllRequiredFieldsAreInjectedInMessageBodyWriter(
      MessageBodyWriterChecker messageBodyWriter) {
    assertNotNull(messageBodyWriter.uriInfo);
    assertNotNull(messageBodyWriter.request);
    assertNotNull(messageBodyWriter.httpHeaders);
    assertNotNull(messageBodyWriter.providers);
    assertNotNull(messageBodyWriter.httpRequest);
  }

  private static void assertThatAllRequiredFieldsAreInjectedInExceptionMapper(
      ExceptionMapperChecker exceptionMapper) {
    assertNotNull(exceptionMapper.uriInfo);
    assertNotNull(exceptionMapper.request);
    assertNotNull(exceptionMapper.httpHeaders);
    assertNotNull(exceptionMapper.providers);
    assertNotNull(exceptionMapper.httpRequest);
  }

  @Test
  public void contextParameterInjectedInMessageBodyReader() throws Exception {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.putSingle(CONTENT_TYPE, "text/plain");

    ContainerResponse response =
        launcher.service("GET", "/a/1", "", headers, "all fields injected".getBytes(), env);

    assertEquals(204, response.getStatus());
  }

  @Test
  public void contextParameterInjectedInMessageBodyWriter() throws Exception {
    ByteArrayContainerResponseWriter responseWriter = new ByteArrayContainerResponseWriter();
    ContainerResponse response =
        launcher.service("GET", "/a/2", "", new MultivaluedHashMap<>(), null, responseWriter, env);

    assertEquals(200, response.getStatus());
    assertEquals(
        "MessageBodyWriterChecker: all fields injected", new String(responseWriter.getBody()));
  }

  @Test
  public void contextParameterInjectedInContextResolver() throws Exception {
    ContainerResponse response =
        launcher.service("GET", "/a/3", "", new MultivaluedHashMap<>(), null, env);

    assertEquals(200, response.getStatus());
    assertEquals("ContextResolverChecker: all fields injected", response.getEntity());
  }

  @Test
  public void contextParameterInjectedInExceptionMapper() throws Exception {
    ContainerResponse response =
        launcher.service("GET", "/a/4", "", new MultivaluedHashMap<>(), null, env);

    assertEquals(200, response.getStatus());
    assertEquals("ExceptionMapperChecker: all fields injected", response.getEntity());
  }
}
