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

import org.exoplatform.services.log.LogConfigurator;
import org.exoplatform.services.log.impl.Log4JConfigurator;

import java.util.Properties;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public abstract class BaseTest extends TestCase
{

   protected ProviderBinder providers;

   protected ResourceBinderImpl resources;

   protected RequestHandlerImpl requestHandler;

   public void setUp() throws Exception
   {
      resources = new ResourceBinderImpl();
      requestHandler = new RequestHandlerImpl(resources, new SimpleDependencySupplier());

      // reset providers to be sure it is clean
      ProviderBinder.setInstance(new ProviderBinder());
      providers = ProviderBinder.getInstance();

      LogConfigurator lc = new Log4JConfigurator();
      Properties props = new Properties();
      props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("conf/log4j.properties"));
      lc.configure(props);
   }

   protected void setContext()
   {
      ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, providers));
   }

   public void tearDown() throws Exception
   {
   }

   public boolean registry(Object resource) throws Exception
   {
      return resources.bind(resource);
   }

   public boolean registry(Class<?> resourceClass) throws Exception
   {
      return resources.bind(resourceClass);
   }

   public boolean unregistry(Object resource)
   {
      return resources.unbind(resource.getClass());
   }

   public boolean unregistry(Class<?> resourceClass)
   {
      return resources.unbind(resourceClass);
   }

}
