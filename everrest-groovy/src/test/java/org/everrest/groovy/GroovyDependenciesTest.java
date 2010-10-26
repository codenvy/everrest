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
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import java.io.InputStream;
import java.net.URL;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class GroovyDependenciesTest extends BaseTest
{
   private InputStream script;

   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      URL root = Thread.currentThread().getContextClassLoader().getResource("repo");
      DefaultGroovyResourceLoader groovyResourceLoader = new DefaultGroovyResourceLoader(root);
      groovyPublisher.getGroovyClassLoader().setResourceLoader(groovyResourceLoader);

      script = Thread.currentThread().getContextClassLoader().getResourceAsStream("a/b/GMain1.groovy");
      assertNotNull(script);
   }

   public void testDependency() throws Exception
   {
      groovyPublisher.publishPerRequest(script, new BaseResourceId("GMain1"), null);
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      ContainerResponse resp = launcher.service("GET", "/a", "", null, null, writer, null);
      assertEquals(200, resp.getStatus());
      assertEquals("dependencies.GDependency1", new String(writer.getBody()));
   }

}
