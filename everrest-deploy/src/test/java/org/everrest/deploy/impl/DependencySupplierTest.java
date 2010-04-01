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
package org.everrest.deploy.impl;

import java.lang.annotation.Annotation;

import org.everrest.core.DependencySupplier;
import org.everrest.core.Parameter;
import org.everrest.deploy.impl.BaseTest;
import org.everrest.core.impl.ConstructorParameterImpl;

/**
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: DependencySupplierTest.java 436 2009-10-28 06:47:29Z aparfonov $
 */
public class DependencySupplierTest extends BaseTest
{

   public void setUp() throws Exception
   {
      super.setUp();
      setContext();
   }

   public static class Component1
   {
      // For testing DependencySupplier.
   }

   public void testDependencySupplier()
   {
      DependencySupplier depSupplier =
         (DependencySupplier)container.getComponentInstanceOfType(DependencySupplier.class);
      assertNotNull(depSupplier);
      container.registerComponentInstance(new Component1());
      Parameter t = new ConstructorParameterImpl(null, new Annotation[0], Component1.class, null, null, false);
      assertNotNull(depSupplier.getComponent(t));
   }

}
