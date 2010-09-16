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
 * @version $Id: GroovySecureRestrictionTest.java 2762 2010-07-09 13:39:29Z aparfonov $
 */
public class GroovySecureRestrictionTest extends BaseTest
{

   private InputStream script;

   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      assertNotNull("SecurityManager not installed", System.getSecurityManager());
      script = Thread.currentThread().getContextClassLoader().getResourceAsStream("GroovyResource3.groovy");
      assertNotNull(script);
   }

   public void testReadSystemPropertyFail() throws Exception
   {
      groovyPublisher.publishPerRequest(script, new BaseResourceId("g1"), null);
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      ContainerResponse resp = launcher.service("GET", "/a/b", "", null, null, writer, null);
      assertEquals(500, resp.getStatus());
      assertTrue(new String(writer.getBody()).startsWith("access denied"));
   }

}
