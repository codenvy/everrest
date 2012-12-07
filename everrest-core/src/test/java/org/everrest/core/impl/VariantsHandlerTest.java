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

import org.everrest.core.tools.SimpleSecurityContext;

import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Variant;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class VariantsHandlerTest extends BaseTest
{
   public void testVariantHandler() throws Exception
   {
      List<Variant> vs =
         Variant.VariantListBuilder.newInstance().mediaTypes(MediaType.valueOf("image/jpeg")).add().mediaTypes(
            MediaType.valueOf("application/xml")).languages(new Locale("en", "us")).add().mediaTypes(
            MediaType.valueOf("text/xml")).languages(new Locale("en")).add().mediaTypes(MediaType.valueOf("text/xml"))
            .languages(new Locale("en", "us")).add().build();

      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      h.putSingle("Accept", glue("text/xml", "image/png", "text/html;q=0.9", "text/plain;q=0.8", "application/xml",
         "*/*;q=0.5"));

      h.putSingle("Accept-Language", "en-us,en;q=0.5");
      ContainerRequest r = new ContainerRequest("GET", null, null, null, h, null);
      Variant v = VariantsHandler.handleVariants(r, vs);
      assertEquals(new MediaType("text", "xml"), v.getMediaType());
      assertEquals(new Locale("en", "us"), v.getLanguage());
      // ---
      h.putSingle("Accept", glue("text/xml;q=0.95", "text/html;q=0.9", "application/xml", "image/png",
         "text/plain;q=0.8", "*/*;q=0.5"));
      h.putSingle("Accept-Language", "en-us;q=0.5,en;q=0.7");
      r = new ContainerRequest("GET", null, null, null, h, new SimpleSecurityContext(false));
      v = VariantsHandler.handleVariants(r, vs);
      // 'application/xml' has higher 'q' value then 'text/xml'
      assertEquals(new MediaType("application", "xml"), v.getMediaType());
      assertEquals(new Locale("en", "us"), v.getLanguage());
      // ---
      h.putSingle("Accept", glue("text/xml", "application/xml", "text/plain;q=0.8", "image/png", "text/html;q=0.9",
         "*/*;q=0.5"));

      h.putSingle("Accept-Language", "en,en-us");
      r = new ContainerRequest("GET", null, null, null, h, new SimpleSecurityContext(false));
      v = VariantsHandler.handleVariants(r, vs);
      assertEquals(new MediaType("text", "xml"), v.getMediaType());
      // then 'en' goes first in 'accept' list
      assertEquals(new Locale("en"), v.getLanguage());
      // ---
      h.putSingle("Accept", glue("text/xml", "application/xml", "image/png", "text/html;q=0.9", "text/plain;q=0.8",
         "*/*;q=0.5"));

      h.putSingle("Accept-Language", "uk");
      r = new ContainerRequest("GET", null, null, null, h, new SimpleSecurityContext(false));
      v = VariantsHandler.handleVariants(r, vs);
      // no language 'uk' in variants then '*/*;q=0.5' will work
      assertEquals(new MediaType("image", "jpeg"), v.getMediaType());
      // ---
      h.putSingle("Accept", glue("text/xml", "application/xml", "image/png", "text/html;q=0.9", "text/plain;q=0.8"));

      h.putSingle("Accept-Language", "uk");
      r = new ContainerRequest("GET", null, null, null, h, new SimpleSecurityContext(false));
      v = VariantsHandler.handleVariants(r, vs);
      // no language 'uk' in variants and '*/*;q=0.5' removed 
      assertNull(v); // 'Not Acceptable' (406) will be generated here
      // ---
      h.putSingle("Accept", glue("text/xml", "application/xml", "image/*", "text/html;q=0.9", "text/plain;q=0.8"));

      h.putSingle("Accept-Language", "uk");
      r = new ContainerRequest("GET", null, null, null, h, new SimpleSecurityContext(false));
      v = VariantsHandler.handleVariants(r, vs);
      // no language 'uk' in variants then 'image/*' will work
      assertEquals(new MediaType("image", "jpeg"), v.getMediaType());
   }

   private static String glue(String... s)
   {
      StringBuilder sb = new StringBuilder();
      for (String _s : s)
      {
         if (sb.length() > 0)
            sb.append(',');
         sb.append(_s);
      }
      return sb.toString();
   }

}
