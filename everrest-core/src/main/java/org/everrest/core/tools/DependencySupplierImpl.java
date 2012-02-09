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

package org.everrest.core.tools;

import org.everrest.core.BaseDependencySupplier;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple dependency resolver.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class DependencySupplierImpl extends BaseDependencySupplier
{
   private final Map<Class<?>, Object> dependencies = new HashMap<Class<?>, Object>();

   public DependencySupplierImpl()
   {
   }

   public DependencySupplierImpl(Class<? extends Annotation> injectAnnotation)
   {
      super(injectAnnotation);
   }

   public void addComponent(Class<?> key, Object instance)
   {
      dependencies.put(key, instance);
   }

   /** @see org.everrest.core.DependencySupplier#getComponent(java.lang.Class) */
   @Override
   public Object getComponent(Class<?> type)
   {
      return dependencies.get(type);
   }
}
