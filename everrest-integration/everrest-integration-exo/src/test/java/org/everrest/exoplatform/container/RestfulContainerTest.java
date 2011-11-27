/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.everrest.exoplatform.container;

import org.everrest.exoplatform.StandaloneBaseTest;
import org.picocontainer.ComponentAdapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class RestfulContainerTest extends StandaloneBaseTest
{
   private RestfulContainer restfulContainer;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      restfulContainer = new RestfulContainer(container);
   }

   /**
    * @see org.everrest.exoplatform.BaseTest#tearDown()
    */
   @Override
   protected void tearDown() throws Exception
   {
      restfulContainer.stop();
      super.tearDown();
   }

   public void testFindComponentAdaptersByAnnotation() throws Exception
   {
      restfulContainer.registerComponentImplementation(A.class);
      restfulContainer.registerComponentImplementation(B.class);
      restfulContainer.registerComponentImplementation(C.class);
      restfulContainer.registerComponentImplementation(D.class);
      List<ComponentAdapter> adapters = restfulContainer.getComponentAdapters(MyAnnotation.class);
      assertEquals(3, adapters.size());
      List<Class<?>> l = new ArrayList<Class<?>>(3);
      for (ComponentAdapter a : adapters)
      {
         l.add(a.getComponentImplementation());
      }
      assertTrue(l.contains(A.class));
      assertTrue(l.contains(B.class));
      assertTrue(l.contains(D.class));
   }

   public void testFindComponentAdaptersByTypeAndAnnotation() throws Exception
   {
      restfulContainer.registerComponentImplementation(A.class);
      restfulContainer.registerComponentImplementation(B.class);
      restfulContainer.registerComponentImplementation(C.class);
      restfulContainer.registerComponentImplementation(D.class);
      List<ComponentAdapter> adapters = restfulContainer.getComponentAdaptersOfType(I.class, MyAnnotation.class);
      assertEquals(2, adapters.size());
      List<Class<?>> l = new ArrayList<Class<?>>(2);
      for (ComponentAdapter a : adapters)
      {
         l.add(a.getComponentImplementation());
      }
      assertTrue(l.contains(A.class));
      assertTrue(l.contains(B.class));
   }

   @Retention(RetentionPolicy.RUNTIME)
   public static @interface MyAnnotation {
   }

   public static interface I
   {
   }

   @MyAnnotation
   public static class A implements I
   {
   }

   @MyAnnotation
   public static class B extends A
   {
   }

   public static class C implements I
   {
   }

   @MyAnnotation
   public static class D
   {
   }
}
