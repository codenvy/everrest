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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class JsonParserTest extends JsonTest
{

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
   }

   public void testArrayString() throws Exception
   {
      JsonParser jsonParser = new JsonParser();
      String jsonString = "[\"JUnit in Action\",\"Advanced JavaScript\",\"Beginning C# 2008\"]";
      jsonParser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));
      JsonValue jsonValue = jsonParser.getJsonObject();
      assertTrue(jsonValue.isArray());
      Set<String> s = new HashSet<String>();
      for (Iterator<JsonValue> elements = jsonValue.getElements(); elements.hasNext();)
      {
         JsonValue next = elements.next();
         assertTrue(next.isString());
         s.add(next.getStringValue());
      }
      assertEquals(3, s.size());
      assertTrue(s.contains("JUnit in Action"));
      assertTrue(s.contains("Advanced JavaScript"));
      assertTrue(s.contains("Beginning C# 2008"));
   }

   public void testArrayLong() throws Exception
   {
      JsonParser jsonParser = new JsonParser();
      String jsonString = "[1, 0xAA, 077, 123, 32765, 77787, 123456789, 0x123456, 0123456, -2387648, -123456789]";

      jsonParser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));
      JsonValue jsonValue = jsonParser.getJsonObject();
      assertTrue(jsonValue.isArray());
      int i = 0;
      for (Iterator<JsonValue> elements = jsonValue.getElements(); elements.hasNext(); i++)
      {
         JsonValue next = elements.next();
         assertTrue(next.isNumeric());
         assertTrue(next.isLong());
         assertFalse(next.isDouble());
         if (i == 0)
            assertEquals(1L, next.getLongValue());
         if (i == 3)
            assertEquals(123L, next.getLongValue());
         if (i == 6)
            assertEquals(123456789L, next.getLongValue());
      }
   }

   public void testArrayDouble() throws Exception
   {
      JsonParser jsonParser = new JsonParser();
      String jsonString =
         "[1.0, 0.0006382746, 111111.2222222, 9999999999999.9999999999999,"
            + "9827394873249.8, 1.23456789E8, 123456.789E8, 3215478352478651238.0,"
            + "982.8, 0.00000000000023456789E8, 1.789E8, 0.0000000000000000000321547835247865123,"
            + "982.8, -0.00000000000023456789E8, -1.789E-8, -0.0000000000000000000321547835247865123]";

      jsonParser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));
      JsonValue jsonValue = jsonParser.getJsonObject();
      assertTrue(jsonValue.isArray());
      int i = 0;
      for (Iterator<JsonValue> elements = jsonValue.getElements(); elements.hasNext(); i++)
      {
         JsonValue next = elements.next();
         assertTrue(next.isNumeric());
         assertFalse(next.isLong());
         assertTrue(next.isDouble());
         if (i == 0)
            assertEquals(1.0, next.getDoubleValue());
         if (i == 2)
            assertEquals(111111.2222222, next.getDoubleValue());
         if (i == 9)
            assertEquals(0.00000000000023456789E8, next.getDoubleValue());
      }
   }

   public void testArrayMixed() throws Exception
   {
      JsonParser jsonParser = new JsonParser();
      String jsonString = "[1.0, \"to be or not to be\", 111, true, {\"object\":{\"foo\":\"bar\"}}]";

      jsonParser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));
      JsonValue jsonValue = jsonParser.getJsonObject();
      assertTrue(jsonValue.isArray());

      ArrayValue exp = new ArrayValue();
      exp.addElement(new DoubleValue(1.0D));
      exp.addElement(new StringValue("to be or not to be"));
      exp.addElement(new LongValue(111));
      exp.addElement(new BooleanValue(true));
      ObjectValue o = new ObjectValue();
      o.addElement("foo", new StringValue("bar"));
      ObjectValue o1 = new ObjectValue();
      o1.addElement("object", o);
      exp.addElement(o1);

      Iterator<JsonValue> elements = jsonValue.getElements();
      Iterator<JsonValue> expElements = jsonValue.getElements();
      for (; elements.hasNext() && expElements.hasNext();)
      {
         JsonValue next = elements.next();
         JsonValue expNext = expElements.next();
         assertEquals(expNext.toString(), next.toString());
      }
      // Both must be empty 
      assertFalse(elements.hasNext() || expElements.hasNext());
   }

   public void testObject() throws Exception
   {
      JsonParser jsonParser = new JsonParser();
      String jsonString =
         "{\"foo\":\"bar\", \"book\":{\"author\":\"Christian Gross\",\"title\":\"Beginning C# 2008\"}}";
      jsonParser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));
      JsonValue jsonValue = jsonParser.getJsonObject();
      assertTrue(jsonValue.isObject());

      JsonValue sValue = jsonValue.getElement("foo");
      assertTrue(sValue.isString());
      assertEquals("bar", sValue.getStringValue());

      JsonValue bookValue = jsonValue.getElement("book");
      assertTrue(bookValue.isObject());
      assertEquals("Beginning C# 2008", bookValue.getElement("title").getStringValue());
      assertEquals("Christian Gross", bookValue.getElement("author").getStringValue());
   }

   public void testMultiDimensionArray() throws Exception
   {
      String jsonString = "[\"foo0\", [\"foo1\", \"bar1\", [\"foo2\", \"bar2\"]], \"bar0\"]";

      JsonParser jsonParser = new JsonParser();
      jsonParser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));
      JsonValue jsonValue = jsonParser.getJsonObject();
      //System.out.println(jsonValue);
      
      ArrayValue exp = new ArrayValue();
      exp.addElement(new StringValue("foo0"));
      ArrayValue l1 = new ArrayValue();
      exp.addElement(l1);
      l1.addElement(new StringValue("foo1"));
      l1.addElement(new StringValue("bar1"));
      ArrayValue l2 = new ArrayValue();
      l1.addElement(l2);
      l2.addElement(new StringValue("foo2"));
      l2.addElement(new StringValue("bar2"));
      exp.addElement(new StringValue("bar0"));

      assertEquals(exp.toString(), jsonValue.toString());
   }

}
