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

import org.everrest.json.BeanWithTransientField;
import org.everrest.json.Book;
import org.everrest.json.BookStorage;
import org.everrest.json.BookWrapper;
import org.everrest.json.JavaMapBean;
import org.everrest.json.impl.JsonGeneratorImpl;
import org.everrest.json.value.JsonValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: JsonGeneratorTest.java 34417 2009-07-23 14:42:56Z dkatayev $
 */
public class JsonGeneratorTest extends TestCase
{

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
   }

   public void testSimpleObject() throws Exception
   {
      int _pages = 386;
      long _isdn = 93011099534534L;
      double _price = 19.37;
      String _title = "JUnit in Action";
      String _author = "Vincent Masson";
      boolean _delivery = true;
      boolean _availability = true;
      Book book = new Book();
      book.setAuthor(_author);
      book.setTitle(_title);
      book.setPages(_pages);
      book.setPrice(_price);
      book.setIsdn(_isdn);
      book.setAvailability(_availability);
      book.setDelivery(_delivery);

      JsonValue jsonValue = new JsonGeneratorImpl().createJsonObject(book);
      assertTrue(jsonValue.isObject());
      assertEquals(_author, jsonValue.getElement("author").getStringValue());
      assertEquals(_title, jsonValue.getElement("title").getStringValue());
      assertEquals(_pages, jsonValue.getElement("pages").getIntValue());
      assertEquals(_price, jsonValue.getElement("price").getDoubleValue());
      assertEquals(_isdn, jsonValue.getElement("isdn").getLongValue());
      assertEquals(_delivery, jsonValue.getElement("delivery").getBooleanValue());
      assertEquals(_availability, jsonValue.getElement("availability").getBooleanValue());
   }

   public void testSimpleObject2() throws Exception
   {
      int _pages = 386;
      int _isdn = 930110995;
      double _price = 19.37;
      String _title = "JUnit in Action";
      String _author = "Vincent Masson";
      boolean _delivery = false;
      boolean _availability = true;
      Book book = new Book();
      book.setAuthor(_author);
      book.setTitle(_title);
      book.setPages(_pages);
      book.setPrice(_price);
      book.setIsdn(_isdn);
      book.setAvailability(_availability);
      book.setDelivery(_delivery);

      BookWrapper bookWrapper = new BookWrapper();
      bookWrapper.setBook(book);
      JsonValue jsonValue = new JsonGeneratorImpl().createJsonObject(bookWrapper);
      assertTrue(jsonValue.isObject());
      assertEquals(_author, jsonValue.getElement("book").getElement("author").getStringValue());
      assertEquals(_title, jsonValue.getElement("book").getElement("title").getStringValue());
      assertEquals(_pages, jsonValue.getElement("book").getElement("pages").getIntValue());
      assertEquals(_price, jsonValue.getElement("book").getElement("price").getDoubleValue());
      assertEquals(_isdn, jsonValue.getElement("book").getElement("isdn").getLongValue());
      assertEquals(_delivery, jsonValue.getElement("book").getElement("delivery").getBooleanValue());
      assertEquals(_availability, jsonValue.getElement("book").getElement("availability").getBooleanValue());
   }

   public void testSimpleObject3() throws Exception
   {
      Book book = new Book();
      book.setAuthor("Vincent Masson");
      book.setTitle("JUnit in Action");
      book.setPages(386);
      book.setPrice(19.37);
      book.setIsdn(93011099534534L);
      book.setAvailability(true);
      book.setDelivery(true);
      List<Book> l = new ArrayList<Book>();
      l.add(book);
      book = new Book();
      book.setAuthor("Christian Gross");
      book.setTitle("Beginning C# 2008 from novice to professional");
      book.setPages(511);
      book.setPrice(23.56);
      book.setIsdn(9781590598696L);
      book.setAvailability(true);
      book.setDelivery(true);
      l.add(book);
      book = new Book();
      book.setAuthor("Chuck Easttom");
      book.setTitle("Advanced JavaScript, Third Edition");
      book.setPages(617);
      book.setPrice(25.99);
      book.setIsdn(9781598220339L);
      book.setAvailability(true);
      book.setDelivery(true);
      l.add(book);
      BookStorage bookStorage = new BookStorage();
      bookStorage.setBooks(l);
      JsonValue jsonValue = new JsonGeneratorImpl().createJsonObject(bookStorage);
      assertTrue(jsonValue.isObject());
      Iterator<JsonValue> iterator = jsonValue.getElement("books").getElements();
      assertEquals(l.get(0).getTitle(), iterator.next().getElement("title").getStringValue());
      assertEquals(l.get(1).getTitle(), iterator.next().getElement("title").getStringValue());
      assertEquals(l.get(2).getTitle(), iterator.next().getElement("title").getStringValue());
   }

   public void test2() throws Exception
   {
      JavaMapBean mb = new JavaMapBean();
      Map<String, Book> m = new HashMap<String, Book>();
      Book book = new Book();
      book.setAuthor("Vincent Masson");
      book.setTitle("JUnit in Action");
      book.setPages(386);
      book.setPrice(19.37);
      book.setIsdn(93011099534534L);
      m.put("test", book);
      mb.setHashMap((HashMap<String, Book>)m);

      List<Book> l = new ArrayList<Book>();
      l.add(book);
      book = new Book();
      book.setAuthor("Christian Gross");
      book.setTitle("Beginning C# 2008 from novice to professional");
      book.setPages(511);
      book.setPrice(23.56);
      book.setIsdn(9781590598696L);
      l.add(book);
      book = new Book();
      book.setAuthor("Chuck Easttom");
      book.setTitle("Advanced JavaScript, Third Edition");
      book.setPages(617);
      book.setPrice(25.99);
      book.setIsdn(9781598220339L);
      l.add(book);
      Map<String, List<Book>> hu = new HashMap<String, List<Book>>();
      hu.put("1", l);
      hu.put("2", l);
      hu.put("3", l);
      mb.setMapList(hu);

      Map<String, String> str = new HashMap<String, String>();
      str.put("key1", "value1");
      str.put("key2", "value2");
      str.put("key3", "value3");
      mb.setStrings(str);

      JsonValue jsonValue = new JsonGeneratorImpl().createJsonObject(mb);

      assertEquals(str.get("key2"), jsonValue.getElement("strings").getElement("key2").getStringValue());

      assertNotNull(jsonValue.getElement("hashMap"));

      assertNotNull(jsonValue.getElement("mapList"));
      assertEquals("JUnit in Action", jsonValue.getElement("mapList").getElement("3").getElements().next().getElement(
         "title").getStringValue());
      // System.out.println(jsonValue);
   }

   public void testBeanWithTransientField() throws Exception
   {
      BeanWithTransientField trBean = new BeanWithTransientField();
      JsonValue jsonValue = new JsonGeneratorImpl().createJsonObject(trBean);
      assertEquals("visible", jsonValue.getElement("field").getStringValue());
      try
      {
         assertEquals("invisible", jsonValue.getElement("transientField").getStringValue());
         fail("It must not be serialized");
      }
      catch (NullPointerException e)
      {
      }
   }

}
