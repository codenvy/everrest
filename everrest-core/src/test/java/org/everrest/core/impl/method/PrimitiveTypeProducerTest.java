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

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class PrimitiveTypeProducerTest extends TestCase
{

   public void testByte() throws Exception
   {
      PrimitiveTypeProducer primitiveTypeProducer = new PrimitiveTypeProducer(Byte.TYPE);
      assertEquals((byte)127, primitiveTypeProducer.createValue("127"));
   }

   public void testShort() throws Exception
   {
      PrimitiveTypeProducer primitiveTypeProducer = new PrimitiveTypeProducer(Short.TYPE);
      assertEquals((short)32767, primitiveTypeProducer.createValue("32767"));
   }

   public void testInt() throws Exception
   {
      PrimitiveTypeProducer primitiveTypeProducer = new PrimitiveTypeProducer(Integer.TYPE);
      assertEquals(2147483647, primitiveTypeProducer.createValue("2147483647"));
   }

   public void testLong() throws Exception
   {
      PrimitiveTypeProducer primitiveTypeProducer = new PrimitiveTypeProducer(Long.TYPE);
      assertEquals(9223372036854775807L, primitiveTypeProducer.createValue("9223372036854775807"));
   }

   public void testFloat() throws Exception
   {
      PrimitiveTypeProducer primitiveTypeProducer = new PrimitiveTypeProducer(Float.TYPE);
      assertEquals(1.23456789F, primitiveTypeProducer.createValue("1.23456789"));
   }

   public void testDouble() throws Exception
   {
      PrimitiveTypeProducer primitiveTypeProducer = new PrimitiveTypeProducer(Double.TYPE);
      assertEquals(1.234567898765432D, primitiveTypeProducer.createValue("1.234567898765432"));
   }

   public void testBoolean() throws Exception
   {
      PrimitiveTypeProducer primitiveTypeProducer = new PrimitiveTypeProducer(Boolean.TYPE);
      assertEquals(true, primitiveTypeProducer.createValue("true"));
   }

}
