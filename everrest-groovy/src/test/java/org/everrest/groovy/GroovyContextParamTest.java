/**
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.everrest.groovy;

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.everrest.test.mock.MockHttpServletRequest;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: GroovyContextParamTest.java 2647 2010-06-17 08:39:29Z aparfonov
 *          $
 */
public class GroovyContextParamTest extends BaseTest
{

   private InputStream script;

   public void setUp() throws Exception
   {
      super.setUp();
      script = Thread.currentThread().getContextClassLoader().getResourceAsStream("a/b/GroovyResource1.groovy");
      assertNotNull(script);
   }

   @Override
   public void tearDown() throws Exception
   {
      groovyPublisher.resources.clear();
      super.tearDown();
   }

   public void testPerRequest() throws Exception
   {
      assertEquals(0, resources.getSize());
      assertEquals(0, groovyPublisher.resources.size());

      groovyPublisher.publishPerRequest(script, new BaseResourceId("g1"), null, null, null);

      assertEquals(1, resources.getSize());
      assertEquals(1, groovyPublisher.resources.size());

      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

      EnvironmentContext envctx = new EnvironmentContext();

      HttpServletRequest httpRequest =
         new MockHttpServletRequest("http://localhost:8080/context/a/b", null, 0, "GET", null);
      envctx.put(HttpServletRequest.class, httpRequest);

      ContainerResponse resp =
         launcher.service("GET", "http://localhost:8080/context/a/b", "http://localhost:8080/context", null, null,
            writer, envctx);
      assertEquals(200, resp.getStatus());
      assertEquals("GET\n/context/a/b", new String(writer.getBody()));
   }

}
