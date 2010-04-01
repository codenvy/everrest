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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 21 Jan 2009
 * 
 * @author <a href="mailto:dmitry.kataev@exoplatform.com.ua">Dmytro Katayev</a>
 * @version $Id: OptionsMethodTest.java
 */
public class OptionsMethodTest extends BaseTest
{

   @Target(ElementType.METHOD)
   @Retention(RetentionPolicy.RUNTIME)
   @HttpMethod("OPTIONS")
   public @interface OPTIONS {

   }

   @Path("/a")
   public static class Resource1
   {

      @OPTIONS
      public String m0()
      {
         return "options";
      }

   }

   @Path("/b")
   public static class Resource2
   {

      @GET
      public String m0()
      {
         return "get";
      }

   }

   private ResourceLauncher launcher;

   public void setUp() throws Exception
   {
      super.setUp();
      this.launcher = new ResourceLauncher(requestHandler);
   }

   public void testOptionsMethod() throws Exception
   {
      Resource1 resource1 = new Resource1();
      registry(resource1);
      assertEquals("options", launcher.service("OPTIONS", "/a", "", null, null, null).getEntity());
      unregistry(resource1);

      Resource2 resource2 = new Resource2();
      registry(resource2);
      assertEquals(200, launcher.service("OPTIONS", "/b", "", null, null, null).getStatus());
      assertNotNull(launcher.service("OPTIONS", "/b", "", null, null, null).getResponse().getMetadata());

   }

}
