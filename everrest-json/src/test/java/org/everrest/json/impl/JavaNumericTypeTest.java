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

import org.everrest.json.JsonHandler;
import org.everrest.json.value.JsonValue;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: JavaNumericTypeTest.java 34417 2009-07-23 14:42:56Z dkatayev $
 */
public class JavaNumericTypeTest extends TestCase
{

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
   }

   public void testLong() throws Exception
   {
      JsonParserImpl jsonParser = new JsonParserImpl();
      JsonHandler jsonHandler = new JsonDefaultHandler();
      String jsonString =
         "{" + "\"long\":[" + "1, 0xAA, 077, 123, 32765, 77787, 123456789," + "0x123456, 0123456, -2387648, -123456789"
            + "]" + "}";

      jsonParser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())), jsonHandler);
      JsonValue jv = jsonHandler.getJsonObject();
      assertTrue(jv.getElement("long").isArray());
      Iterator<JsonValue> values = jv.getElement("long").getElements();
      int i = 0;
      while (values.hasNext())
      {
         JsonValue v = values.next();
         assertTrue(v.isNumeric());
         assertTrue(v.isLong());
         assertFalse(v.isDouble());
         if (i == 0)
            assertEquals(1L, v.getLongValue());
         if (i == 3)
            assertEquals(123L, v.getLongValue());
         if (i == 6)
            assertEquals(123456789L, v.getLongValue());
         i++;
      }
   }

   public void testDouble() throws Exception
   {
      JsonParserImpl jsonParser = new JsonParserImpl();
      JsonHandler jsonHandler = new JsonDefaultHandler();
      String jsonString =
         "{" + "\"double\":[" + "1.0, 0.0006382746, 111111.2222222, 9999999999999.9999999999999,"
            + "9827394873249.8, 1.23456789E8, 123456.789E8, 3215478352478651238.0,"
            + "982.8, 0.00000000000023456789E8, 1.789E8, 0.0000000000000000000321547835247865123,"
            + "982.8, -0.00000000000023456789E8, -1.789E-8, -0.0000000000000000000321547835247865123" + "]" + "}";

      jsonParser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())), jsonHandler);
      JsonValue jv = jsonHandler.getJsonObject();
      assertTrue(jv.getElement("double").isArray());
      Iterator<JsonValue> values = jv.getElement("double").getElements();
      int i = 0;
      while (values.hasNext())
      {
         JsonValue v = values.next();
         assertTrue(v.isNumeric());
         assertFalse(v.isLong());
         assertTrue(v.isDouble());
         if (i == 0)
            assertEquals(1.0, v.getDoubleValue());
         if (i == 2)
            assertEquals(111111.2222222, v.getDoubleValue());
         if (i == 9)
            assertEquals(0.00000000000023456789E8, v.getDoubleValue());
         i++;
      }
   }

}
