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
import javax.xml.bind.JAXBElement;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: JAXBEntityTest.java 497 2009-11-08 13:19:25Z aparfonov $
 */
public class JAXBEntityTest extends BaseTest
{

   public void setUp() throws Exception
   {
      super.setUp();
   }

   @Path("/")
   public static class Resource1
   {
      @POST
      @Path("a")
      @Consumes("application/xml")
      public void m0(Book book)
      {
         assertEquals("Java and XML Data Binding", book.getTitle());
         assertEquals("Brett McLaughlin", book.getAuthor());
         assertEquals("EUR", book.getPrice().getCurrency());
         assertEquals("EUR", book.getMemberPrice().getCurrency());
         assertTrue(book.isSendByPost());
      }

      @POST
      @Path("b")
      @Consumes("application/xml")
      public void m1(JAXBElement<Book> e)
      {
         Book book = e.getValue();
         assertEquals("Java and XML Data Binding", book.getTitle());
         assertEquals("Brett McLaughlin", book.getAuthor());
         assertEquals("EUR", book.getPrice().getCurrency());
         assertEquals("EUR", book.getMemberPrice().getCurrency());
         assertTrue(book.isSendByPost());
      }
   }

   @Path("/")
   public static class Resource2
   {
      @GET
      @Produces("application/xml")
      public Book m0()
      {
         Book book = new Book();
         book.setAuthor("William Shakespeare");
         book.setTitle("Hamlet");
         book.setSendByPost(true);
         // ignore some fields
         return book;
      }

      // Without @Produces annotation also should work.
      @POST
      public Book m1()
      {
         Book book = new Book();
         book.setAuthor("William Shakespeare\n");
         book.setTitle("Hamlet\n");
         book.setSendByPost(false);
         // ignore some fields
         return book;
      }
   }

   private static final String XML_DATA =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<book send-by-post=\"true\">"
         + "<title>Java and XML Data Binding</title>" + "<author>Brett McLaughlin</author>"
         + "<price currency=\"EUR\">34.95</price>" + "<member-price currency=\"EUR\">26.56</member-price>" + "</book>";

   public void testJAXBEntityParameter() throws Exception
   {
      Resource1 r1 = new Resource1();
      registry(r1);
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();

      // JAXBElement
      h.putSingle("content-type", "application/xml");
      byte[] data = XML_DATA.getBytes("UTF-8");
      h.putSingle("content-length", "" + data.length);
      assertEquals(204, launcher.service("POST", "/a", "", h, data, null).getStatus());

      // Object transfered via XML (JAXB)
      assertEquals(204, launcher.service("POST", "/b", "", h, data, null).getStatus());
      unregistry(r1);
   }

   public void testJAXBEntityReturn() throws Exception
   {
      Resource2 r2 = new Resource2();
      registry(r2);
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();

      // Resource2#m1()
      h.putSingle("accept", "application/xml");
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      ContainerResponse response = launcher.service("GET", "/", "", h, null, writer, null);
      assertEquals(200, response.getStatus());
      assertEquals("application/xml", response.getContentType().toString());
      Book book = (Book)response.getEntity();
      assertEquals("Hamlet", book.getTitle());
      assertEquals("William Shakespeare", book.getAuthor());
      assertTrue(book.isSendByPost());

      // Resource2#m2()
      writer = new ByteArrayContainerResponseWriter();
      response = launcher.service("POST", "/", "", h, null, writer, null);
      assertEquals(200, response.getStatus());
      assertEquals("application/xml", response.getContentType().toString());
      book = (Book)response.getEntity();
      assertEquals("Hamlet\n", book.getTitle());
      assertEquals("William Shakespeare\n", book.getAuthor());
      assertFalse(book.isSendByPost());

      unregistry(r2);
   }
}
