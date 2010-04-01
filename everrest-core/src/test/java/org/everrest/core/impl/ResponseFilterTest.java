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
package org.everrest.core.impl;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;

import org.everrest.core.Filter;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.ResponseFilter;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.tools.ResourceLauncher;
import org.everrest.test.mock.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ResponseFilterTest.java -1   $
 */
public class ResponseFilterTest extends BaseTest
{

   private ResourceLauncher launcher;

   public void setUp() throws Exception
   {
      super.setUp();
      this.launcher = new ResourceLauncher(requestHandler);
   }

   @Filter
   public static class ResponseFilter1 implements ResponseFilter
   {

      @Context
      private UriInfo uriInfo;

      @Context
      private HttpHeaders httpHeaders;

      private Providers providers;

      private HttpServletRequest httpRequest;

      public ResponseFilter1(@Context Providers providers, @Context HttpServletRequest httpRequest)
      {
         this.providers = providers;
         this.httpRequest = httpRequest;
      }

      public void doFilter(GenericContainerResponse response)
      {
         if (uriInfo != null && httpHeaders != null && providers != null && httpRequest != null)
            response.setResponse(Response.status(200).entity("to be or not to be").type("text/plain").build());
      }

   }

   @Path("a/b/c/{x:.*}")
   @Filter
   public static class ResponseFilter2 implements ResponseFilter
   {

      public void doFilter(GenericContainerResponse response)
      {
         response.setResponse(Response.status(response.getStatus()).entity(response.getEntity()).type(
            "application/json").build());
      }

   }

   @Path("a")
   public static class Resource1
   {

      @POST
      public void m0()
      {
      }

      @POST
      @Path("b/c/d/e")
      @Produces("text/plain")
      public String m1()
      {
         // text/plain will be overridden in response filter 
         return "{\"name\":\"andrew\", \"password\":\"hello\"}";
      }

   }

   //------------------------------------

   public void testFilter() throws Exception
   {
      Resource1 r = new Resource1();
      registry(r);
      ContainerResponse resp = launcher.service("POST", "/a", "", null, null, null);
      assertEquals(204, resp.getStatus());

      // should not be any changes after add this
      providers.addResponseFilter(new ResponseFilter2());
      resp = launcher.service("POST", "/a", "", null, null, null);
      assertEquals(204, resp.getStatus());

      // add response filter and try again
      providers.addResponseFilter(ResponseFilter1.class);

      EnvironmentContext env = new EnvironmentContext();
      env.put(HttpServletRequest.class, new MockHttpServletRequest("", new ByteArrayInputStream(new byte[0]), 0,
         "POST", new HashMap<String, List<String>>()));
      resp = launcher.service("POST", "/a", "", null, null, env);
      assertEquals(200, resp.getStatus());
      assertEquals("text/plain", resp.getContentType().toString());
      assertEquals("to be or not to be", resp.getEntity());

      unregistry(r);
   }

   public void testFilter2() throws Exception
   {
      Resource1 r = new Resource1();
      registry(r);
      ContainerResponse resp = launcher.service("POST", "/a/b/c/d/e", "", null, null, null);
      assertEquals(200, resp.getStatus());
      assertEquals("text/plain", resp.getContentType().toString());
      assertEquals("{\"name\":\"andrew\", \"password\":\"hello\"}", resp.getEntity());

      // add response filter and try again
      providers.addResponseFilter(new ResponseFilter2());

      resp = launcher.service("POST", "/a/b/c/d/e", "", null, null, null);
      assertEquals(200, resp.getStatus());
      assertEquals("application/json", resp.getContentType().toString());
      assertEquals("{\"name\":\"andrew\", \"password\":\"hello\"}", resp.getEntity());

      unregistry(r);
   }

}
