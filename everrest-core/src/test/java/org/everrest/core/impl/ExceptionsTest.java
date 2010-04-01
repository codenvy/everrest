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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.everrest.core.impl.AbstractResourceTest;
import org.everrest.core.ExtHttpHeaders;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.UnhandledException;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 24 Dec 2009
 * 
 * @author <a href="mailto:max.shaposhnik@exoplatform.com">Max Shaposhnik</a>
 * @version $Id: WebApplicationExceptionTest.java
 */
public class ExceptionsTest extends AbstractResourceTest
{

   @Path("a")
   public static class Resource1
   {

      @GET
      @Path("0")
      public void m0() throws WebApplicationException
      {
         Exception e = new Exception(errorMessage);
         throw new WebApplicationException(e, 500);
      }

      @GET
      @Path("1")
      public void m1() throws WebApplicationException
      {
         Response response = Response.status(500).entity(errorMessage).type("text/plain").build();
         throw new WebApplicationException(new Exception(), response);
      }

      @GET
      @Path("2")
      public Response m2() throws WebApplicationException
      {
         throw new WebApplicationException(500);
      }

      @GET
      @Path("3")
      public void m3() throws Exception
      {
         throw new RuntimeException("Runtime exception");
      }

      @GET
      @Path("4")
      public Response m4() throws Exception
      {
         return Response.status(500).entity(errorMessage).type("text/plain").build();
      }

   }

   private static String errorMessage = "test-error-message";

   private Resource1 resource;

   public void setUp() throws Exception
   {
      super.setUp();
      resource = new Resource1();
      registry(resource);
   }

   public void tearDown() throws Exception
   {
      unregistry(resource);
      super.tearDown();
   }
   
   public void testErrorResponse() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      ContainerResponse response = service("GET", "/a/4", "", null, null, writer);
      assertEquals(500, response.getStatus());
      String entity = new String(writer.getBody());
      assertEquals(errorMessage, entity);
      assertNotNull(response.getHttpHeaders().getFirst(ExtHttpHeaders.JAXRS_BODY_PROVIDED));
   }

   public void testUncheckedException() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      try
      {
         service("GET", "/a/3", "", null, null, writer);
         fail("UnhandledException should be throw by RequstHandlerImpl");
      }
      catch (UnhandledException e)
      {
         // OK
      }
   }

   public void testWebApplicationExceptionWithCause() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      ContainerResponse response = service("GET", "/a/0", "", null, null, writer);
      assertEquals(500, response.getStatus());
      String entity = new String(writer.getBody());
      assertEquals(new Exception(errorMessage).toString(), entity);
      assertNotNull(response.getHttpHeaders().getFirst(ExtHttpHeaders.JAXRS_BODY_PROVIDED));
   }

   public void testWebApplicationExceptionWithoutCause() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      ContainerResponse response = service("GET", "/a/2", "", null, null, writer);
      assertEquals(500, response.getStatus());
      assertNull(response.getEntity());
      assertNull(response.getHttpHeaders().getFirst(ExtHttpHeaders.JAXRS_BODY_PROVIDED));
   }

   public void testWebApplicationExceptionWithResponse() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      ContainerResponse response = service("GET", "/a/1", "", null, null, writer);
      assertEquals(500, response.getStatus());
      String entity = new String(writer.getBody());
      assertEquals(errorMessage, entity);
      assertNotNull(response.getHttpHeaders().getFirst(ExtHttpHeaders.JAXRS_BODY_PROVIDED));
   }

}
