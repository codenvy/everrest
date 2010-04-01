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
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.tools.ResourceLauncher;
import org.everrest.test.mock.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: RequestFilterTest.java -1   $
 */
public class RequestFilterTest extends BaseTest
{

   private ResourceLauncher launcher;

   public void setUp() throws Exception
   {
      super.setUp();
      this.launcher = new ResourceLauncher(requestHandler);
   }

   @Filter
   public static class RequestFilter1 implements RequestFilter
   {

      @Context
      private UriInfo uriInfo;

      @Context
      private HttpHeaders httpHeaders;

      private Providers providers;

      private HttpServletRequest httpRequest;

      public RequestFilter1(@Context Providers providers, @Context HttpServletRequest httpRequest)
      {
         this.providers = providers;
         this.httpRequest = httpRequest;
      }

      public void doFilter(GenericContainerRequest request)
      {
         if (uriInfo != null && httpHeaders != null && providers != null && httpRequest != null)
            request.setMethod("POST");
      }

   }

   @Path("a/b/c/{x:.*}")
   @Filter
   public static class RequestFilter2 implements RequestFilter
   {

      public void doFilter(GenericContainerRequest request)
      {
         request.setMethod("DELETE");
      }

   }

   @Path("a")
   public static class Resource1
   {

      @POST
      public void m0()
      {
      }

      @DELETE
      @Path("b/c/d/e")
      public void m1()
      {

      }

      @PUT
      @Path("c/d/e")
      public void m2()
      {

      }
   }

   public void testWithoutFilter1() throws Exception
   {
      registry(Resource1.class);
      ContainerResponse resp = launcher.service("GET", "/a", "", null, null, null);
      assertEquals(405, resp.getStatus());
      assertEquals(1, resp.getHttpHeaders().get("allow").size());
      assertTrue(resp.getHttpHeaders().get("allow").get(0).toString().contains("POST"));
      unregistry(Resource1.class);
   }

   public void testWithFilter2() throws Exception
   {
      registry(Resource1.class);

      // add filter that can change method
      providers.addRequestFilter(RequestFilter1.class);
      EnvironmentContext env = new EnvironmentContext();
      env.put(HttpServletRequest.class, new MockHttpServletRequest("", new ByteArrayInputStream(new byte[0]), 0,
         "GET", new HashMap<String, List<String>>()));

      // should get status 204
      ContainerResponse resp = launcher.service("GET", "/a", "", null, null, env);
      assertEquals(204, resp.getStatus());

      unregistry(Resource1.class);

   }

   public void testFilter2() throws Exception
   {
      registry(Resource1.class);
      ContainerResponse resp = launcher.service("GET", "/a/b/c/d/e", "", null, null, null);
      assertEquals(405, resp.getStatus());
      assertEquals(1, resp.getHttpHeaders().get("allow").size());
      assertTrue(resp.getHttpHeaders().get("allow").get(0).toString().contains("DELETE"));

      // add filter that can change method
      providers.addRequestFilter(new RequestFilter2());

      // not should get status 204
      resp = launcher.service("GET", "/a/b/c/d/e", "", null, null, null);
      assertEquals(204, resp.getStatus());

      unregistry(Resource1.class);
   }

}
