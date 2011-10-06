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

import junit.framework.TestCase;

import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.RuntimeDelegateImpl;
import org.exoplatform.container.StandaloneContainer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import javax.ws.rs.ext.RuntimeDelegate;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public abstract class BaseTest extends TestCase
{
   protected StandaloneContainer container;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
      
      // reset set of providers for each test 
      Constructor<ProviderBinder> c = ProviderBinder.class.getDeclaredConstructor();
      c.setAccessible(true);
      ProviderBinder.setInstance(c.newInstance());

      String conf = getClass().getResource("/conf/test-configuration.xml").toString();
      StandaloneContainer.setConfigurationURL(conf);
      container = StandaloneContainer.getInstance();
   }

   @Override
   protected void tearDown() throws Exception
   {
      container.stop();
      Field containerField = StandaloneContainer.class.getDeclaredField("container");
      containerField.setAccessible(true);
      containerField.set(null, null);
      container = null;
      super.tearDown();
   }
}
