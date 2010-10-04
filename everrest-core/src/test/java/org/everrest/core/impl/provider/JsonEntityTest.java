/*
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.everrest.core.impl.provider;

import org.everrest.core.generated.Book;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: JsonEntityTest.java 497 2009-11-08 13:19:25Z aparfonov $
 */
public class JsonEntityTest extends BaseTest
{

   @Path("/")
   public static class Resource1
   {
      @POST
      @Consumes("application/json")
      public void m1(Book book)
      {
         assertEquals("Hamlet", book.getTitle());
         assertEquals("William Shakespeare", book.getAuthor());
         assertTrue(book.isSendByPost());
      }
   }

   @Path("/")
   public static class Resource11
   {
      @POST
      @Consumes("application/json")
      public void m1(Book[] b)
      {
         assertEquals("Hamlet", b[0].getTitle());
         assertEquals("William Shakespeare", b[0].getAuthor());
         assertTrue(b[0].isSendByPost());

         assertEquals("Collected Stories", b[1].getTitle());
         assertEquals("Gabriel Garcia Marquez", b[1].getAuthor());
         assertTrue(b[1].isSendByPost());
      }
   }

   @Path("/")
   public static class Resource2
   {
      @GET
      @Produces("application/json")
      public Book m1()
      {
         Book book = new Book();
         book.setTitle("Hamlet");
         book.setAuthor("William Shakespeare");
         book.setSendByPost(true);
         return book;
      }

      // Without @Produces annotation also should work.
      @POST
      public Book m2()
      {
         Book book = new Book();
         book.setTitle("Hamlet\n");
         book.setAuthor("William Shakespeare\n");
         book.setSendByPost(false);
         return book;
      }
   }

   @Path("/")
   public static class Resource22
   {
      @GET
      @Produces("application/json")
      public Book[] m1()
      {
         Book book1 = new Book();
         book1.setTitle("Hamlet");
         book1.setAuthor("William Shakespeare");
         book1.setSendByPost(true);

         Book book2 = new Book();
         book2.setTitle("Collected Stories");
         book2.setAuthor("Gabriel Garcia Marquez");
         book2.setSendByPost(true);

         return new Book[]{book1, book2};
      }

      // Without @Produces annotation also should work.
      @POST
      public Book[] m2()
      {
         Book book1 = new Book();
         book1.setTitle("Hamlet\n");
         book1.setAuthor("William Shakespeare\n");
         book1.setSendByPost(false);

         Book book2 = new Book();
         book2.setTitle("Collected Stories\n");
         book2.setAuthor("Gabriel Garcia Marquez\n");
         book2.setSendByPost(false);

         return new Book[]{book1, book2};
      }
   }

   private byte[] jsonBookData;

   private byte[] jsonArrayData;

   public void setUp() throws Exception
   {
      super.setUp();
      jsonBookData =
         ("{\"title\":\"Hamlet\", \"author\":\"William Shakespeare\", \"sendByPost\":true}").getBytes("UTF-8");
      jsonArrayData =
         ("[{\"title\":\"Hamlet\", \"author\":\"William Shakespeare\", \"sendByPost\":true},"
            + "{\"title\":\"Collected Stories\", \"author\":\"Gabriel Garcia Marquez\", \"sendByPost\":true}]")
            .getBytes("UTF-8");
   }

   public void testJsonEntityBean() throws Exception
   {
      Resource1 r1 = new Resource1();
      registry(r1);
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      // Object is transfered via JSON
      h.putSingle("content-type", "application/json");
      // with JSON transformation for Book have restriction can't pass BigDecimal
      // (has not simple constructor and it is not in JSON known types)
      h.putSingle("content-length", "" + jsonBookData.length);
      assertEquals(204, launcher.service("POST", "/", "", h, jsonBookData, null).getStatus());
      unregistry(r1);
   }

   public void testJsonEntityArray() throws Exception
   {
      Resource11 r11 = new Resource11();
      registry(r11);
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      // Object is transfered via JSON
      h.putSingle("content-type", "application/json");
      h.putSingle("content-length", "" + jsonArrayData.length);
      assertEquals(204, launcher.service("POST", "/", "", h, jsonArrayData, null).getStatus());
      unregistry(r11);
   }

   public void testJsonReturnBean() throws Exception
   {
      Resource2 r2 = new Resource2();
      registry(r2);
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      h.putSingle("accept", "application/json");
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

      // Resource2#m1()
      ContainerResponse response = launcher.service("GET", "/", "", h, null, writer, null);
      assertEquals(200, response.getStatus());
      assertEquals("application/json", response.getContentType().toString());
      Book book = (Book)response.getEntity();
      assertEquals("Hamlet", book.getTitle());
      assertEquals("William Shakespeare", book.getAuthor());
      assertTrue(book.isSendByPost());

      // Resource2#m2()
      response = launcher.service("POST", "/", "", h, null, writer, null);
      assertEquals(200, response.getStatus());
      assertEquals("application/json", response.getContentType().toString());
      book = (Book)response.getEntity();
      assertEquals("Hamlet\n", book.getTitle());
      assertEquals("William Shakespeare\n", book.getAuthor());
      assertFalse(book.isSendByPost());
      writer = new ByteArrayContainerResponseWriter();
      unregistry(r2);
   }

}
