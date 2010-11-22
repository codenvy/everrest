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

package org.everrest.spring.servlet;

import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.ApplicationPublisher;
import org.everrest.core.servlet.EverrestServletContextInitializer;
import org.everrest.spring.SpringComponentsLoader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;

/**
 * SpringComponentsLoader which obtains resources and providers delivered via
 * {@link Application} or obtained after scanning JAX-RS components if
 * Application is not configured as JAX-RS specification says.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: SpringComponentsServletContextLoader.java 88 2010-11-11
 *          11:22:12Z andrew00x $
 */
public class SpringComponentsServletContextLoader extends SpringComponentsLoader implements ServletContextAware
{

   private ServletContext servletContext;

   public SpringComponentsServletContextLoader(ResourceBinder resources, ApplicationProviderBinder providers)
   {
      super(resources, providers);
   }

   /**
    * {@inheritDoc}
    */
   public void setServletContext(ServletContext servletContext)
   {
      this.servletContext = servletContext;
   }

   @Override
   public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
   {
      super.postProcessBeanFactory(beanFactory);
      EverrestServletContextInitializer everrestInitializer = new EverrestServletContextInitializer(servletContext);
      Application application = everrestInitializer.getApplication();
      if (application != null)
         new ApplicationPublisher(getResources(), getProviders()).publish(application);
   }

}
