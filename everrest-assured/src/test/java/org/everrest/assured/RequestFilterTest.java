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

import static com.jayway.restassured.RestAssured.expect;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.sample.book.Book;
import org.everrest.sample.book.BookService;
import org.everrest.sample.book.BookStorage;
import org.hamcrest.Matchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class RequestFilterTest {

  @Filter
  public static class RequestFilter1 implements RequestFilter {
    @Context private UriInfo uriInfo;
    @Context private HttpHeaders httpHeaders;
    private Providers providers;
    private HttpServletRequest httpRequest;

    public RequestFilter1(@Context Providers providers, @Context HttpServletRequest httpRequest) {
      this.providers = providers;
      this.httpRequest = httpRequest;
    }

    public void doFilter(GenericContainerRequest request) {
      if (uriInfo != null && httpHeaders != null && providers != null && httpRequest != null) {
        request.setMethod("GET");
      }
    }
  }

  RequestFilter1 requestFilter1;

  @Mock private BookStorage bookStorage;

  @InjectMocks private BookService bookService;

  @Test
  public void shouldChangeMethodName() throws Exception {
    Collection<Book> bookCollection = new ArrayList<Book>();
    Book book = new Book();
    book.setId("123-1235-555");
    bookCollection.add(book);
    when(bookStorage.getAll()).thenReturn(bookCollection);

    // unsecure call to rest service
    expect().body("id", Matchers.hasItem("123-1235-555")).when().post("/books");

    verify(bookStorage).getAll();
  }

  @Test
  public void shouldBeAbleToWorkTwice() throws Exception {
    Collection<Book> bookCollection = new ArrayList<Book>();
    Book book = new Book();
    book.setId("123-1235-555");
    bookCollection.add(book);
    when(bookStorage.getAll()).thenReturn(bookCollection);

    // unsecure call to rest service
    expect().body("id", Matchers.hasItem("123-1235-555")).when().post("/books");

    verify(bookStorage).getAll();
  }
}
