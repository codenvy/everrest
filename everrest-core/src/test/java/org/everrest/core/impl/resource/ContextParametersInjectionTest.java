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

import org.everrest.core.InitialProperties;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.header.HeaderHelper;
import org.everrest.core.tools.ResourceLauncher;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ContextParametersInjectionTest.java 497 2009-11-08 13:19:25Z
 *          aparfonov $
 */
public class ContextParametersInjectionTest extends BaseTest
{

   private ResourceLauncher launcher;

   public void setUp() throws Exception
   {
      super.setUp();
      this.launcher = new ResourceLauncher(requestHandler);
   }

   @Path("/a/b")
   public static class Resource1
   {

      @GET
      @Path("c")
      public String m0(@Context UriInfo uriInfo)
      {
         return uriInfo.getRequestUri().toString();
      }

      @GET
      @Path("d")
      public String m1(@Context HttpHeaders headers)
      {
         List<String> l = headers.getRequestHeader("Accept");
         return HeaderHelper.convertToString(l);
      }

      @GET
      @Path("e")
      public String m2(@Context Request request)
      {
         return request.getMethod();
      }

      @GET
      @Path("f")
      public void m3(@Context Providers providers)
      {
         assertNotNull(providers);
      }

      @GET
      @Path("g")
      public void m4(@Context InitialProperties properties)
      {
         assertNotNull(properties);
      }
   }

   public void testMethodContextInjection() throws Exception
   {
      Resource1 r1 = new Resource1();
      registry(r1);
      injectionTest();
      unregistry(r1);
   }

   //--------------------

   @Path("/a/b")
   public static class Resource2
   {

      @Context
      private UriInfo uriInfo;

      @Context
      private HttpHeaders headers;

      @Context
      private Request request;

      @Context
      private Providers providers;

      @Context
      private InitialProperties properties;

      @GET
      @Path("c")
      public String m0()
      {
         return uriInfo.getRequestUri().toString();
      }

      @GET
      @Path("d")
      public String m1()
      {
         List<String> l = headers.getRequestHeader("Accept");
         return HeaderHelper.convertToString(l);
      }

      @GET
      @Path("e")
      public String m2()
      {
         return request.getMethod();
      }

      @GET
      @Path("f")
      public void m3()
      {
         assertNotNull(providers);
      }

      @GET
      @Path("g")
      public void m4()
      {
         assertNotNull(properties);
      }

   }

   public void testFieldInjection() throws Exception
   {
      registry(Resource2.class);
      injectionTest();
      unregistry(Resource2.class);
   }

   //--------------------

   @Path("/a/b")
   public static class Resource3
   {

      private UriInfo uriInfo;

      private HttpHeaders headers;

      private Request request;

      private Providers providers;

      private InitialProperties properties;

      public Resource3(@Context UriInfo uriInfo, @Context HttpHeaders headers, @Context Request request,
         @Context Providers providers, @Context InitialProperties properties)
      {
         this.uriInfo = uriInfo;
         this.headers = headers;
         this.request = request;
         this.providers = providers;
         this.properties = properties;
      }

      @GET
      @Path("c")
      public String m0()
      {
         return uriInfo.getRequestUri().toString();
      }

      @GET
      @Path("d")
      public String m1()
      {
         List<String> l = headers.getRequestHeader("Accept");
         return HeaderHelper.convertToString(l);
      }

      @GET
      @Path("e")
      public String m2()
      {
         return request.getMethod();
      }

      @GET
      @Path("f")
      public void m3()
      {
         assertNotNull(providers);
      }

      @GET
      @Path("g")
      public void m4()
      {
         assertNotNull(properties);
         properties.setProperty("ws.rs.tmpdir", "null");
      }
   }

   public void testConstructorInjection() throws Exception
   {
      registry(Resource3.class);
      injectionTest();
      unregistry(Resource3.class);
   }

   //

   private void injectionTest() throws Exception
   {
      assertEquals("http://localhost/test/a/b/c", launcher.service("GET", "http://localhost/test/a/b/c",
         "http://localhost/test", null, null, null).getEntity());
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      h.add("Accept", "text/xml");
      h.add("Accept", "text/plain;q=0.7");
      assertEquals("text/xml,text/plain;q=0.7", launcher.service("GET", "http://localhost/test/a/b/d",
         "http://localhost/test", h, null, null).getEntity());
      assertEquals("GET", launcher.service("GET", "http://localhost/test/a/b/e", "http://localhost/test", null, null,
         null).getEntity());
      assertEquals(204, launcher.service("GET", "http://localhost/test/a/b/f", "http://localhost/test", null, null,
         null).getStatus());
      assertEquals(204, launcher.service("GET", "http://localhost/test/a/b/g", "http://localhost/test", null, null,
         null).getStatus());
   }

}
