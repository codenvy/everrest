/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.everrest.core.impl.async;

import org.everrest.core.ContainerResponseWriter;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.provider.json.JsonParser;
import org.everrest.core.impl.provider.json.ObjectBuilder;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import java.io.ByteArrayInputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class AsynchronousRequestTest extends BaseTest
{
   @Path("a")
   public static class Resource1
   {
      @GET
      public String m()
      {
         try
         {
            Thread.sleep(2000);
         }
         catch (InterruptedException ie)
         {
            return "stopped";
         }
         return "asynchronous response";
      }
   }

   @Path("b")
   public static class Resource2
   {
      @GET
      @Produces("application/json")
      @Path("sub")
      public Msg m()
      {
         return new Msg();
      }
   }

   @Path("c")
   public static class Resource3
   {
      @GET
      public void m()
      {
         throw new RuntimeException("test process exceptions in asynchronous mode");
      }
   }

   @Path("d")
   public static class Resource4
   {
      @Path("sub")
      public SubResource m()
      {
         return new SubResource1();
      }
   }

   public static interface SubResource
   {
      @GET
      @Produces("application/json")
      Msg m();
   }

   public static class SubResource1 implements SubResource
   {
      public Msg m()
      {
         return new Msg();
      }
   }

   //
   
   public static class Msg
   {
      public String getMessage()
      {
         return "to be or not to be";
      }
   }

   //
   
   public void testRunJob() throws Exception
   {
      registry(Resource1.class);
      ContainerResponse response = launcher.service("GET", "/a?async=true", "", null, null, null);
      assertEquals(202, response.getStatus());
      String jobUrl = (String)response.getEntity();
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      response = getAsyncronousResponse(jobUrl, writer);
      assertEquals(200, response.getStatus());
      assertEquals("asynchronous response", new String(writer.getBody()));
      // Try one more time. Job must be removed from pool so expected result is 404.
      response = launcher.service("GET", jobUrl, "", null, null, null);
      assertEquals(404, response.getStatus());
      unregistry(Resource1.class);
   }

   public void testRemoveJob() throws Exception
   {
      registry(Resource1.class);
      ContainerResponse response = launcher.service("GET", "/a?async=true", "", null, null, null);
      assertEquals(202, response.getStatus());
      String jobUrl = (String)response.getEntity();
      response = launcher.service("DELETE", jobUrl, "", null, null, null);
      assertEquals(204, response.getStatus());
      // check job
      response = launcher.service("GET", jobUrl, "", null, null, null);
      assertEquals(404, response.getStatus());
      unregistry(Resource1.class);
   }

   public void testMimeTypeResolving() throws Exception
   {
      registry(Resource2.class);
      ContainerResponse response = launcher.service("GET", "/b/sub?async=true", "", null, null, null);
      assertEquals(202, response.getStatus());
      String jobUrl = (String)response.getEntity();
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      response = getAsyncronousResponse(jobUrl, writer);
      assertEquals(200, response.getStatus());
      assertEquals("application/json", response.getContentType().toString());
      JsonParser parser = new JsonParser();
      parser.parse(new ByteArrayInputStream(writer.getBody()));
      Msg msg = ObjectBuilder.createObject(Msg.class, parser.getJsonObject());
      assertEquals("to be or not to be", msg.getMessage());
      unregistry(Resource2.class);
   }

   public void testProcessExceptions() throws Exception
   {
      registry(Resource3.class);
      ContainerResponse response = launcher.service("GET", "/c?async=true", "", null, null, null);
      assertEquals(202, response.getStatus());
      String jobUrl = (String)response.getEntity();
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      response = getAsyncronousResponse(jobUrl, writer);
      assertEquals(500, response.getStatus());
      assertEquals("text/plain", response.getContentType().toString());
      assertEquals("test process exceptions in asynchronous mode", new String(writer.getBody()));
      unregistry(Resource3.class);
   }

   public void testWithLocators() throws Exception
   {
      registry(Resource4.class);
      ContainerResponse response = launcher.service("GET", "/d/sub?async=true", "", null, null, null);
      assertEquals(202, response.getStatus());
      String jobUrl = (String)response.getEntity();
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      response = getAsyncronousResponse(jobUrl, writer);
      assertEquals(200, response.getStatus());
      assertEquals("application/json", response.getContentType().toString());
      JsonParser parser = new JsonParser();
      parser.parse(new ByteArrayInputStream(writer.getBody()));
      Msg msg = ObjectBuilder.createObject(Msg.class, parser.getJsonObject());
      assertEquals("to be or not to be", msg.getMessage());
      unregistry(Resource4.class);
   }

   private ContainerResponse getAsyncronousResponse(String jobUrl, ContainerResponseWriter writer) throws Exception
   {
      ContainerResponse response;
      // Limit end time to avoid infinite loop if something going wrong.
      final long endTime = System.currentTimeMillis() + 5000;
      synchronized (this)
      {
         while ((response = launcher.service("GET", jobUrl, "", null, null, writer, null)).getStatus() == 202
            && System.currentTimeMillis() < endTime)
         {
            wait(300);
         }
      }
      return response;
   }
}
