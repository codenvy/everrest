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
package org.everrest.sample.book;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collection;

/** */
@Path("books")
public class BookService {
  @javax.inject.Inject private BookStorage bookStorage;

  @Path("{id}")
  @GET
  @Produces("application/json")
  public Book get(@PathParam("id") String id) throws BookNotFoundException {
    Book book = bookStorage.getBook(id);
    if (book == null) {
      throw new BookNotFoundException(id);
    }
    return book;
  }

  @GET
  @Produces("application/json")
  public Collection<Book> getAll() {
    return bookStorage.getAll();
  }

  @PUT
  @Consumes("application/json")
  public Response put(Book book, @Context UriInfo uriInfo) {
    String id = bookStorage.putBook(book);
    URI location = uriInfo.getBaseUriBuilder().path(getClass()).path(id).build();
    return Response.created(location).entity(location.toString()).type("text/plain").build();
  }
}
