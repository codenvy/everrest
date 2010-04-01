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

import org.everrest.core.Property;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.RequestHandlerImpl;
import org.everrest.core.tools.ResourceLauncher;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class ParametersInjectionTest extends BaseTest
{

   @Path("/a/{x}")
   public static class Resource1
   {

      @GET
      @Path("/0/{y}/")
      public void m0(@PathParam("x") String x, @PathParam("y") String y)
      {
         assertNotNull(x);
         assertNotNull(y);
         assertEquals(x, y);
      }

      @GET
      @Path("/1/{y}/")
      public void m1(@PathParam("x") List<String> x, @PathParam("y") List<String> y)
      {
         assertNotNull(x);
         assertEquals(1, x.size());
         assertNotNull(y);
         assertEquals(1, y.size());
         assertEquals(x, y);
      }

      @GET
      @Path("/2/{y}/")
      public void m2(@PathParam("x") Set<String> x, @PathParam("y") Set<String> y)
      {
         assertNotNull(x);
         assertEquals(1, x.size());
         assertNotNull(y);
         assertEquals(1, y.size());
         assertEquals(x, y);
      }

      @GET
      @Path("/3/{y}/")
      public void m3(@PathParam("x") SortedSet<String> x, @PathParam("y") SortedSet<String> y)
      {
         assertNotNull(x);
         assertEquals(1, x.size());
         assertNotNull(y);
         assertEquals(1, y.size());
         assertEquals(x, y);
      }

      @GET
      @Path("/4/{y}/")
      public void m4(@PathParam("x") Integer x, @PathParam("y") Integer y)
      {
         assertNotNull(x);
         assertNotNull(y);
         assertEquals(1111, x - y);
      }

      @GET
      @Path("/5/{y}/")
      public void m5(@PathParam("x") long x, @PathParam("y") long y)
      {
         assertNotNull(x);
         assertNotNull(y);
         assertEquals(1111, x - y);
      }

      @GET
      @Path("/6/{y}/")
      public void m6(@PathParam("x") Test x, @PathParam("y") Test y)
      {
         assertNotNull(x);
         assertNotNull(y);
         assertEquals(x.toString(), y.toString());
      }

      @GET
      @Path("/7/{y}/")
      public void m7(@QueryParam("x") List<String> x, @QueryParam("y") List<String> y)
      {
         assertNotNull(x);
         assertEquals(3, x.size());
         assertNotNull(y);
         assertEquals(2, y.size());
         assertEquals("1", x.get(0));
         assertEquals("3", x.get(1));
         assertEquals("5", x.get(2));
         assertEquals("2", y.get(0));
         assertEquals("4", y.get(1));
      }

      @GET
      @Path("/8/{y}/")
      public void m8(@HeaderParam("foo") String x, @HeaderParam("bar") String y)
      {
         assertNotNull(x);
         assertNotNull(y);
         assertEquals(x, y);
      }

      @POST
      @Path("/9/{y}/")
      public void m9(@FormParam("foo") String x, @FormParam("bar") String y)
      {
         assertNotNull(x);
         assertNotNull(y);
         assertEquals(x, y);
      }

      @GET
      @Path("/10/{y}/")
      public void m10(@MatrixParam("foo") int x, @MatrixParam("bar") int y)
      {
         assertNotNull(x);
         assertNotNull(y);
         assertEquals(1111, x - y);
      }

      @GET
      @Path("/11/{y}/")
      public void m11(@CookieParam("foo") Cookie x, @CookieParam("bar") Cookie y)
      {
         assertNotNull(x);
         assertNotNull(y);
         assertEquals(x.getDomain(), y.getDomain());
         assertEquals(x.getPath(), y.getPath());
      }

      @GET
      @Path("/12/{y}/")
      public void m12(@Context UriInfo uriInfo)
      {
         assertNotNull(uriInfo);
         assertEquals(2, uriInfo.getPathParameters().size());
      }

      @GET
      @Path("/13")
      public String m13(@QueryParam("query") @DefaultValue("111") String param)
      {
         assertNotNull(param);

         return param;
      }

      @GET
      @Path("/14")
      public String m14(@Property("prop1") @DefaultValue("hello") String prop)
      {

         assertNotNull(prop);

         return prop;
      }

   }

   public static class Test
   {
      private final String s;

      public Test(String s)
      {
         this.s = s;
      }

      public String toString()
      {
         return s;
      }
   }

   private ResourceLauncher launcher;

   public void setUp() throws Exception
   {
      super.setUp();
      this.launcher = new ResourceLauncher(requestHandler);
   }

   public void testParameterTypes() throws Exception
   {
      Resource1 r1 = new Resource1();
      registry(r1);
      assertEquals(204, launcher.service("GET", "/a/test/0/test", "", null, null, null).getStatus());
      assertEquals(204, launcher.service("GET", "/a/test/1/test", "", null, null, null).getStatus());
      assertEquals(204, launcher.service("GET", "/a/test/2/test", "", null, null, null).getStatus());
      assertEquals(204, launcher.service("GET", "/a/test/3/test", "", null, null, null).getStatus());
      assertEquals(204, launcher.service("GET", "/a/3333/4/2222", "", null, null, null).getStatus());
      assertEquals(204, launcher.service("GET", "/a/5555/5/4444", "", null, null, null).getStatus());
      assertEquals(204, launcher.service("GET", "/a/test/6/test", "", null, null, null).getStatus());
      assertEquals(204, launcher.service("GET", "/a/test/7/test?x=1&y=2&x=3&y=4&x=5", "", null, null, null).getStatus());

      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      h.putSingle("foo", "to be or not to be");
      h.putSingle("bar", "to be or not to be");
      assertEquals(204, launcher.service("GET", "/a/test/8/test", "", h, null, null).getStatus());

      h.clear();
      h.putSingle("Content-Type", "application/x-www-form-urlencoded");
      assertEquals(204, launcher.service("POST", "/a/test/9/test", "", h,
         "bar=to%20be%20or%20not%20to%20be&foo=to%20be%20or%20not%20to%20be".getBytes("UTF-8"), null).getStatus());

      h.clear();
      h.putSingle("Cookie",
         "$Version=1;foo=foo;$Domain=exo.com;$Path=/exo,$Version=1;bar=ar;$Domain=exo.com;$Path=/exo");
      assertEquals(204, launcher.service("GET", "/a/test/11/test", "", h, null, null).getStatus());

      assertEquals(204, launcher.service("GET", "/a/111/12/222", "", null, null, null).getStatus());

      assertEquals("111", launcher.service("GET", "/a/111/13", "", null, null, null).getEntity());
      assertEquals("222", launcher.service("GET", "/a/111/13?query=222", "", null, null, null).getEntity());

      try
      {
         assertEquals("hello", launcher.service("GET", "/a/111/14", "", null, null, null).getEntity());
         RequestHandlerImpl.setProperty("prop1", "to be or not to be");
         assertEquals("to be or not to be", launcher.service("GET", "/a/111/14", "", null, null, null).getEntity());
      }
      finally
      {
         RequestHandlerImpl.setProperty("prop1", null);
      }

      unregistry(r1);
   }

}
