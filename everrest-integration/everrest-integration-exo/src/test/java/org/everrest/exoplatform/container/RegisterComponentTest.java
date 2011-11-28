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
package org.everrest.exoplatform.container;

import org.everrest.core.ContainerResponseWriter;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.ContainerRequest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.InputHeadersMap;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.tools.DummyContainerResponseWriter;
import org.everrest.exoplatform.StandaloneBaseTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class RegisterComponentTest extends StandaloneBaseTest
{
   private RestfulContainer restfulContainer;
   private Field restToComponentAdaptersField;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      restfulContainer = new RestfulContainer(container);
      ContainerResponseWriter writer = new DummyContainerResponseWriter();
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      h.putSingle("TEST", "TO BE OR NOT TO BE");
      ContainerRequest request =
         new ContainerRequest("GET", new URI("/a"), new URI(""), new ByteArrayInputStream(new byte[0]),
            new InputHeadersMap(h));
      ContainerResponse response = new ContainerResponse(writer);
      ApplicationContextImpl.setCurrent(new ApplicationContextImpl(request, response, null));
      // Add dependencies to different containers to be sure both are resolvable.
      container.registerComponentInstance(new ConstructorDependency());
      restfulContainer.registerComponentInstance(new InjectDependency());

      restToComponentAdaptersField = restfulContainer.getClass().getDeclaredField("restToComponentAdapters");
      restToComponentAdaptersField.setAccessible(true);
   }

   /**
    * @see org.everrest.exoplatform.BaseTest#tearDown()
    */
   @Override
   protected void tearDown() throws Exception
   {
      restfulContainer.stop();
      ApplicationContextImpl.setCurrent(null);
      super.tearDown();
   }

   @SuppressWarnings("rawtypes")
   public void testRegisterProvider() throws Exception
   {
      restfulContainer.registerComponentImplementation("A", A.class);
      assertEquals(1, ((Map)restToComponentAdaptersField.get(restfulContainer)).size());
      restfulContainer.unregisterComponent("A");
      assertEquals(0, ((Map)restToComponentAdaptersField.get(restfulContainer)).size());
   }

   @SuppressWarnings("rawtypes")
   public void testRegisterResource() throws Exception
   {
      restfulContainer.registerComponentImplementation("X", X.class);
      assertEquals(1, ((Map)restToComponentAdaptersField.get(restfulContainer)).size());
      restfulContainer.unregisterComponent("X");
      assertEquals(0, ((Map)restToComponentAdaptersField.get(restfulContainer)).size());
   }

   public void testCreateProvider() throws Exception
   {
      restfulContainer.registerComponentImplementation("A", A.class);
      A a = (A)restfulContainer.getComponentInstance("A");
      assertNotNull(a);
      assertNotNull(a.h);
      assertNotNull(a.u);
      assertEquals("/a", a.u.getRequestUri().getPath());
      assertEquals("TO BE OR NOT TO BE", a.h.getRequestHeaders().getFirst("test"));
      assertNotNull(a.inj);
      assertNotNull(a.c);
   }

   public void testCreateResource() throws Exception
   {
      restfulContainer.registerComponentImplementation("X", X.class);
      X x = (X)restfulContainer.getComponentInstance("X");
      assertNotNull(x);
      assertNotNull(x.h);
      assertNotNull(x.u);
      assertEquals("/a", x.u.getRequestUri().getPath());
      assertEquals("TO BE OR NOT TO BE", x.h.getRequestHeaders().getFirst("test"));
      assertNotNull(x.inj);
      assertNotNull(x.c);
   }

   public static class ConstructorDependency
   {
   }

   public static class InjectDependency
   {
   }

   @Path("X")
   public static class X
   {
      @Context
      HttpHeaders h;
      @Context
      UriInfo u;
      @Inject
      InjectDependency inj;
      final ConstructorDependency c;

      public X(ConstructorDependency c)
      {
         this.c = c;
      }

      @GET
      public void a()
      {
      }
   }

   @Provider
   public static class A implements MessageBodyReader<Object>
   {
      @Context
      HttpHeaders h;
      @Context
      UriInfo u;
      @Inject
      InjectDependency inj;
      final ConstructorDependency c;

      public A(ConstructorDependency c)
      {
         this.c = c;
      }

      @Override
      public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
      {
         return false;
      }

      @Override
      public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
         MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
         WebApplicationException
      {
         return null;
      }
   }
}
