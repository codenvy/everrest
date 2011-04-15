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
import org.everrest.core.impl.MultivaluedMapImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class JsonEntityProviderTest extends BaseTest
{

   private static final String DATA = "{\"name\":\"andrew\",\"password\":\"hello\"}";

   private MediaType mediaType;

   public void setUp() throws Exception
   {
      super.setUp();
      mediaType = new MediaType("application", "json");
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   public void testRead() throws Exception
   {
      MessageBodyReader reader = providers.getMessageBodyReader(Bean.class, null, null, mediaType);
      assertNotNull(reader);
      assertTrue(reader.isReadable(Bean.class, Bean.class, null, mediaType));
      byte[] data = DATA.getBytes("UTF-8");
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      h.putSingle(HttpHeaders.CONTENT_LENGTH, "" + data.length);
      Bean bean = (Bean)reader.readFrom(Bean.class, Bean.class, null, mediaType, h, new ByteArrayInputStream(data));
      assertEquals("andrew", bean.getName());
      assertEquals("hello", bean.getPassword());
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   public void testWrite() throws Exception
   {
      MessageBodyWriter writer = providers.getMessageBodyWriter(Bean.class, null, null, mediaType);
      assertNotNull(writer);
      assertTrue(writer.isWriteable(Bean.class, Bean.class, null, mediaType));
      Bean bean = new Bean();
      bean.setName("andrew");
      bean.setPassword("test");
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      writer.writeTo(bean, Bean.class, Bean.class, null, mediaType, null, outputStream);
      System.out.println(new String(outputStream.toByteArray()));
   }

   //

   public static class Bean
   {
      private String name;

      private String password;

      public String getName()
      {
         return name;
      }

      public void setName(String name)
      {
         this.name = name;
      }

      public String getPassword()
      {
         return password;
      }

      public void setPassword(String password)
      {
         this.password = password;
      }

      public String toString()
      {
         return "name=" + name + "; password=" + password;
      }
   }

}
