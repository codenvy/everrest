/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.assured;

import org.everrest.sample.book.BookNotFoundExceptionMapper;
import org.everrest.sample.book.BookService;
import org.everrest.sample.book.BookStorage;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.ext.ExceptionMapper;

import static com.jayway.restassured.RestAssured.given;
import static org.mockito.Mockito.*;

/** Test of exception mapper testing. */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class ExceptionMapperTest {
    @Mock
    private BookStorage bookStorage;

    private BookNotFoundExceptionMapper notFoundMapper = new BookNotFoundExceptionMapper();

    @InjectMocks
    private BookService bookService;

    @Test
    public void shoudlThrow404IfBookIsNotFound() throws Exception {
        when(bookStorage.getBook(eq("123-1235-555"))).thenReturn(null);

        //unsecure call to rest service
        given().
                pathParam("id", "123-1235-555").
                expect().statusCode(404).when().get("/books/{id}");

        verify(bookStorage).getBook(eq("123-1235-555"));
    }
}
