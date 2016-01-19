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
package org.everrest.sample.groovy

import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo

@Path('books')
class BookService {

    @Inject
    BookStorage bookStorage

    @Path('{id}')
    @GET
    @Produces('application/json')
    Book get(@PathParam('id') String id) throws BookNotFoundException {
        Book book = bookStorage.getBook(id)
        if (book == null)
            throw new BookNotFoundException(id)
        book
    }

    @GET
    @Produces('application/json')
    public Collection<Book> getAll() {
        bookStorage.getAll()
    }

    @PUT
    @Consumes('application/json')
    public Response put(Book book, @Context UriInfo uriInfo) {
        String id = bookStorage.putBook(book)
        URI location = uriInfo.getBaseUriBuilder().path(getClass()).path(id).build()
        Response.created(location).entity(location.toString()).type('text/plain').build()
    }
}