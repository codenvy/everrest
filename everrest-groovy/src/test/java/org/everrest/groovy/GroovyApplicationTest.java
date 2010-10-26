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

import groovy.lang.GroovyClassLoader;

import java.net.URL;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class GroovyApplicationTest extends BaseTest
{

   protected GroovyApplicationPublisher groovyApplicationPublisher;

   protected GroovyClassLoader groovyClassLoader;

   protected void setUp() throws Exception
   {
      super.setUp();
      groovyClassLoader = new GroovyClassLoader();
      URL root = Thread.currentThread().getContextClassLoader().getResource("repo");
      DefaultGroovyResourceLoader groovyResourceLoader = new DefaultGroovyResourceLoader(root);
      groovyClassLoader.setResourceLoader(groovyResourceLoader);
      groovyApplicationPublisher = new GroovyApplicationPublisher(resources, providers, groovyClassLoader);
   }

   public void testApplication() throws Exception
   {
      String application = "class Application0 extends org.everrest.groovy.GroovyApplication {\n" //
         + "Set<String> getScripts(){new HashSet<String>(['a.b.GResource1','a.b.GExceptionMapper'])}" //
         + "}";
      Class<?> class1 = groovyClassLoader.parseClass(application);
      GroovyApplication groovyApplication = (GroovyApplication)class1.newInstance();
      groovyApplicationPublisher.publish(groovyApplication);
      assertEquals("GResource1", launcher.service("GET", "/a/1", "", null, null, null).getEntity());
      // ExceptionMapper written in Groovy should process a.b.GRuntimeException.
      assertEquals("GExceptionMapper", launcher.service("GET", "/a/2", "", null, null, null).getEntity());
   }

}
