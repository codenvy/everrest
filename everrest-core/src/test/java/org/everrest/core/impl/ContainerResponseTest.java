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

import static com.google.common.collect.Lists.newArrayList;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static jakarta.ws.rs.core.MediaType.WILDCARD_TYPE;
import static org.everrest.core.util.ParameterizedTypeImpl.newParameterizedType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyWriter;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.everrest.core.ApplicationContext;
import org.everrest.core.ContainerResponseWriter;
import org.everrest.core.impl.provider.StringEntityProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.stubbing.Answer;

public class ContainerResponseTest {
  private ProviderBinder providers;
  private ContainerRequest containerRequest;
  private ContainerResponseWriter containerResponseWriter;
  private ByteArrayOutputStream entityStream;

  private ContainerResponse containerResponse;

  @Before
  public void setUp() throws Exception {
    providers = mock(ProviderBinder.class);
    containerRequest = mock(ContainerRequest.class);
    ApplicationContext applicationContext = mock(ApplicationContext.class);
    when(applicationContext.getProviders()).thenReturn(providers);
    when(applicationContext.getContainerRequest()).thenReturn(containerRequest);
    when(applicationContext.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
    ApplicationContext.setCurrent(applicationContext);

    entityStream = new ByteArrayOutputStream();
    containerResponseWriter = mock(ContainerResponseWriter.class);
    containerResponse = new ContainerResponse(containerResponseWriter);
    doAnswer(writeEntity())
        .when(containerResponseWriter)
        .writeBody(same(containerResponse), any(MessageBodyWriter.class));
  }

  private Answer<Void> writeEntity() {
    return invocation -> {
      MessageBodyWriter messageBodyWriter = (MessageBodyWriter) invocation.getArguments()[1];
      Object entity = containerResponse.getEntity();
      if (entity != null) {
        messageBodyWriter.writeTo(
            entity,
            entity.getClass(),
            containerResponse.getEntityType(),
            null,
            containerResponse.getContentType(),
            containerResponse.getHttpHeaders(),
            entityStream);
      }
      return null;
    };
  }

  @Test
  public void setsNullResponse() throws Exception {
    containerResponse.setResponse(null);

    assertNull(containerResponse.getResponse());
    assertEquals(0, containerResponse.getStatus());
    assertNull(containerResponse.getEntity());
    assertNull(containerResponse.getEntityType());
    assertTrue(containerResponse.getHttpHeaders().isEmpty());
    assertNull(containerResponse.getContentType());
  }

  @Test
  public void setsResponse() throws Exception {
    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    headers.putSingle(CONTENT_TYPE, TEXT_PLAIN_TYPE);
    Response response = mockResponse(200, headers, "foo");

    containerResponse.setResponse(response);

    assertSame(response, containerResponse.getResponse());
    assertEquals(200, containerResponse.getStatus());
    assertEquals("foo", containerResponse.getEntity());
    assertEquals(String.class, containerResponse.getEntityType());
    assertEquals(headers, containerResponse.getHttpHeaders());
    assertEquals(TEXT_PLAIN_TYPE, containerResponse.getContentType());
  }

  @Test
  public void setsResponseWithGenericEntity() throws Exception {
    GenericEntity<List<String>> genericEntity =
        new GenericEntity<List<String>>(newArrayList("foo")) {};
    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    headers.putSingle(CONTENT_TYPE, TEXT_PLAIN_TYPE);
    Response response = mockResponse(200, headers, genericEntity);

    containerResponse.setResponse(response);

    assertSame(response, containerResponse.getResponse());
    assertEquals(200, containerResponse.getStatus());
    assertEquals(newArrayList("foo"), containerResponse.getEntity());
    assertEquals(newParameterizedType(List.class, String.class), containerResponse.getEntityType());
    assertEquals(headers, containerResponse.getHttpHeaders());
    assertEquals(TEXT_PLAIN_TYPE, containerResponse.getContentType());
  }

  @Test
  public void writesResponseWhenResponseEntityIsNullAndRequestMethodIsGET() throws Exception {
    when(providers.getMessageBodyWriter(String.class, String.class, null, TEXT_PLAIN_TYPE))
        .thenReturn(new StringEntityProvider());
    when(containerRequest.getMethod()).thenReturn("GET");

    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    Response response = mockResponse(200, headers, null);
    containerResponse.setResponse(response);

    containerResponse.writeResponse();

    assertEquals(200, containerResponse.getStatus());
    verify(containerResponseWriter).writeHeaders(containerResponse);
    verify(containerResponseWriter, never())
        .writeBody(same(containerResponse), any(MessageBodyWriter.class));
  }

  @Test
  public void
      writesResponseWhenResponseEntityIsNotNullRequestMethodIsHEADAndContentTypeIsSetExplicitly()
          throws Exception {
    when(providers.getMessageBodyWriter(String.class, String.class, null, TEXT_PLAIN_TYPE))
        .thenReturn(new StringEntityProvider());
    when(containerRequest.getMethod()).thenReturn("HEAD");

    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    headers.putSingle(CONTENT_TYPE, TEXT_PLAIN_TYPE);
    Response response = mockResponse(200, headers, "foo");
    containerResponse.setResponse(response);

    containerResponse.writeResponse();

    assertEquals(200, containerResponse.getStatus());
    InOrder inOrder = inOrder(containerResponseWriter);
    inOrder.verify(containerResponseWriter).writeHeaders(containerResponse);
    inOrder
        .verify(containerResponseWriter, never())
        .writeBody(same(containerResponse), any(MessageBodyWriter.class));
  }

  @Test
  public void
      writesResponseWhenResponseEntityIsNotNullRequestMethodIsGETAndContentTypeIsSetExplicitly()
          throws Exception {
    when(providers.getMessageBodyWriter(String.class, String.class, null, TEXT_PLAIN_TYPE))
        .thenReturn(new StringEntityProvider());
    when(containerRequest.getMethod()).thenReturn("GET");

    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    headers.putSingle(CONTENT_TYPE, TEXT_PLAIN_TYPE);
    Response response = mockResponse(200, headers, "foo");
    containerResponse.setResponse(response);

    containerResponse.writeResponse();

    assertEquals(200, containerResponse.getStatus());
    InOrder inOrder = inOrder(containerResponseWriter);
    inOrder.verify(containerResponseWriter).writeHeaders(containerResponse);
    inOrder
        .verify(containerResponseWriter, never())
        .writeBody(same(containerResponse), any(MessageBodyWriter.class));
  }

  @Test
  public void
      writesResponseWhenResponseEntityIsNotNullRequestMethodIsGETAcceptHeaderIsSetAndContentTypeIsNotSet()
          throws Exception {
    when(providers.getMessageBodyWriter(String.class, String.class, null, TEXT_PLAIN_TYPE))
        .thenReturn(new StringEntityProvider());
    when(containerRequest.getMethod()).thenReturn("GET");
    when(containerRequest.getAcceptableMediaTypes()).thenReturn(newArrayList(TEXT_PLAIN_TYPE));

    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    Response response = mockResponse(200, headers, "foo");
    containerResponse.setResponse(response);

    containerResponse.writeResponse();

    assertEquals(200, containerResponse.getStatus());
    assertEquals(TEXT_PLAIN_TYPE, containerResponse.getContentType());
    verify(containerResponseWriter).writeHeaders(containerResponse);
    verify(containerResponseWriter)
        .writeBody(same(containerResponse), any(MessageBodyWriter.class));
  }

  @Test
  public void
      writesResponseWhenResponseEntityIsNotNullRequestMethodIsGETAcceptHeaderIsNotSetAndContentTypeIsNotSet()
          throws Exception {
    when(providers.getMessageBodyWriter(String.class, String.class, null, TEXT_PLAIN_TYPE))
        .thenReturn(new StringEntityProvider());
    when(providers.getAcceptableWriterMediaTypes(String.class, String.class, null))
        .thenReturn(newArrayList(TEXT_PLAIN_TYPE));
    when(containerRequest.getMethod()).thenReturn("GET");
    when(containerRequest.getAcceptableMediaTypes()).thenReturn(newArrayList(WILDCARD_TYPE));
    when(containerRequest.getAcceptableMediaType(newArrayList(TEXT_PLAIN_TYPE)))
        .thenReturn(TEXT_PLAIN_TYPE);

    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    Response response = mockResponse(200, headers, "foo");
    containerResponse.setResponse(response);

    containerResponse.writeResponse();

    assertEquals(200, containerResponse.getStatus());
    verify(containerResponseWriter).writeHeaders(containerResponse);
    verify(containerResponseWriter)
        .writeBody(same(containerResponse), any(MessageBodyWriter.class));
  }

  @Test
  public void createsNOT_ACCEPTABLEResponseWhenNotFoundAnyWriterForEntity() throws Exception {
    when(containerRequest.getMethod()).thenReturn("GET");
    when(containerRequest.getAcceptableMediaTypes()).thenReturn(newArrayList(WILDCARD_TYPE));

    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    Response response = mockResponse(200, headers, "foo");
    containerResponse.setResponse(response);

    containerResponse.writeResponse();

    assertEquals(406, containerResponse.getStatus());
    verify(containerResponseWriter).writeHeaders(containerResponse);
    verify(containerResponseWriter)
        .writeBody(same(containerResponse), any(MessageBodyWriter.class));
  }

  @Test
  public void neverCreatesNOT_ACCEPTABLEResponseForHEADRequestEvenWhenNotFoundAnyWriterForEntity()
      throws Exception {
    when(containerRequest.getMethod()).thenReturn("HEAD");
    when(containerRequest.getAcceptableMediaTypes()).thenReturn(newArrayList(WILDCARD_TYPE));

    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    Response response = mockResponse(200, headers, "foo");
    containerResponse.setResponse(response);

    containerResponse.writeResponse();

    assertEquals(200, containerResponse.getStatus());
    verify(containerResponseWriter).writeHeaders(containerResponse);
    verify(containerResponseWriter, never())
        .writeBody(same(containerResponse), any(MessageBodyWriter.class));
  }

  private Response mockResponse(int status, MultivaluedMap<String, Object> headers, Object entity) {
    Response response = mock(Response.class);
    when(response.getStatus()).thenReturn(status);
    when(response.getMetadata()).thenReturn(headers);
    when(response.getEntity()).thenReturn(entity);
    return response;
  }
}
