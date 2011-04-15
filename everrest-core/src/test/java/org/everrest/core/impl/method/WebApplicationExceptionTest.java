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
import org.everrest.core.impl.method.MethodExceptionTest.UncheckedException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 24 Dec 2009
 *
 * @author <a href="mailto:max.shaposhnik@exoplatform.com">Max Shaposhnik</a>
 * @version $Id: WebApplicationExceptionTest.java
 */
public class WebApplicationExceptionTest extends BaseTest
{

   @Path("/a")
   public static class Resource1
   {

      @GET
      @Path("/0")
      public void m0() throws WebApplicationException
      {
         Exception e = new Exception("testmsg");
         throw new WebApplicationException(e, 500);
      }

      @GET
      @Path("/1")
      public Response m1() throws WebApplicationException
      {
         throw new WebApplicationException(500);
      }

      @GET
      @Path("/2")
      public void m2() throws Exception
      {
         throw new UncheckedException("Unchecked exception");
      }

   }

   public void testExceptionMessage() throws Exception
   {
      Resource1 resource = new Resource1();
      registry(resource);

      assertEquals(500, launcher.service("GET", "/a/0", "", null, null, null).getStatus());
      String entity = (String)launcher.service("GET", "/a/0", "", null, null, null).getEntity();
      assertTrue(entity.indexOf("testmsg") > 0);

      assertEquals(500, launcher.service("GET", "/a/1", "", null, null, null).getStatus());
      assertEquals(null,launcher.service("GET", "/a/1", "", null, null, null).getEntity());
      unregistry(resource);
   }

}
