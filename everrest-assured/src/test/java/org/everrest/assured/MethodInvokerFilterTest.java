package org.everrest.assured;


import com.jayway.restassured.RestAssured;

import org.everrest.core.Filter;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.resource.GenericMethodResource;
import org.everrest.sample.book.Book;
import org.everrest.sample.book.BookService;
import org.everrest.sample.book.BookStorage;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        public void accept(GenericMethodResource genericMethodResource, Object[] params) throws WebApplicationException {
            if (id.equals("00000")) {
                params[0] = "123-1235-555";
            }
        }
    }

    @Mock
    private BookStorage bookStorage;

    @InjectMocks
    private BookService bookService;

    @Test
    public void shouldBeAbleToGetBook() throws Exception {
        Book book = new Book();
        book.setId("123-1235-555");
        when(bookStorage.getBook(eq("123-1235-555"))).thenReturn(book);

        //unsecure call to rest service
        RestAssured.given()
                   .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD)
                   .pathParam("id", "00000").
                           when().
                           get("/private/books/{id}").
                           then().statusCode(200);

        verify(bookStorage).getBook(eq("123-1235-555"));
    }
}
