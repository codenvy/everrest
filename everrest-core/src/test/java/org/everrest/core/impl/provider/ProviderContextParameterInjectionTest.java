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
package org.everrest.core.impl.provider;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.provider.EntityProvider;
import org.everrest.core.tools.ResourceLauncher;
import org.everrest.test.mock.MockHttpServletRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ProviderContextParameterInjectionTest.java 497 2009-11-08
 *          13:19:25Z aparfonov $
 */
public class ProviderContextParameterInjectionTest extends BaseTest
{

   public static class MockEntity
   {
      String entity;
   }

   @Provider
   public static class EntityProviderChecker implements EntityProvider<MockEntity>
   {

      @Context
      private UriInfo uriInfo;

      @Context
      private Request request;

      @Context
      private HttpHeaders httpHeaders;

      @Context
      private Providers providers;

      @Context
      private HttpServletRequest httpRequest;

      // EntityProvider can be used for reading/writing ONLY if all fields above
      // initialized

      public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
      {
         return uriInfo != null && request != null && httpHeaders != null && providers != null && httpRequest != null;
      }

      public MockEntity readFrom(Class<MockEntity> type, Type genericType, Annotation[] annotations,
         MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
         WebApplicationException
      {
         MockEntity me = new MockEntity();
         me.entity = IOHelper.readString(entityStream, IOHelper.DEFAULT_CHARSET_NAME);
         return me;
      }

      public long getSize(MockEntity t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
      {
         return 0;
      }

      public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
      {
         return uriInfo != null && request != null && httpHeaders != null && providers != null && httpRequest != null;
      }

      public void writeTo(MockEntity t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
         MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
         WebApplicationException
      {
         IOHelper.writeString(t.entity, entityStream, IOHelper.DEFAULT_CHARSET_NAME);
      }

   }

   @Provider
   public static class ExceptionMapperChecker implements ExceptionMapper<RuntimeException>
   {

      @Context
      private UriInfo uriInfo;

      @Context
      private Request request;

      @Context
      private HttpHeaders httpHeaders;

      @Context
      private Providers providers;

      @Context
      private HttpServletRequest httpRequest;

      public Response toResponse(RuntimeException exception)
      {
         if (uriInfo != null && request != null && httpHeaders != null && providers != null && httpRequest != null)
            return Response.status(200).build();
         else
            return Response.status(500).build();
      }

   }

   @Provider
   @Produces("text/plain")
   public static class ContextResolverChecker implements ContextResolver<String>
   {

      @Context
      private UriInfo uriInfo;

      @Context
      private Request request;

      @Context
      private HttpHeaders httpHeaders;

      @Context
      private Providers providers;

      @Context
      private HttpServletRequest httpRequest;

      public String getContext(Class<?> type)
      {
         if (uriInfo != null && request != null && httpHeaders != null && providers != null && httpRequest != null)
            return "to be to not to be";
         return null;
      }

   }

   private ResourceLauncher launcher;

   public void setUp() throws Exception
   {
      super.setUp();
      providers.addMessageBodyReader(EntityProviderChecker.class);
      providers.addMessageBodyWriter(EntityProviderChecker.class);
      providers.addExceptionMapper(ExceptionMapperChecker.class);
      providers.addContextResolver(ContextResolverChecker.class);
      this.launcher = new ResourceLauncher(requestHandler);
   }

   public void tearDown() throws Exception
   {
      super.tearDown();
   }

   @Path("a")
   public static class Resource1
   {

      @Context
      private Providers providers;

      @GET
      @Path("1")
      public MockEntity m0(MockEntity me)
      {
         assertNotNull(me);
         assertEquals("to be or not to be", me.entity);
         me.entity = "to be";
         return me;
      }

      @GET
      @Path("2")
      public void m1()
      {
         throw new RuntimeException();
      }

      @GET
      @Path("3")
      public String m2()
      {
         ContextResolver<String> r = providers.getContextResolver(String.class, new MediaType("text", "plain"));
         return r.getContext(String.class);
      }
   }

   public void testParameterInjection() throws Exception
   {
      registry(Resource1.class);

      EnvironmentContext env = new EnvironmentContext();
      env.put(HttpServletRequest.class, new MockHttpServletRequest("", new ByteArrayInputStream(new byte[0]), 0, "GET",
         new HashMap<String, List<String>>()));
      ContainerResponse resp = launcher.service("GET", "/a/1", "", null, "to be or not to be".getBytes(), env);
      assertEquals("to be", ((MockEntity)resp.getEntity()).entity);

      resp = launcher.service("GET", "/a/2", "", null, null, env);
      assertEquals(200, resp.getStatus());

      resp = launcher.service("GET", "/a/3", "", null, null, env);
      assertEquals(200, resp.getStatus());
      assertEquals("to be to not to be", resp.getEntity());

      unregistry(Resource1.class);
   }

}
