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

package org.everrest.spring;

import org.everrest.core.BaseDependencySupplier;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * Implementation of DependencySupplier that obtain dependencies from Spring IoC
 * container.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: SpringDependencySupplier.java 88 2010-11-11 11:22:12Z andrew00x
 *          $
 */
public final class SpringDependencySupplier extends BaseDependencySupplier implements BeanFactoryAware
{
   private BeanFactory beanFactory;

   @Override
   public Object getComponent(Class<?> type)
   {
      try
      {
         return beanFactory.getBean(type);
      }
      catch (NoSuchBeanDefinitionException be)
      {
         return null;
      }
   }

   @Override
   public Object getComponentByName(String name)
   {
      try
      {
         return beanFactory.getBean(name);
      }
      catch (NoSuchBeanDefinitionException be)
      {
         return null;
      }
   }

   @Override
   public void setBeanFactory(BeanFactory beanFactory) throws BeansException
   {
      this.beanFactory = beanFactory;
   }
}
