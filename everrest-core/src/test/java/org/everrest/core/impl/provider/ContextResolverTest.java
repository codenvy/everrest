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

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ContextResolverTest.java 497 2009-11-08 13:19:25Z aparfonov $
 */
public class ContextResolverTest extends BaseTest
{

   @Provider
   @Produces("text/plain")
   public static class ContextResolver1 implements javax.ws.rs.ext.ContextResolver<String>
   {

      public String getContext(Class<?> type)
      {
         return "text";
      }

   }

   @Provider
   public static class ContextResolver2 implements javax.ws.rs.ext.ContextResolver<String>
   {

      public String getContext(Class<?> type)
      {
         return "*";
      }

   }

   @Provider
   @Produces("text/xml")
   public static class ContextResolver3 implements javax.ws.rs.ext.ContextResolver<String>
   {

      public String getContext(Class<?> type)
      {
         return "xml";
      }

   }

   @Provider
   @Produces("text/html")
   public static class ContextResolver4 implements javax.ws.rs.ext.ContextResolver<String>
   {

      public String getContext(Class<?> type)
      {
         return "html";
      }

   }

   @Provider
   @Produces("text/*")
   public static class ContextResolver5 implements javax.ws.rs.ext.ContextResolver<String>
   {

      public String getContext(Class<?> type)
      {
         return "anytext";
      }

   }

   public void setUp() throws Exception
   {
      super.setUp();
      providers.addContextResolver(ContextResolver1.class);
      providers.addContextResolver(ContextResolver2.class);
      providers.addContextResolver(ContextResolver3.class);
      providers.addContextResolver(ContextResolver4.class);
      providers.addContextResolver(ContextResolver5.class);
   }

   public void tearDown() throws Exception
   {
      super.setUp();
   }

   public void testContextResolver()
   {
      assertEquals("text", providers.getContextResolver(String.class, new MediaType("text", "plain")).getContext(
         String.class));
      assertEquals("*", providers.getContextResolver(String.class, new MediaType("xxx", "xxx"))
         .getContext(String.class));
      assertEquals("xml", providers.getContextResolver(String.class, new MediaType("text", "xml")).getContext(
         String.class));
      assertEquals("html", providers.getContextResolver(String.class, new MediaType("text", "html")).getContext(
         String.class));
      assertEquals("anytext", providers.getContextResolver(String.class, new MediaType("text", "xxx")).getContext(
         String.class));
   }

}
