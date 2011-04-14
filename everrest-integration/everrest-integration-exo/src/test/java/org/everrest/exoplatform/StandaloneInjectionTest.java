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
package org.everrest.exoplatform;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class StandaloneInjectionTest extends StandaloneBaseTest
{
   public static interface Injectable
   {
      String getName();
   }

   public static class InjectableImpl implements Injectable
   {
      @Override
      public String getName()
      {
         return getClass().getName();
      }
   }

   @Path("StandaloneInjectionTest.Resource1")
   public static class Resource1
   {
      @javax.inject.Inject
      Injectable inj;

      @GET
      public void m0()
      {
         assertNotNull(inj);
         assertEquals(InjectableImpl.class.getName(), inj.getName());
      }
   }

   @Path("StandaloneInjectionTest.Resource2")
   public static class Resource2
   {
      @javax.inject.Inject
      javax.inject.Provider<Injectable> pInj;

      @GET
      public void m0()
      {
         assertNotNull(pInj);
         Injectable inj = pInj.get();
         assertNotNull(inj);
         assertEquals(InjectableImpl.class.getName(), inj.getName());
      }
   }

   /* ------------------------------------------------------------- */

   // Implementation of this interface used as ExoContainer component.
   public static interface InjectableComponent
   {
      String getName();
   }

   public static class InjectableComponentImpl implements InjectableComponent
   {
      @Override
      public String getName()
      {
         return getClass().getName();
      }
   }

   @Path("StandaloneInjectionTest.Resource3")
   public static class Resource3
   {
      @javax.inject.Inject
      InjectableComponent inj;

      @GET
      public void m0()
      {
         assertNotNull(inj);
         assertEquals(InjectableComponentImpl.class.getName(), inj.getName());
      }
   }

   @Path("StandaloneInjectionTest.Resource4")
   public static class Resource4
   {
      @javax.inject.Inject
      javax.inject.Provider<InjectableComponent> pInj;

      @GET
      public void m0()
      {
         assertNotNull(pInj);
         InjectableComponent inj = pInj.get();
         assertNotNull(inj);
         assertEquals(InjectableComponentImpl.class.getName(), inj.getName());
      }
   }

   /* ================================================================ */

   private final String injectableProviderKey = "StandaloneInjectionTest.Provider.Injectable";

   /**
    * @see org.everrest.exoplatform.WebAppBaseTest#setUp()
    */
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      javax.inject.Provider<Injectable> provider = new javax.inject.Provider<Injectable>() {
         @Override
         public Injectable get()
         {
            return new InjectableImpl();
         }
      };
      container.registerComponentInstance(injectableProviderKey, provider);
      container.registerComponentInstance(InjectableComponent.class, new InjectableComponentImpl());
   }

   /**
    * @see junit.framework.TestCase#tearDown()
    */
   @Override
   protected void tearDown() throws Exception
   {
      container.unregisterComponent(injectableProviderKey);
      container.unregisterComponent(InjectableComponent.class);
      super.tearDown();
   }

   public void testInjectInstance() throws Exception
   {
      resources.addResource(Resource1.class, null);
      assertEquals(204, launcher.service("GET", "/StandaloneInjectionTest.Resource1", "", null, null, null)
         .getStatus());
      resources.removeResource(Resource1.class);
   }

   public void testInjectProvider() throws Exception
   {
      resources.addResource(Resource2.class, null);
      assertEquals(204, launcher.service("GET", "/StandaloneInjectionTest.Resource2", "", null, null, null)
         .getStatus());
      resources.removeResource(Resource2.class);
   }

   /*
    * Test to inject instance to JAX-RS resource directly from ExoContainer.
    */
   public void testInjectInstance2() throws Exception
   {
      resources.addResource(Resource3.class, null);
      assertEquals(204, launcher.service("GET", "/StandaloneInjectionTest.Resource3", "", null, null, null)
         .getStatus());
      resources.removeResource(Resource3.class);
   }

   /*
    * Test to inject javax.inject.Provider to JAX-RS resource from ExoContainer.
    * ExoContainer does not have javax.inject.Provider for InjectableComponent
    * but if should be created because implementation of InjectableComponent
    * registered in container. 
    */
   public void testInjectProvider2() throws Exception
   {
      resources.addResource(Resource4.class, null);
      assertEquals(204, launcher.service("GET", "/StandaloneInjectionTest.Resource4", "", null, null, null).getStatus());
      resources.removeResource(Resource4.class);
   }

}
