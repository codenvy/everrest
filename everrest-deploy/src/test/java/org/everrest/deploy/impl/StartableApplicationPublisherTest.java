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
package org.everrest.deploy.impl;

import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.RequestFilter;
import org.everrest.core.RequestHandler;
import org.everrest.core.ResponseFilter;
import org.everrest.core.impl.ContainerRequest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.provider.EntityProvider;
import org.everrest.core.resource.GenericMethodResource;
import org.everrest.core.resource.ResourceContainer;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: StartableApplicationPublisherTest.java 436 2009-10-28 06:47:29Z aparfonov $
 */
public class StartableApplicationPublisherTest extends BaseTest
{

   private RequestHandler handler;

   public void setUp() throws Exception
   {
      super.setUp();
      setContext();
      handler = (RequestHandler)container.getComponentInstanceOfType(RequestHandler.class);
      assertNotNull(handler);
   }

   // Check does we get all additional component specified in configuration.
   public void _testDeployAdditionalComponents()
   {
      ProviderBinder providers = ProviderBinder.getInstance();
      // XXX Able (no NPE) to use null instead "path" because UriPattern
      // for filters is null and path will be never checked.
      assertEquals(1, providers.getRequestFilters(null).size());
      assertEquals(1, providers.getMethodInvokerFilters(null).size());
      // -----
      assertNotNull(providers.getMessageBodyReader(FakeEntity.class, null, null, MediaType.APPLICATION_XML_TYPE));
      assertNotNull(providers.getMessageBodyWriter(FakeEntity.class, null, null, MediaType.APPLICATION_XML_TYPE));
   }

   public void testResource() throws Exception
   {
      ContainerRequest request =
         new ContainerRequest("GET", new URI("http://localhost:8080/rest/a/b"), new URI("http://localhost:8080/rest"),
            new ByteArrayInputStream("to be or not to be".getBytes("UTF-8")), new MultivaluedMapImpl());
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      ContainerResponse response = new ContainerResponse(writer);
      handler.handleRequest(request, response);
      assertEquals(200, response.getStatus());
      assertEquals("echo: to be or not to be", new String(writer.getBody()));
   }

   @Provider
   @Produces(MediaType.APPLICATION_XML)
   @Consumes(MediaType.APPLICATION_XML)
   public static class FakeEntityProvider implements EntityProvider<FakeEntity>
   {

      public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
      {
         return type == FakeEntity.class;
      }

      public FakeEntity readFrom(Class<FakeEntity> type, Type genericType, Annotation[] annotations,
         MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
         WebApplicationException
      {
         return new FakeEntity();
      }

      public long getSize(FakeEntity t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
      {
         return 0;
      }

      public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
      {
         return type == FakeEntity.class;
      }

      public void writeTo(FakeEntity t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
         MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
         WebApplicationException
      {
         entityStream.write("fake-entity".getBytes("UTF-8"));
      }

   }

   public static class FakeEntity
   {

   }

   // --------------

   @Filter
   public static class FakeRequestFilter implements RequestFilter
   {
      public void doFilter(GenericContainerRequest request)
      {
      }
   }

   // --------------

   @Filter
   public static class FakeResponseFilter implements ResponseFilter
   {
      public void doFilter(GenericContainerResponse response)
      {
      }
   }

   // --------------

   @Filter
   public static class FakeMethodInvokerFilter implements MethodInvokerFilter
   {
      public void accept(GenericMethodResource genericMethodResource)
      {
      }
   }

   // -------------- Component of exo-container. It will become to be singleton resource.

   @Path("a")
   public static class Resource1 implements ResourceContainer
   {

      @GET
      @Path("b")
      public String m0(String s)
      {
         return "echo: " + s;
      }

   }

}
