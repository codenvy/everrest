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

import org.everrest.core.ResourcePublicationException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class ResourceBinderTest extends BaseTest
{

   /**
    * {@inheritDoc}
    */
   @Override
   public void setUp() throws Exception
   {
      super.setUp();
   }

   public void testBind()
   {
      resources.addResource(Resource.class, null);
      assertEquals(1, resources.getSize());
   }

   public void testUnbind()
   {
      resources.addResource(Resource.class, null);
      resources.removeResource(Resource.class);
      assertEquals(0, resources.getSize());
   }

   @Path("/a/b/{c}")
   public static class Resource
   {

      @SuppressWarnings("unused")
      @PathParam("c")
      private String pathsegm;

      public Resource()
      {
      }

      public Resource(@Context UriInfo uriInfo)
      {
      }

      @GET
      @Produces("text/html")
      public void m1()
      {
      }

      @GET
      @Path("d")
      @Produces("text/html")
      public void m2()
      {
      }

      @Path("d")
      public void m3()
      {
      }
   }

   //-------------------------------------

   public void testSameResourceURI()
   {
      resources.addResource(SameURIResource1.class, null);
      assertEquals(1, resources.getSize());
      try
      {
         resources.addResource(SameURIResource2.class, null);
      }
      catch (ResourcePublicationException e)
      {
      }
      assertEquals(1, resources.getSize());

      resources.clear();
      resources.addResource(SameURIResource2.class, null);
      assertEquals(1, resources.getSize());
      try
      {
         resources.addResource(SameURIResource1.class, null);
      }
      catch (ResourcePublicationException e)
      {
      }
      assertEquals(1, resources.getSize());

      resources.clear();
      resources.addResource(new SameURIResource1(), null);
      assertEquals(1, resources.getSize());
      try
      {
         resources.addResource(new SameURIResource2(), null);
      }
      catch (ResourcePublicationException e)
      {
      }
      assertEquals(1, resources.getSize());

      resources.clear();
      resources.addResource(new SameURIResource2(), null);
      assertEquals(1, resources.getSize());
      try
      {
         resources.addResource(new SameURIResource1(), null);
      }
      catch (ResourcePublicationException e)
      {
      }
      assertEquals(1, resources.getSize());
   }

   @Path("/a/b/c/{d}/e")
   public static class SameURIResource1
   {
      @GET
      public void m0()
      {
      }
   }

   @Path("/a/b/c/{d}/e")
   public static class SameURIResource2
   {
      @GET
      public void m0()
      {
      }
   }

}
