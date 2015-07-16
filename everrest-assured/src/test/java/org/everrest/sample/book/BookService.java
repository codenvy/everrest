/**
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.everrest.sample.book;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collection;

/**
 *
 */
@Path("books")
public class BookService {
    @javax.inject.Inject
    private BookStorage bookStorage;

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
