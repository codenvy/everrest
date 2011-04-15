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
package org.everrest.core.impl;

import junit.framework.TestCase;

import org.everrest.core.ObjectFactory;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.tools.DependencySupplierImpl;
import org.everrest.core.tools.ResourceLauncher;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public abstract class BaseTest extends TestCase
{

   protected ProviderBinder providers;

   protected ResourceBinderImpl resources;

   protected RequestHandlerImpl requestHandler;

   protected ResourceLauncher launcher;

   public void setUp() throws Exception
   {
      resources = new ResourceBinderImpl();
      // reset embedded providers to be sure it is clean
      ProviderBinder.setInstance(new ProviderBinder());
      providers = new ApplicationProviderBinder();
      requestHandler =
         new RequestHandlerImpl(new RequestDispatcher(resources), providers, new DependencySupplierImpl(),
            new EverrestConfiguration());
      launcher = new ResourceLauncher(requestHandler);
   }

   protected void setContext()
   {
      ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, providers));
   }

   public void tearDown() throws Exception
   {
   }

   public void registry(Object resource) throws Exception
   {
      resources.addResource(resource, null);
   }

   public void registry(Class<?> resourceClass) throws Exception
   {
      resources.addResource(resourceClass, null);
   }

   public ObjectFactory<AbstractResourceDescriptor> unregistry(Object resource)
   {
      return resources.removeResource(resource.getClass());
   }

   public ObjectFactory<AbstractResourceDescriptor> unregistry(Class<?> resourceClass)
   {
      return resources.removeResource(resourceClass);
   }

}
