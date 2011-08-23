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

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class GroovyIoCInjectTest extends BaseTest
{

   private InputStream script;

   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      script = Thread.currentThread().getContextClassLoader().getResourceAsStream("a/b/GroovyResource2.groovy");
      assertNotNull(script);
   }

   @Override
   public void tearDown() throws Exception
   {
      groovyPublisher.resources.clear();
      super.tearDown();
   }

   public void testComponentPerRequest() throws Exception
   {
      dependencies.addComponent(Component1.class, new Component1());
      iocComponentTest(false, new BaseResourceId("g1"));
   }

   public void testComponentSingleton() throws Exception
   {
      dependencies.addComponent(Component1.class, new Component1());
      iocComponentTest(true, new BaseResourceId("g2"));
   }

   private void iocComponentTest(boolean singleton, ResourceId resourceId) throws Exception
   {
      int initSize = resources.getSize();
      assertEquals(0, groovyPublisher.resources.size());

      if (singleton)
         groovyPublisher.publishSingleton(script, resourceId, null, null, null);
      else
         groovyPublisher.publishPerRequest(script, resourceId, null, null, null);

      assertEquals(initSize + 1, resources.getSize());
      assertEquals(1, groovyPublisher.resources.size());

      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      ContainerResponse resp = launcher.service("GET", "/a/b", "", null, null, writer, null);
      assertEquals(200, resp.getStatus());
      assertEquals("ioc component", new String(writer.getBody()));
   }

   public static class Component1
   {
      public String getName()
      {
         return "ioc component";
      }
   }

}
