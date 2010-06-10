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
import org.everrest.core.generated.MemberPrice;
import org.everrest.core.generated.Price;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.MultivaluedMapImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class JAXBEntityProviderTest extends BaseTest
{

   private byte[] data;

   private MediaType mediaType;

   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      setContext();
      mediaType = new MediaType("application", "xml");
      data =
         ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<book send-by-post=\"true\">"
            + "<title>Java and XML Data Binding</title>" + "<author>Brett McLaughlin</author>" + "<price>34.95</price>"
            + "<member-price currency=\"US\">26.56</member-price>" + "</book>").getBytes("UTF-8");
   }

   public static JAXBElement<Book> m(JAXBElement<Book> je)
   {
      return je;
   };

   @SuppressWarnings("unchecked")
   public void testReadJAXBElement() throws Exception
   {
      Method m = getClass().getMethod("m", new Class[]{JAXBElement.class});
      assertNotNull(m);
      Class<?> type = m.getParameterTypes()[0];
      Type genericType = m.getGenericParameterTypes()[0];
      MessageBodyReader reader = providers.getMessageBodyReader(type, genericType, null, mediaType);
      assertNotNull(reader);
      assertTrue(reader.isReadable(type, genericType, null, mediaType));
      InputStream in = new ByteArrayInputStream(data);
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      h.putSingle(HttpHeaders.CONTENT_LENGTH, "" + data.length);
      JAXBElement<Book> je = (JAXBElement<Book>)reader.readFrom(type, genericType, null, mediaType, h, in);
      assertTrue("Java and XML Data Binding".equals(je.getValue().getTitle()));
   }

   @SuppressWarnings("unchecked")
   public void testWriteJAXBElement() throws Exception
   {
      Method m = getClass().getMethod("m", new Class[]{JAXBElement.class});
      assertNotNull(m);
      Class<?> returnType = m.getReturnType();
      Type genericReturnType = m.getGenericReturnType();
      MessageBodyWriter writer = providers.getMessageBodyWriter(returnType, genericReturnType, null, mediaType);
      assertNotNull(writer);
      assertTrue(writer.isWriteable(returnType, genericReturnType, null, mediaType));
      JAXBContext ctx = JAXBContext.newInstance(Book.class);
      Unmarshaller um = ctx.createUnmarshaller();
      Source src = new StreamSource(new ByteArrayInputStream(data));
      JAXBElement<Book> je = um.unmarshal(src, Book.class);
      writer.writeTo(je, returnType, genericReturnType, null, mediaType, null, new ByteArrayOutputStream());
   }

   @SuppressWarnings("unchecked")
   public void testReadJAXBObject() throws Exception
   {
      MessageBodyReader prov = providers.getMessageBodyReader(Book.class, null, null, mediaType);
      assertNotNull(prov);
      assertTrue(prov.isReadable(Book.class, Book.class, null, mediaType));
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      h.putSingle(HttpHeaders.CONTENT_LENGTH, "" + data.length);
      Book book = (Book)prov.readFrom(Book.class, Book.class, null, mediaType, h, new ByteArrayInputStream(data));
      assertEquals("Brett McLaughlin", book.getAuthor());
   }

   @SuppressWarnings("unchecked")
   public void testWriteJAXBObject() throws Exception
   {
      MessageBodyWriter writer = providers.getMessageBodyWriter(Book.class, null, null, mediaType);
      assertNotNull(writer);
      assertTrue(writer.isWriteable(Book.class, Book.class, null, mediaType));
      Book book = new Book();
      book.setAuthor("William Shakespeare");
      book.setTitle("Hamlet");
      book.setPrice(createPrice("EUR", 15.15F));
      book.setMemberPrice(createMemberPrice("EUR", 14.73F));
      book.setSendByPost(true);
      writer.writeTo(book, Book.class, Book.class, null, mediaType, null, new ByteArrayOutputStream());
   }

   private static Price createPrice(String currency, Float value)
   {
      Price price = new Price();
      price.setCurrency(currency);
      price.setValue(new BigDecimal(value));
      return price;
   }

   private static MemberPrice createMemberPrice(String currency, Float value)
   {
      MemberPrice mprice = new MemberPrice();
      mprice.setCurrency(currency);
      mprice.setValue(new BigDecimal(value));
      return mprice;
   }

}
