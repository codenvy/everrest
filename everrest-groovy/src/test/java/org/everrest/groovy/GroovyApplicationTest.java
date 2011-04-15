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

import org.everrest.core.impl.ApplicationPublisher;
import org.everrest.groovy.servlet.GroovyEverrestServletContextInitializer;
import org.everrest.test.mock.MockServletContext;

import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class GroovyApplicationTest extends BaseTest
{

   protected ApplicationPublisher applicationPublisher;

   protected GroovyClassLoader groovyClassLoader;

   protected void setUp() throws Exception
   {
      super.setUp();
      groovyClassLoader = new GroovyClassLoader();
      URL root = Thread.currentThread().getContextClassLoader().getResource("repo");
      DefaultGroovyResourceLoader groovyResourceLoader = new DefaultGroovyResourceLoader(root);
      groovyClassLoader.setResourceLoader(groovyResourceLoader);
      applicationPublisher = new ApplicationPublisher(resources, providers);
   }

   public void testApplication() throws Exception
   {
      String application =
         "class Application0 extends javax.ws.rs.core.Application\n"
            + "{\n" //
            + "Set<Class<?>> getClasses(){new HashSet<Class<?>>([a.b.GResource1.class, a.b.GExceptionMapper.class])}\n"
            + "}\n";
      Class<?> class1 = groovyClassLoader.parseClass(application);
      javax.ws.rs.core.Application groovyApplication = (javax.ws.rs.core.Application)class1.newInstance();
      applicationPublisher.publish(groovyApplication);
      assertEquals("GResource1", launcher.service("GET", "/a/1", "", null, null, null).getEntity());
      // ExceptionMapper written in Groovy should process a.b.GRuntimeException.
      assertEquals("GExceptionMapper", launcher.service("GET", "/a/2", "", null, null, null).getEntity());
   }

   public void testScanComponents()
   {
      MockServletContext mockContext = new MockServletContext("test");
      StringBuilder classPath = new StringBuilder();
      classPath.append(Thread.currentThread().getContextClassLoader().getResource("scan/").toString());
      mockContext.setInitParameter(GroovyEverrestServletContextInitializer.EVERREST_GROOVY_ROOT_RESOURCES, classPath
         .toString());
      mockContext.setInitParameter(GroovyEverrestServletContextInitializer.EVERREST_GROOVY_SCAN_COMPONENTS, "true");
      GroovyEverrestServletContextInitializer initializer = new GroovyEverrestServletContextInitializer(mockContext);
      Application application = initializer.getApplication();
      Set<Class<?>> classes = application.getClasses();
      assertNotNull(classes);
      assertEquals(2, classes.size());
      java.util.List<String> l = new ArrayList<String>(2);
      for (Class<?> c : classes)
         l.add(c.getName());
      assertTrue(l.contains("org.everrest.A"));
      assertTrue(l.contains("org.everrest.B"));
   }

}
