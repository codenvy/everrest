/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.sample.book;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collection;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
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
        if (book == null)
            throw new BookNotFoundException(id);
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
