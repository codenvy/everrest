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
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class UriBuilderTest extends BaseTest
{

   public void testReplaceScheme()
   {
      URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c").scheme("https").build();
      assertEquals(URI.create("https://localhost:8080/a/b/c"), u);
   }

   public void testReplaceSchemeSpecificPart()
   {
      URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c").schemeSpecificPart("//localhost:8080/a/b/c/d").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c/d"), u);
   }

   public void testReplaceUserInfo()
   {
      URI u = UriBuilder.fromUri("http://exo@localhost:8080/a/b/c").userInfo("andrew").build();
      assertEquals(URI.create("http://andrew@localhost:8080/a/b/c"), u);
   }

   public void testReplaceHost()
   {
      URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c").host("exoplatform.org").build();
      assertEquals(URI.create("http://exoplatform.org:8080/a/b/c"), u);
      u = UriBuilder.fromUri("http://localhost:8080/a/b/c").host("te st.org").build();
      assertEquals(URI.create("http://te%20st.org:8080/a/b/c"), u);
   }

   public void testReplacePort()
   {
      URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c").port(9000).build();
      assertEquals(URI.create("http://localhost:9000/a/b/c"), u);

      u = UriBuilder.fromUri("http://localhost:8080/a/b/c").port(-1).build();
      assertEquals(URI.create("http://localhost/a/b/c"), u);
   }

   public void testReplacePath()
   {
      URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c").replacePath("/x/y/z").build();
      assertEquals(URI.create("http://localhost:8080/x/y/z"), u);
   }

   public void testReplaceMatrixParam()
   {
      URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c;a=x;b=y;a=z").replaceMatrixParam("a", "b", "c").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c;b=y;a=b;a=c"), u);
      u = UriBuilder.fromUri("http://localhost:8080/a/b/c;y=b;a=x;b=y;a=z").replaceMatrixParam("a", "b", "c").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c;y=b;b=y;a=b;a=c"), u);
   }

   public void testReplaceMatrixParams()
   {
      URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c;a=x;b=y").replaceMatrix("x=a;y=b").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c;x=a;y=b"), u);
   }

   public void testReplaceQueryParam()
   {
      URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y&a=z").replaceQueryParam("a", "b", "c").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c?b=y&a=b&a=c"), u);
      u = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&a=z&b=y").replaceQueryParam("a", "b", "c").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c?b=y&a=b&a=c"), u);
      u =
         UriBuilder.fromUri("http://localhost:8080/a/b/c?b=y&a=x&y=b&a=z").replaceQueryParam("a", "b%20", "c%").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c?b=y&y=b&a=b%20&a=c%25"), u);

   }

   public void testReplaceQuery()
   {
      URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").replaceQuery("x=a&y=b&zzz=").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c?x=a&y=b&zzz"), u);
      u = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").replaceQuery("x=a&zzz=&y=b").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c?x=a&zzz&y=b"), u);
      try
      {
         u = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").replaceQuery("x=a&=zzz&y=b").build();
         fail("UriBuilderException should be here");
      }
      catch (UriBuilderException e)
      {
      }
   }

   public void testReplaceFragment()
   {
      URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y#hi").fragment("hel lo").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c?a=x&b=y#hel%20lo"), u);
   }

   public void testReplaceUri()
   {
      URI u0 = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");

      URI u = UriBuilder.fromUri(u0).uri(URI.create("https://exo@localhost:8080")).build();
      assertEquals(URI.create("https://exo@localhost:8080/a/b/c?a=x&b=y#fragment"), u);

      u = UriBuilder.fromUri(u0).uri(URI.create("http://andrew@localhost:9000")).build();
      assertEquals(URI.create("http://andrew@localhost:9000/a/b/c?a=x&b=y#fragment"), u);

      u = UriBuilder.fromUri(u0).uri(URI.create("/x/y/z")).build();
      assertEquals(URI.create("http://exo@localhost:8080/x/y/z?a=x&b=y#fragment"), u);

      u = UriBuilder.fromUri(u0).uri(URI.create("?x=a&b=y")).build();
      assertEquals(URI.create("http://exo@localhost:8080/a/b/c?x=a&b=y#fragment"), u);

      u = UriBuilder.fromUri(u0).uri(URI.create("#fragment2")).build();
      assertEquals(URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment2"), u);
   }

   public void testSchemeSpecificPart()
   {
      URI u = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");

      URI u2 = UriBuilder.fromUri(u).schemeSpecificPart("//andrew@exoplatform.org:9000/x/y/z?x=a&y=b").build();
      assertEquals(URI.create("http://andrew@exoplatform.org:9000/x/y/z?x=a&y=b#fragment"), u2);

      u = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");

      u2 = UriBuilder.fromUri(u).schemeSpecificPart("//andrew@exoplatform.org:9000/x /y/z?x= a&y=b").build();
      assertEquals(URI.create("http://andrew@exoplatform.org:9000/x%20/y/z?x=%20a&y=b#fragment"), u2);
   }

   public void testAppendPath()
   {
      URI u = UriBuilder.fromUri("http://localhost:8080").path("a/b/c").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c"), u);

      u = UriBuilder.fromUri("http://localhost:8080/").path("a/b/c").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c"), u);

      u = UriBuilder.fromUri("http://localhost:8080").path("/a/b/c").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c"), u);

      u = UriBuilder.fromUri("http://localhost:8080/").path("/a/b/c").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c"), u);

      u = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("/").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c/"), u);

      u = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c/"), u);

      u = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("/x/y/z").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), u);

      u = UriBuilder.fromUri("http://localhost:8080/a/b/c%20").path("/x/y /z").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c%20/x/y%20/z"), u);
   }

   public void testAppendSegments()
   {
      URI u = UriBuilder.fromUri("http://localhost:8080").segment("a/b/c").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c"), u);

      u = UriBuilder.fromUri("http://localhost:8080/").path("a/b/c").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c"), u);

      u = UriBuilder.fromUri("http://localhost:8080").path("/a/b/c").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c"), u);

      u = UriBuilder.fromUri("http://localhost:8080/").path("/a/b/c").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c"), u);

      u = UriBuilder.fromUri("http://localhost:8080").segment("a/b/c", "/x/y/z").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), u);

      u = UriBuilder.fromUri("http://localhost:8080/").segment("a/b/c/", "x/y/z").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), u);

      u = UriBuilder.fromUri("http://localhost:8080").segment("/a/b/c/", "/x/y/z").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), u);

      u = UriBuilder.fromUri("http://localhost:8080/").segment("/a/b/c", "x/y/z").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), u);
   }

   public void testAppendQueryParams()
   {
      URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").queryParam("c ", "%25z").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c?a=x&b=y&c%20=%25z"), u);
   }

   public void testAppendMatrixParams()
   {
      URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c;a=x;b=y").matrixParam(" c", "%z").build();
      assertEquals(URI.create("http://localhost:8080/a/b/c;a=x;b=y;%20c=%25z"), u);
   }

   public void testAppendPathAndMatrixParams()
   {
      URI u =
         UriBuilder.fromUri("http://localhost:8080/").path("a").matrixParam("x", " foo").matrixParam("y", "%20bar")
            .path("b").matrixParam("x", "f o%20o").build();
      assertEquals(URI.create("http://localhost:8080/a;x=%20foo;y=%20bar/b;x=f%20o%20o"), u);
   }

   @Path("resource")
   class R
   {
      @Path("method1")
      public void get()
      {
      }
   }

   public void testResourceAppendPath() throws NoSuchMethodException
   {
      URI u = UriBuilder.fromUri("http://localhost:8080/base").path(R.class).build();
      assertEquals(URI.create("http://localhost:8080/base/resource"), u);

      u = UriBuilder.fromUri("http://localhost:8080/base").path(R.class.getMethod("get")).build();
      assertEquals(URI.create("http://localhost:8080/base/method1"), u);
   }

   public void testResourceAndMethodAppendPath()
   {
      URI u = UriBuilder.fromUri("http://localhost:8080/base").path(R.class, "get").build();
      assertEquals(URI.create("http://localhost:8080/base/resource/method1"), u);
   }

   public void testTemplates()
   {
      URI u =
         UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/{baz}/{foo}").build("%25x", "%y", "z",
            "wrong");
      assertEquals(URI.create("http://localhost:8080/a/b/c/%2525x/%25y/z/%2525x"), u);

      Map<String, Object> m = new HashMap<String, Object>();
      m.put("foo", "%25x");
      m.put("bar", "%y");
      m.put("baz", "z");
      u = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/{baz}/{foo}").buildFromMap(m);
      assertEquals(URI.create("http://localhost:8080/a/b/c/%2525x/%25y/z/%2525x"), u);

      u =
         UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/{baz}/{foo}").buildFromEncoded("%25x",
            "%y", "z", "wrong");
      assertEquals(URI.create("http://localhost:8080/a/b/c/%25x/%25y/z/%25x"), u);

      m = new HashMap<String, Object>();
      m.put("foo", "%25x");
      m.put("bar", "%y");
      m.put("baz", "z");
      u = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/{baz}/{foo}").buildFromEncodedMap(m);
      assertEquals(URI.create("http://localhost:8080/a/b/c/%25x/%25y/z/%25x"), u);
   }

   public void testClone()
   {
      UriBuilder u = UriBuilder.fromUri("http://user@localhost:8080/?query#fragment").path("a");
      URI full = u.clone().path("b").build();
      URI base = u.build();

      assertEquals(URI.create("http://user@localhost:8080/a?query#fragment"), base);
      assertEquals(URI.create("http://user@localhost:8080/a/b?query#fragment"), full);
   }

}
