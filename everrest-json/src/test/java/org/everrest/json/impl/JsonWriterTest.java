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

import java.io.ByteArrayOutputStream;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: JsonWriterTest.java 34417 2009-07-23 14:42:56Z dkatayev $
 */
public class JsonWriterTest extends TestCase
{

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
   }

   public void testJSONWriter() throws Exception
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      JsonWriterImpl jsw = new JsonWriterImpl(out);
      String key = "key";
      String value = "value";

      jsw.writeStartObject();
      jsw.writeKey(key + "_top");
      jsw.writeStartObject();
      for (int i = 0; i <= 5; i++)
      {
         jsw.writeKey(key + i);
         jsw.writeString(value + i);
      }
      jsw.writeKey(key + "_inner_top");
      jsw.writeStartObject();
      jsw.writeKey(key + "_string");
      jsw.writeString("string");
      jsw.writeKey(key + "_null");
      jsw.writeNull();
      jsw.writeKey(key + "_boolean");
      jsw.writeValue(true);
      jsw.writeKey(key + "_long");
      jsw.writeValue(121);
      jsw.writeKey(key + "_double");
      jsw.writeValue(121.121);
      jsw.writeEndObject();
      jsw.writeEndObject();
      jsw.writeKey(key + "_array");
      jsw.writeStartArray();
      for (int i = 0; i <= 5; i++)
         jsw.writeString(value + i);
      //    try {
      //      jsw.writeEndObject();
      //      fail("JsonException should be here.");
      //    } catch(JsonException e) {}
      jsw.writeEndArray();
      jsw.writeEndObject();
      jsw.flush();
      jsw.close();
      System.out.println(new String(out.toByteArray()));
   }

}
