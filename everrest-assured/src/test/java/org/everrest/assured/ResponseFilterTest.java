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

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.RestAssured;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.ResponseFilter;
import org.everrest.sample.book.Book;
import org.everrest.sample.book.BookService;
import org.everrest.sample.book.BookStorage;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class ResponseFilterTest {

  @Filter
  public static class ResponseFilter1 implements ResponseFilter {
    @Context private UriInfo uriInfo;
    @Context private HttpHeaders httpHeaders;
    private Providers providers;
    private HttpServletRequest httpRequest;

    public ResponseFilter1(@Context Providers providers, @Context HttpServletRequest httpRequest) {
      this.providers = providers;
      this.httpRequest = httpRequest;
    }

    public void doFilter(GenericContainerResponse response) {
      if (uriInfo != null && httpHeaders != null && providers != null && httpRequest != null) {
        response.setResponse(
            Response.status(200).entity("to be or not to be").type("text/plain").build());
      }
    }
  }

  ResponseFilter1 responseFilter1;

  @Mock private BookStorage bookStorage;

  @InjectMocks private BookService bookService;

  @Test
  public void shouldChangeMethodName() throws Exception {
    Book book = new Book();
    book.setId("123-1235-555");
    when(bookStorage.getBook(eq("123-1235-555"))).thenReturn(book);

    // unsecure call to rest service
    RestAssured.given()
        .pathParam("id", "123-1235-555")
        .when()
        .get("/books/{id}")
        .then()
        .statusCode(200)
        .contentType("text/plain")
        .body(equalTo("to be or not to be"));

    verify(bookStorage).getBook(eq("123-1235-555"));
  }

  @Test
  public void shouldBeAbleToWorkTwice() throws Exception {
    Book book = new Book();
    book.setId("123-1235-555");
    when(bookStorage.getBook(eq("123-1235-555"))).thenReturn(book);

    // unsecure call to rest service
    RestAssured.given()
        .pathParam("id", "123-1235-555")
        .when()
        .get("/books/{id}")
        .then()
        .statusCode(200)
        .contentType("text/plain")
        .body(equalTo("to be or not to be"));

    verify(bookStorage).getBook(eq("123-1235-555"));
  }
}
