package org.everrest.sample.groovy

import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class BookNotFoundExceptionMapper implements ExceptionMapper<BookNotFoundException>
{
   Response toResponse(BookNotFoundException exception)
   {
      Response.status(404).entity(exception.getMessage()).type('text/plain').build()
   }
}