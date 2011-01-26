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
package org.everrest.core.impl.method;

import junit.framework.TestCase;

import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.method.TypeProducer;

import javax.ws.rs.core.MultivaluedMap;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class StringValueOfProducerTest extends TestCase
{

   public void testByte() throws Exception
   {
      StringValueOfProducer stringValueOfProducer =
         new StringValueOfProducer(Byte.class.getMethod("valueOf", String.class));
      assertEquals(Byte.valueOf("127"), stringValueOfProducer.createValue("127"));
   }

   public void testShort() throws Exception
   {
      StringValueOfProducer stringValueOfProducer =
         new StringValueOfProducer(Short.class.getMethod("valueOf", String.class));
      assertEquals(Short.valueOf("32767"), stringValueOfProducer.createValue("32767"));
   }

   public void testInteger() throws Exception
   {
      StringValueOfProducer stringValueOfProducer =
         new StringValueOfProducer(Integer.class.getMethod("valueOf", String.class));
      assertEquals(Integer.valueOf("2147483647"), stringValueOfProducer.createValue("2147483647"));
   }

   public void testLong() throws Exception
   {
      StringValueOfProducer stringValueOfProducer =
         new StringValueOfProducer(Long.class.getMethod("valueOf", String.class));
      assertEquals(Long.valueOf("9223372036854775807"), stringValueOfProducer.createValue("9223372036854775807"));
   }

   public void testFloat() throws Exception
   {
      StringValueOfProducer stringValueOfProducer =
         new StringValueOfProducer(Float.class.getMethod("valueOf", String.class));
      assertEquals(Float.valueOf("1.23456789"), stringValueOfProducer.createValue("1.23456789"));
   }

   public void testDouble() throws Exception
   {
      StringValueOfProducer stringValueOfProducer =
         new StringValueOfProducer(Double.class.getMethod("valueOf", String.class));
      assertEquals(Double.valueOf("1.234567898765432"), stringValueOfProducer.createValue("1.234567898765432"));
   }

   public void testBoolean() throws Exception
   {
      StringValueOfProducer stringValueOfProducer =
         new StringValueOfProducer(Boolean.class.getMethod("valueOf", String.class));
      assertEquals(Boolean.valueOf("true"), stringValueOfProducer.createValue("true"));
   }

   public void testCuctomTypeValueOf() throws Exception
   {
      TypeProducer t = ParameterHelper.createTypeProducer(StringValueOf.class, null);
      MultivaluedMap<String, String> values = new MultivaluedMapImpl();
      values.putSingle("key1", "valueof test");
      StringValueOf o1 = (StringValueOf)t.createValue("key1", values, null);
      assertEquals("valueof test", o1.getValue());
      values.clear();
      o1 = (StringValueOf)t.createValue("key1", values, "default value");
      assertEquals("default value", o1.getValue());
   }

   public static class StringValueOf
   {
      private String value;

      private StringValueOf(String value)
      {
         this.value = value;
      }

      public String getValue()
      {
         return value;
      }

      public static StringValueOf valueOf(String value)
      {
         return new StringValueOf(value);
      }
   }

}
