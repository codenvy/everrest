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
package org.everrest.core.impl.header;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.header.HeaderParameterParser;

import java.text.ParseException;
import java.util.Map;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class HeaderParameterParserTest extends BaseTest
{

   public void testSimple() throws ParseException
   {
      HeaderParameterParser hp = new HeaderParameterParser();
      String src = "text/plain;foo=bar";
      Map<String, String> m = hp.parse(src);
      assertEquals("bar", m.get("foo"));
      src = "text/plain;foo=\"bar\"";
      m = hp.parse(src);
      assertEquals("bar", m.get("foo"));
   }

   public void testQuoted() throws ParseException
   {
      HeaderParameterParser hp = new HeaderParameterParser();
      String src = "text/plain;foo=\"\\\"he\\\";llo\\\"\"   ;  ba r  =  f o o       ; foo2";
      Map<String, String> m = hp.parse(src);
      assertEquals(3, m.size());
      assertEquals("\"he\";llo\"", m.get("foo"));
      assertEquals("f o o", m.get("ba r"));
      assertNull(m.get("foo2"));

      src = "text/plain;bar=\"foo\" \t; bar2; test=\"\\a\\b\\c\\\"\"   ;  foo=bar";
      m = hp.parse(src);
      assertEquals(4, m.size());
      assertEquals("foo", m.get("bar"));
      assertEquals("\\a\\b\\c\"", m.get("test"));
      assertEquals("bar", m.get("foo"));
      assertNull(m.get("bar2"));
   }

}
