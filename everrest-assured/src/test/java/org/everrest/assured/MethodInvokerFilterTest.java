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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.RestAssured;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import org.everrest.core.Filter;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.resource.GenericResourceMethod;
import org.everrest.sample.book.Book;
import org.everrest.sample.book.BookService;
import org.everrest.sample.book.BookStorage;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class MethodInvokerFilterTest {

  @SuppressWarnings("unused")
  MyFilter INV_FILTER;

  @Filter
  @Path("/books/{id}")
  public static class MyFilter implements MethodInvokerFilter {

    @PathParam("id")
    String id;

    @Override
    public void accept(GenericResourceMethod resourceMethod, Object[] params)
        throws WebApplicationException {
      if (id.equals("00000")) {
        params[0] = "123-1235-555";
      }
    }
  }

  @Mock private BookStorage bookStorage;

  @InjectMocks private BookService bookService;

  @Test
  public void shouldBeAbleToGetBook() throws Exception {
    Book book = new Book();
    book.setId("123-1235-555");
    when(bookStorage.getBook(eq("123-1235-555"))).thenReturn(book);

    // unsecure call to rest service
    RestAssured.given()
        .auth()
        .basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD)
        .pathParam("id", "00000")
        .when()
        .get("/private/books/{id}")
        .then()
        .statusCode(200);

    verify(bookStorage).getBook(eq("123-1235-555"));
  }
}
