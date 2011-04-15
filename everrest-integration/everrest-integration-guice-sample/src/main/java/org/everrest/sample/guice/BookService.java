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

package org.everrest.sample.guice;

import com.google.inject.Inject;

import java.net.URI;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
@Path("books")
public class BookService
{
   @Inject
   private BookStorage bookStorage;

   @Path("{id}")
   @GET
   @Produces("application/json")
   public Book get(@PathParam("id") String id) throws BookNotFoundException
   {
      Book book = bookStorage.getBook(id);
      if (book == null)
         throw new BookNotFoundException(id);
      return book;
   }


   @GET
   @Produces("application/json")
   public Collection<Book> getAll()
   {
      return bookStorage.getAll();
   }

   @PUT
   @Consumes("application/json")
   public Response put(Book book, @Context UriInfo uriInfo)
   {
      String id = bookStorage.putBook(book);
      URI location = uriInfo.getBaseUriBuilder().path(getClass()).path(id).build();
      return Response.created(location).entity(location.toString()).type("text/plain").build();
   }
}
