package org.everrest.sample.groovy

import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Produces
import javax.ws.rs.Consumes
import javax.ws.rs.core.Response
import javax.ws.rs.core.Context
import javax.ws.rs.core.UriInfo

@Path('books')
class BookService
{
   BookStorage bookStorage
   
   @Path('{id}')
   @GET
   @Produces('application/json')
   Book get(@PathParam('id') String id) throws BookNotFoundException
   {
      Book book = bookStorage.getBook(id)
      if (book == null)
         throw new BookNotFoundException(id)
      book
   }
   
   @GET
   @Produces('application/json')
   public Collection<Book> getAll()
   {
      bookStorage.getAll()
   }
   
   @PUT
   @Consumes('application/json')
   public Response put(Book book, @Context UriInfo uriInfo)
   {
      String id = bookStorage.putBook(book)
      URI location = uriInfo.getBaseUriBuilder().path(getClass()).path(id).build()
      Response.created(location).entity(location.toString()).type('text/plain').build()
   }
}