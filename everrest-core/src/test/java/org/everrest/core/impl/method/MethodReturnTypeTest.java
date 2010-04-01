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

import org.everrest.core.impl.BaseTest;
import org.everrest.core.tools.ResourceLauncher;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 20 Jan 2009
 * 
 * @author <a href="mailto:dmitry.kataev@exoplatform.com.ua">Dmytro Katayev</a>
 * @version $Id: ResourceReturnTypeTest.java
 */
public class MethodReturnTypeTest extends BaseTest
{

   @Path("/a")
   public static class Resource1
   {

      @GET
      @Path("/0")
      public void m0()
      {

      }

      @GET
      @Path("/1")
      public Response m1()
      {
         // will return Response with 200 (ok) status code
         return Response.ok("body").build();
      }

      @GET
      @Path("/2")
      public Response m2()
      {
         return null;
      }

      @GET
      @Path("/3")
      public GenericEntity<List<String>> m3()
      {
         return null;
      }

      @GET
      @Path("/5")
      public String m5()
      {
         return null;
      }

      @GET
      @Path("/6")
      public String m6()
      {
         return new String("body");
      }

   }

   private ResourceLauncher launcher;

   public void setUp() throws Exception
   {
      super.setUp();
      this.launcher = new ResourceLauncher(requestHandler);
   }

   public void testResourceMethodReturnType() throws Exception
   {
      Resource1 r = new Resource1();
      registry(r);

      // void Results in an empty entity body with a 204 status code.
      assertEquals(204, launcher.service("GET", "/a/0", "", null, null, null).getStatus());
      assertNull(launcher.service("GET", "/a/o", "", null, null, null).getEntity());

      // Response Results in an entity body mapped from the entity property of the
      // Response
      // with the status code specified by the status property of the Response.
      assertEquals(200, launcher.service("GET", "/a/1", "", null, null, null).getStatus());
      assertEquals("body", launcher.service("GET", "/a/1", "", null, null, null).getEntity());

      // GenericEntity Results: null return value results in a 204 status code
      assertEquals(204, launcher.service("GET", "/a/2", "", null, null, null).getStatus());
      assertNull(launcher.service("GET", "/a/2", "", null, null, null).getEntity());

      // a null return value results in a 204 status code.
      assertEquals(204, launcher.service("GET", "/a/3", "", null, null, null).getStatus());
      assertNull(launcher.service("GET", "/a/3", "", null, null, null).getEntity());

      // Other Results: null return value results in a 204 status code
      assertEquals(204, launcher.service("GET", "/a/5", "", null, null, null).getStatus());
      assertNull(launcher.service("GET", "/a/5", "", null, null, null).getEntity());

      // Other Results: null return value results in a 204 status code
      assertEquals(200, launcher.service("GET", "/a/6", "", null, null, null).getStatus());
      assertNotNull(launcher.service("GET", "/a/6", "", null, null, null).getEntity());

      unregistry(r);
   }

}
