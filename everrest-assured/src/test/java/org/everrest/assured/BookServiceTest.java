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
import org.everrest.sample.book.Book;
import org.everrest.sample.book.BookService;
import org.everrest.sample.book.BookStorage;
import org.hamcrest.Matchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.ITestContext;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class BookServiceTest {
  @Mock private BookStorage bookStorage;

  @InjectMocks private BookService bookService;

  @Test
  public void testName(ITestContext context) throws Exception {
    Collection<Book> bookCollection = new ArrayList<Book>();
    Book book = new Book();
    book.setId("123-1235-555");
    bookCollection.add(book);
    when(bookStorage.getAll()).thenReturn(bookCollection);

    // unsecure call to rest service
    expect().body("id", Matchers.hasItem("123-1235-555")).when().get("/books");

    verify(bookStorage).getAll();
  }
}
