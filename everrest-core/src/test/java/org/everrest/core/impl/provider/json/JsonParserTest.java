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
package org.everrest.core.impl.provider.json;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class JsonParserTest extends JsonTest
{

   ArrayList<Book> sourceCollection;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      sourceCollection = new ArrayList<Book>(3);
      sourceCollection.add(junitBook);
      sourceCollection.add(csharpBook);
      sourceCollection.add(javaScriptBook);
   }




   public void testCollection() throws Exception
   {
      // test restore Collection of standard Java Object from JSON source
      JsonParser jsonParser = new JsonParser();
      String jsonString =
         "{" + "\"strings\":[\"JUnit in Action\",\"Advanced JavaScript\",\"Beginning C# 2008\"],"
            + "\"chars\":[\"b\",\"o\",\"o\",\"k\"]," + "\"integers\":[386, 421, 565]" + "}";
      jsonParser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));
      JsonValue jsonValue = jsonParser.getJsonObject();
      JavaCollectionBean o = ObjectBuilder.createObject(JavaCollectionBean.class, jsonValue);
      List<String> s = o.getStrings();
      assertEquals(3, s.size());
      assertEquals("JUnit in Action", s.get(0));
      assertEquals("Advanced JavaScript", s.get(1));
      assertEquals("Beginning C# 2008", s.get(2));
      List<Character> c = o.getChars();
      assertEquals('b', c.get(0).charValue());
      assertEquals('o', c.get(1).charValue());
      assertEquals('o', c.get(2).charValue());
      assertEquals('k', c.get(3).charValue());
      List<Integer> i = o.getIntegers();
      assertEquals(386, i.get(0).intValue());
      assertEquals(421, i.get(1).intValue());
      assertEquals(565, i.get(2).intValue());
      // more testing for other type of Collection with custom object
   }

   public void testCollection2() throws Exception
   {
      // test restore Collection of other Java Object from JSON source
      JsonParser jsonParser = new JsonParser();
      // check restore different type of Collection
      jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(
         "CollectionTest.txt")));
      JsonValue jsonValue = jsonParser.getJsonObject();
      JavaCollectionBean o = ObjectBuilder.createObject(JavaCollectionBean.class, jsonValue);

      assertEquals(3, o.getArrayList().size());
      assertTrue(o.getArrayList().get(0).equals(sourceCollection.get(0)));
      assertTrue(o.getArrayList().get(1).equals(sourceCollection.get(1)));
      assertTrue(o.getArrayList().get(2).equals(sourceCollection.get(2)));

      assertEquals(3, o.getVector().size());
      assertTrue(o.getVector().get(0).equals(sourceCollection.get(0)));
      assertTrue(o.getVector().get(1).equals(sourceCollection.get(1)));
      assertTrue(o.getVector().get(2).equals(sourceCollection.get(2)));

      assertEquals(3, o.getLinkedList().size());
      assertTrue(o.getLinkedList().get(0).equals(sourceCollection.get(0)));
      assertTrue(o.getLinkedList().get(1).equals(sourceCollection.get(1)));
      assertTrue(o.getLinkedList().get(2).equals(sourceCollection.get(2)));

      assertEquals(3, o.getLinkedHashSet().size());

      assertEquals(3, o.getHashSet().size());

      assertEquals(3, o.getList().size());
      assertTrue(o.getList().get(0).equals(sourceCollection.get(0)));
      assertTrue(o.getList().get(1).equals(sourceCollection.get(1)));
      assertTrue(o.getList().get(2).equals(sourceCollection.get(2)));

      assertEquals(3, o.getSet().size());

      assertEquals(3, o.getQueue().size());

      assertEquals(3, o.getCollection().size());

      assertEquals(3, o.getArray().length);
      assertTrue(o.getArray()[0].equals(sourceCollection.get(0)));
      assertTrue(o.getArray()[1].equals(sourceCollection.get(1)));
      assertTrue(o.getArray()[2].equals(sourceCollection.get(2)));
   }

   public void testMap() throws Exception
   {
      JsonParser jsonParser = new JsonParser();
      String jsonString =
         "{" + "\"strings\":{" + "\"book\":\"Beginning C# 2008\"," + "\"author\":\"Christian Gross\"" + "},"
            + "\"integers\":{" + "\"one\":1," + "\"two\":2," + "\"three\":3" + "}," + "\"booleans\":{"
            + "\"true\":true," + "\"false\":false" + "}" + "}";
      jsonParser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));
      JsonValue jsonValue = jsonParser.getJsonObject();
      JavaMapBean o = ObjectBuilder.createObject(JavaMapBean.class, jsonValue);

      assertEquals("Beginning C# 2008", o.getStrings().get("book"));
      assertEquals("Christian Gross", o.getStrings().get("author"));

      assertEquals(1, o.getIntegers().get("one").intValue());
      assertEquals(2, o.getIntegers().get("two").intValue());
      assertEquals(3, o.getIntegers().get("three").intValue());

      assertTrue(o.getBooleans().get("true"));
      assertFalse(o.getBooleans().get("false"));
   }

   public void testMap2() throws Exception
   {
      JsonParser jsonParser = new JsonParser();
      jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(
         "MapTest.txt")));
      JsonValue jv = jsonParser.getJsonObject();
      JavaMapBean o = ObjectBuilder.createObject(JavaMapBean.class, jv);

      assertTrue(o.getMap().get("JUnit").equals(sourceCollection.get(0)));
      assertTrue(o.getMap().get("C#").equals(sourceCollection.get(1)));
      assertTrue(o.getMap().get("JavaScript").equals(sourceCollection.get(2)));

      assertTrue(o.getHashMap().get("JUnit").equals(sourceCollection.get(0)));
      assertTrue(o.getHashMap().get("C#").equals(sourceCollection.get(1)));
      assertTrue(o.getHashMap().get("JavaScript").equals(sourceCollection.get(2)));

      assertTrue(o.getHashtable().get("JUnit").equals(sourceCollection.get(0)));
      assertTrue(o.getHashtable().get("C#").equals(sourceCollection.get(1)));
      assertTrue(o.getHashtable().get("JavaScript").equals(sourceCollection.get(2)));

      assertTrue(o.getLinkedHashMap().get("JUnit").equals(sourceCollection.get(0)));
      assertTrue(o.getLinkedHashMap().get("C#").equals(sourceCollection.get(1)));
      assertTrue(o.getLinkedHashMap().get("JavaScript").equals(sourceCollection.get(2)));

   }

   public void testBean() throws Exception
   {
      JsonParser jsonParser = new JsonParser();
      jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(
         "BookStorage.txt")));
      JsonValue jv = jsonParser.getJsonObject();
      BookStorage o = ObjectBuilder.createObject(BookStorage.class, jv);
      assertTrue(o.getBooks().get(0).equals(sourceCollection.get(0)));
      assertTrue(o.getBooks().get(1).equals(sourceCollection.get(1)));
      assertTrue(o.getBooks().get(2).equals(sourceCollection.get(2)));
   }

   public void testArray() throws Exception
   {
      String source = "{\"array\":[\"a\",\"b\",\"c\"]}";
      JsonParser jsonParser = new JsonParser();
      jsonParser.parse(new ByteArrayInputStream(source.getBytes()));
      JsonValue jsonValue = jsonParser.getJsonObject();
      System.out.println(jsonValue.getElement("array").isArray());
   }

   public void testMultiDimensionArray() throws Exception
   {
      JsonParser jsonParser = new JsonParser();
      jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(
         "MultiDimension.txt")));
      JsonValue jsonValue = jsonParser.getJsonObject();
      //    System.out.println(jsonValue);
      assertTrue(jsonValue.isObject());
      assertTrue(jsonValue.getElement("books").isArray());
      assertTrue(jsonValue.getElement("books").getElements().next().isArray());
      assertTrue(jsonValue.getElement("books").getElements().next().getElements().next().isArray());
      assertTrue(jsonValue.getElement("books").getElements().next().getElements().next().getElements().next()
         .isObject());
      assertEquals("JUnit in Action", jsonValue.getElement("books").getElements().next().getElements().next()
         .getElements().next().getElement("title").getStringValue());
   }

   public void testEnumSerialization() throws Exception
   {
      String source =
         "{\"countList\":[\"ONE\",\"TWO\",\"TREE\"], \"name\":\"andrew\",\"count\":\"TREE\",\"counts\":[\"TWO\",\"TREE\"]}";
      JsonParser jsonParser = new JsonParser();
      jsonParser.parse(new ByteArrayInputStream(source.getBytes()));
      JsonValue jsonValue = jsonParser.getJsonObject();
      //System.out.println(jsonValue);

      BeanWithSimpleEnum o = ObjectBuilder.createObject(BeanWithSimpleEnum.class, jsonValue);

      assertEquals("andrew", o.getName());

      assertEquals(StringEnum.TREE, o.getCount());

      StringEnum[] counts = o.getCounts();
      assertEquals(2, counts.length);

      List<StringEnum> tmp = Arrays.asList(counts);
      assertTrue(tmp.contains(StringEnum.TWO));
      assertTrue(tmp.contains(StringEnum.TREE));

      tmp = o.getCountList();
      assertEquals(3, tmp.size());
      assertTrue(tmp.contains(StringEnum.ONE));
      assertTrue(tmp.contains(StringEnum.TWO));
      assertTrue(tmp.contains(StringEnum.TREE));
   }

   public void testEnumSerialization2() throws Exception
   {
      String source = "{\"book\":\"BEGINNING_C\"}";
      JsonParser parser = new JsonParser();
      parser.parse(new ByteArrayInputStream(source.getBytes()));
      JsonValue jsonValue = parser.getJsonObject();
      //System.out.println(jsonValue);
      BeanWithBookEnum o = ObjectBuilder.createObject(BeanWithBookEnum.class, jsonValue);
      assertEquals(BookEnum.BEGINNING_C, o.getBook());
   }
}
