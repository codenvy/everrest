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
package org.everrest.assured;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import io.restassured.response.Response;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import org.everrest.sample.book.Book;
import org.everrest.sample.book.BookService;
import org.everrest.sample.book.BookStorage;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Test of exception mapper testing. */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class ProviderTest {
  @Mock private BookStorage bookStorage;

  private BookJsonProvider bookJsonProvider;

  @InjectMocks private BookService bookService;

  @Test
  public void shoudlThrow404IfBookIsNotFound() throws Exception {
    when(bookStorage.getBook(eq("123-1235-555"))).thenReturn(new Book());

    // unsecure call to rest service
    final Response response =
        given().pathParam("id", "123-1235-555").expect().statusCode(200).when().get("/books/{id}");
    assertEquals(response.getBody().print(), "ping");
  }

  @Provider
  @Produces(MediaType.APPLICATION_JSON)
  public static class BookJsonProvider implements MessageBodyReader<Book>, MessageBodyWriter<Book> {
    @Override
    public boolean isReadable(
        Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
      return type.isAssignableFrom(Book.class);
    }

    @Override
    public Book readFrom(
        Class<Book> type,
        Type genericType,
        Annotation[] annotations,
        MediaType mediaType,
        MultivaluedMap<String, String> httpHeaders,
        InputStream entityStream)
        throws IOException, WebApplicationException {
      return null;
    }

    @Override
    public boolean isWriteable(
        Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
      return type.isAssignableFrom(Book.class);
    }

    @Override
    public long getSize(
        Book book, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
      return -1;
    }

    @Override
    public void writeTo(
        Book book,
        Class<?> type,
        Type genericType,
        Annotation[] annotations,
        MediaType mediaType,
        MultivaluedMap<String, Object> httpHeaders,
        OutputStream entityStream)
        throws IOException, WebApplicationException {
      entityStream.write("ping".getBytes());
    }
  }
}
