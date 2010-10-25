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
package org.everrest.core.impl.uri;

import org.everrest.core.impl.BaseTest;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class UriComponentTest extends BaseTest
{

   public void testCheckHexCharacters()
   {
      String str = "%20%23%a0%ag";
      assertTrue(UriComponent.checkHexCharacters(str, 0));
      assertFalse(UriComponent.checkHexCharacters(str, 1));
      assertTrue(UriComponent.checkHexCharacters(str, 3));
      assertTrue(UriComponent.checkHexCharacters(str, 6));
      assertFalse(UriComponent.checkHexCharacters(str, 9));
      assertFalse(UriComponent.checkHexCharacters(str, 11));
   }

   public void testEncodeDecode()
   {
      String str = "\u041f?\u0440#\u0438 \u0432\u0456\u0442";
      String estr = "%D0%9F%3F%D1%80%23%D0%B8%20%D0%B2%D1%96%D1%82";
      assertEquals(estr, UriComponent.encode(str, UriComponent.HOST, false));
      assertEquals(str, UriComponent.decode(estr, UriComponent.HOST));

      // wrong encoded string, near %9g
      String estr1 = "%D0%9g%3F%D1%80%23%D0%B8%20%D0%B2%D1%96%D1%82";
      try
      {
         UriComponent.decode(estr1, UriComponent.HOST);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      // wrong encoded string, end %8
      estr1 = "%D0%9F%3F%D1%80%23%D0%B8%20%D0%B2%D1%96%D1%8";
      try
      {
         UriComponent.decode(estr1, UriComponent.HOST);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
   }

   public void testParseQueryString()
   {
      String str = "q1=to%20be%20or%20not%20to%20be&q2=foo&q2=%D0%9F%D1%80%D0%B8%D0%B2%D1%96%D1%82";
      MultivaluedMap<String, String> m = UriComponent.parseQueryString(str, false);
      assertEquals(2, m.size());
      assertEquals(1, m.get("q1").size());
      assertEquals(2, m.get("q2").size());
      m = UriComponent.parseQueryString(str, true);
      assertEquals(2, m.size());
      assertEquals(1, m.get("q1").size());
      assertEquals(2, m.get("q2").size());
      assertEquals("to be or not to be", m.get("q1").get(0));
      assertEquals("foo", m.get("q2").get(0));
      assertEquals("\u041f\u0440\u0438\u0432\u0456\u0442", m.get("q2").get(1));
   }

   public void testParsePathSegment()
   {
      String path = "/to/be/or%20not/to/be;a=foo;b=b%20a%23r";
      List<PathSegment> segms = UriComponent.parsePathSegments(path, true);
      assertEquals(5, segms.size());
      assertEquals("to", segms.get(0).getPath());
      assertEquals("be", segms.get(1).getPath());
      assertEquals("or not", segms.get(2).getPath());
      assertEquals("to", segms.get(3).getPath());
      assertEquals("be", segms.get(4).getPath());
      assertEquals("foo", segms.get(4).getMatrixParameters().get("a").get(0));
      assertEquals("b a#r", segms.get(4).getMatrixParameters().get("b").get(0));
   }

   public void testRecognizeEncoding()
   {
      String str = "to be%23or not to%20be";
      // double encoding here, %23 -> %2523 and %20 -> %2520
      assertEquals("to%20be%2523or%20not%20to%2520be", UriComponent.encode(str, UriComponent.PATH_SEGMENT, false));
      // no double encoding here
      assertEquals("to%20be%23or%20not%20to%20be", UriComponent.recognizeEncode(str, UriComponent.PATH_SEGMENT, false));
   }

   public void testURINormalization() throws Exception
   {
      String[] testUris = {"http://localhost:8080/servlet/../1//2/3/./../../4", //
         "http://localhost:8080/servlet/./1//2/3/./../../4", //
         "http://localhost:8080/servlet/1//2/3/./../../4", //
         "http://localhost:8080/servlet/1//2./3/./../4", //
         "http://localhost:8080/servlet/1//.2/3/./../4", //
         "http://localhost:8080/servlet/1..//.2/3/./../4", //
         "http://localhost:8080/servlet/./1//2/3/./../../4", //
         "http://localhost:8080/servlet/.", //
         "http://localhost:8080/servlet/..", //
         "http://localhost:8080/servlet/1"};

      String[] normalizedUris = {"http://localhost:8080/1/4", //
         "http://localhost:8080/servlet/1/4", //
         "http://localhost:8080/servlet/1/4", //
         "http://localhost:8080/servlet/1/2./4", //
         "http://localhost:8080/servlet/1/.2/4", //
         "http://localhost:8080/servlet/1../.2/4", //
         "http://localhost:8080/servlet/1/4", //
         "http://localhost:8080/servlet/", //
         "http://localhost:8080/", //
         "http://localhost:8080/servlet/1"};

      for (int i = 0; i < testUris.length; i++)
      {
         URI requestUri = new URI(testUris[i]);
         assertEquals(normalizedUris[i], UriComponent.normalize(requestUri).toString());
      }
   }

}
