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
package org.everrest.ext.filter;

import org.everrest.core.impl.ContainerRequest;
import org.everrest.core.impl.RuntimeDelegateImpl;
import org.everrest.ext.BaseTest;

import java.net.URI;

import javax.ws.rs.ext.RuntimeDelegate;

/**
 * Created by The eXo Platform SAS Author : Dmytro Katayev
 * work.visor.ck@gmail.com 25 Sep 2008
 */
public class UriNormalizationFilterTest extends BaseTest
{

   private String[] testUris =
      {"http://localhost:8080/servlet/../1//2/3/./../../4", "http://localhost:8080/servlet/./1//2/3/./../../4",
         "http://localhost:8080/servlet/1//2/3/./../../4", "http://localhost:8080/servlet/1//2./3/./../4",
         "http://localhost:8080/servlet/1//.2/3/./../4", "http://localhost:8080/servlet/1..//.2/3/./../4",
         "http://localhost:8080/servlet/./1//2/3/./../../4", "http://localhost:8080/servlet/.",
         "http://localhost:8080/servlet/..", "http://localhost:8080/servlet/1"};

   private String[] normalizedUris =
      {"http://localhost:8080/1/4", "http://localhost:8080/servlet/1/4", "http://localhost:8080/servlet/1/4",
         "http://localhost:8080/servlet/1/2./4", "http://localhost:8080/servlet/1/.2/4",
         "http://localhost:8080/servlet/1../.2/4", "http://localhost:8080/servlet/1/4",
         "http://localhost:8080/servlet/", "http://localhost:8080/", "http://localhost:8080/servlet/1"};

   public void setUp()
   {
      RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
   }

   public void testURIFilter() throws Exception
   {

      URI baseUri = new URI("http://localhost:8080/servlet/");
      UriNormalizationFilter filter = new UriNormalizationFilter();

      for (int i = 0; i < testUris.length; i++)
      {
         URI requestUri = new URI(testUris[i]);
         ContainerRequest request = new ContainerRequest("", requestUri, baseUri, null, null);
         filter.doFilter(request);
         assertEquals(normalizedUris[i], request.getRequestUri().toString());
      }
   }
}
