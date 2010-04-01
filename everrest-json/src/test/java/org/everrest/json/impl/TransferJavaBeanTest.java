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
package org.everrest.json.impl;

import junit.framework.TestCase;

import org.everrest.json.Book;
import org.everrest.json.JsonHandler;
import org.everrest.json.JsonParser;
import org.everrest.json.JsonWriter;
import org.everrest.json.impl.BeanBuilder;
import org.everrest.json.impl.JsonDefaultHandler;
import org.everrest.json.impl.JsonGeneratorImpl;
import org.everrest.json.impl.JsonParserImpl;
import org.everrest.json.impl.JsonWriterImpl;
import org.everrest.json.value.JsonValue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: TransferJavaBeanTest.java 34417 2009-07-23 14:42:56Z dkatayev $
 */
public class TransferJavaBeanTest extends TestCase
{

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
   }

   public void testTransfer() throws Exception
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      String title = "JUnit in Action";
      String author = "Vincent Masson";
      int pages = 386;
      double price = 19.37;
      long isdn = 930110995;

      Book book = new Book();
      book.setAuthor(author);
      book.setTitle(title);
      book.setPages(pages);
      book.setPrice(price);
      book.setIsdn(isdn);

      JsonValue jv = new JsonGeneratorImpl().createJsonObject(book);
      JsonWriter jsonWriter = new JsonWriterImpl(out);
      jv.writeTo(jsonWriter);
      jsonWriter.flush();
      jsonWriter.close();

      ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
      JsonParser jsonParser = new JsonParserImpl();
      JsonHandler jsonHandler = new JsonDefaultHandler();

      jsonParser.parse(in, jsonHandler);
      JsonValue jsonValue = jsonHandler.getJsonObject();
      Book newBook = (Book)new BeanBuilder().createObject(Book.class, jsonValue);
      assertEquals(author, newBook.getAuthor());
      assertEquals(title, newBook.getTitle());
      assertEquals(pages, newBook.getPages());
      assertEquals(price, newBook.getPrice());
      assertEquals(isdn, newBook.getIsdn());
   }

}
