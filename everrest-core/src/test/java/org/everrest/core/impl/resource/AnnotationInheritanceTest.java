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

import org.everrest.core.ComponentLifecycleScope;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.resource.AbstractResourceDescriptorImpl;
import org.everrest.core.tools.ResourceLauncher;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 23 Jan 2009
 * 
 * @author <a href="mailto:dmitry.kataev@exoplatform.com.ua">Dmytro Katayev</a>
 * @version $Id: AnnotationInheritanceTest.java
 */
public class AnnotationInheritanceTest extends BaseTest
{

   public static interface ResourceInterface
   {
      @GET
      @Produces(MediaType.TEXT_XML)
      String m0(String type);
   }

   @Path("/a")
   public static class Resource1 implements ResourceInterface
   {
      public String m0(String type)
      {
         return "m0";
      }
   }

   @Path("/a")
   public static class Resource2 implements ResourceInterface
   {
      @Produces(MediaType.APPLICATION_ATOM_XML)
      public String m0(String type)
      {
         return "m0";
      }
   }

   // 

   public static interface ResourceInterface1
   {
      @GET
      void m0();
   }

   public static interface ResourceInterface2
   {
      @GET
      void m0();
   }

   @Path("a")
   public static class Resource3 implements ResourceInterface1, ResourceInterface2
   {
      public void m0()
      {
      }
   }

   private ResourceLauncher launcher;

   public void setUp() throws Exception
   {
      super.setUp();
      this.launcher = new ResourceLauncher(requestHandler);
   }

   public void testFailedInheritance()
   {
      try
      {
         new AbstractResourceDescriptorImpl(Resource3.class, ComponentLifecycleScope.PER_REQUEST);
         fail("Should be failed here, equivocality annotation on method m0");
      }
      catch (RuntimeException e)
      {
      }
   }

   public void testAnnotationsInheritance() throws Exception
   {
      Resource1 resource1 = new Resource1();
      Resource2 resource2 = new Resource2();

      registry(resource1);

      assertEquals(200, launcher.service("GET", "/a", "", null, null, null).getStatus());
      assertEquals("m0", launcher.service("GET", "/a", "", null, null, null).getEntity());
      assertEquals(MediaType.TEXT_XML_TYPE, launcher.service("GET", "/a", "", null, null, null)
         .getContentType());

      unregistry(resource1);

      registry(resource2);
      assertEquals(200, launcher.service("GET", "/a", "", null, null, null).getStatus());
      assertEquals("m0", launcher.service("GET", "/a", "", null, null, null).getEntity());
      assertEquals(MediaType.APPLICATION_ATOM_XML_TYPE, launcher.service("GET", "/a", "", null, null, null)
         .getContentType());
      unregistry(resource2);

   }

}
