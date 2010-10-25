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
package org.everrest.core.impl.resource;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.MultivaluedMapImpl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class AcceptResourceTest extends BaseTest
{

   @Path("/a")
   public static class Resource1
   {
      @POST
      @Consumes({"text/*"})
      public String m0()
      {
         return "m0";
      }

      @POST
      @Consumes({"image/*"})
      public String m1()
      {
         return "m1";
      }

      @POST
      @Consumes({"text/xml", "application/xml"})
      public String m2()
      {
         return "m2";
      }

      @POST
      @Consumes({"image/jpeg", "image/png"})
      public String m3()
      {
         return "m3";
      }

      @POST
      public String m4()
      {
         return "m4";
      }
   }

   @Path("/a")
   public static class Resource2
   {
      @GET
      @Produces({"text/plain", "text/html"})
      public String m0()
      {
         return "m0";
      }

      @GET
      @Produces({"text/xml", "text/*"})
      public String m1()
      {
         return "m1";
      }

      @GET
      @Produces({"image/*"})
      public String m2()
      {
         return "m2";
      }

      @GET
      @Produces({"image/jpeg", "image/png"})
      public String m3()
      {
         return "m3";
      }

      @GET
      public String m4()
      {
         return "m4";
      }
   }

   @Path("/a")
   public static class Resource3
   {
      @POST
      @Consumes({"text/plain", "text/xml"})
      @Produces({"text/xml"})
      public String m0()
      {
         return "m0";
      }

      @POST
      @Consumes({"image/*", "image/png"})
      @Produces({"image/gif"})
      public String m1()
      {
         return "m1";
      }

      @POST
      @Consumes({"application/xml", "text/xml"})
      @Produces({"text/html"})
      public String m2()
      {
         return "m2";
      }

      @POST
      public String m3()
      {
         return "m3";
      }

   }

   @Path("/a")
   public static class Resource4
   {
      @POST
      @Consumes({"text/*+xml"})
      @Produces({"text/*+xml"})
      public String m0()
      {
         return "m0";
      }

      @POST
      @Consumes({"application/*+xml"})
      @Produces({"application/xhtml+xml"})
      public String m1()
      {
         return "m1";
      }

   }

   public void setUp() throws Exception
   {
      super.setUp();
   }

   public void testContentType() throws Exception
   {
      Resource1 r1 = new Resource1();
      registry(r1);
      assertEquals("m0", testContentType("text/html"));
      assertEquals("m2", testContentType("text/xml"));
      assertEquals("m2", testContentType("application/xml"));
      assertEquals("m1", testContentType("image/gif"));
      assertEquals("m3", testContentType("image/jpeg"));
      assertEquals("m3", testContentType("image/png"));
      assertEquals("m4", testContentType("application/x-www-form-urlencoded"));
      unregistry(r1);
   }

   public void testAcceptedMediaType() throws Exception
   {
      Resource2 r2 = new Resource2();
      registry(r2);
      assertEquals("m0", testAcceptedMediaType("text/plain;q=0.9,text/html;q=0.7,text/*;q=0.5"));
      assertEquals("m0", testAcceptedMediaType("text/plain;q=0.7,text/html;q=0.9,text/*;q=0.5"));
      assertEquals("m0", testAcceptedMediaType("text/plain;q=0.5,text/html;q=0.7,text/*;q=0.9"));

      assertEquals("m1", testAcceptedMediaType("text/xml;q=0.9,text/bell;q=0.5"));
      assertEquals("m1", testAcceptedMediaType("text/foo"));
      assertEquals("m2", testAcceptedMediaType("image/gif"));

      assertEquals("m3", testAcceptedMediaType("image/jpeg;q=0.8,  image/png;q=0.9"));
      assertEquals("m3", testAcceptedMediaType("image/foo;q=0.8,  image/png;q=0.9"));
      assertEquals("m2", testAcceptedMediaType("image/foo;q=0.9,  image/png;q=0.8"));

      assertEquals("m2", testAcceptedMediaType("image/foo;q=0.9,  image/gif;q=0.8"));

      assertEquals("m4", testAcceptedMediaType("application/x-www-form-urlencoded"));
      assertEquals("m0", testAcceptedMediaType("application/x-www-form-urlencoded;q=0.5,text/plain"));
      unregistry(r2);
   }

   public void testComplex() throws Exception
   {
      Resource3 r3 = new Resource3();
      registry(r3);
      assertEquals("m3", testComplex("text/plain", "text/plain;q=0.9"));
      assertEquals("m0", testComplex("text/plain", "text/plain;q=0.3,text/xml;q=0.9"));
      assertEquals("m3", testComplex("text/xml", "text/plain;q=0.9,text/html;q=0.3"));
      assertEquals("m0", testComplex("text/xml", "text/xml,text/*;q=0.3"));
      assertEquals("m1", testComplex("image/*", "image/*"));
      assertEquals("m3", testComplex("image/*", "image/png"));
      assertEquals("m3", testComplex("image/*", "image/png,image/gif;q=0.1"));
      assertEquals("m1", testComplex("image/*", "image/*,image/gif;q=0.1"));
      assertEquals("m3", testComplex("foo/bar", "foo/bar"));

      unregistry(r3);
   }

   private String testContentType(String contentType) throws Exception
   {
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      h.putSingle("content-type", contentType);
      return (String)launcher.service("POST", "/a", "", h, null, null).getEntity();
   }

   private String testAcceptedMediaType(String acceptMediaType) throws Exception
   {
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      h.putSingle("accept", acceptMediaType);
      return (String)launcher.service("GET", "/a", "", h, null, null).getEntity();
   }

   private String testComplex(String contentType, String acceptMediaType) throws Exception
   {
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      h.putSingle("content-type", contentType);
      h.putSingle("accept", acceptMediaType);
      return (String)launcher.service("POST", "/a", "", h, null, null).getEntity();
   }
   
   public void testExtSubtype() throws Exception
   {
      registry(Resource4.class);
      assertEquals("m0", testComplex("text/xml", "text/xml,text/*+xml;q=.8"));
      unregistry(Resource4.class);
   }

   public void testExtSubtype2() throws Exception
   {
      registry(Resource4.class);
      assertEquals("m1", testComplex("application/atom+xml", "application/xhtml+xml"));
      unregistry(Resource4.class);
   }
}
