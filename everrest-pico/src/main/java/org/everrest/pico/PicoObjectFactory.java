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

package org.everrest.pico;

import org.everrest.core.ApplicationContext;
import org.everrest.core.ObjectFactory;
import org.everrest.core.ObjectModel;
import org.picocontainer.web.PicoServletContainerFilter.ServletFilter;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class PicoObjectFactory<T extends ObjectModel> implements ObjectFactory<T>
{

   protected final T model;

   public PicoObjectFactory(T model)
   {
      this.model = model;
   }

   /**
    * {@inheritDoc}
    */
   public Object getInstance(ApplicationContext context)
   {
      Class<?> clazz = model.getObjectClass();
      Object object = ServletFilter.getRequestComponentForThread(clazz);
      /*for (FieldInjector field : model.getFieldInjectors())
      {
         field.inject(object, context);
      }*/
      return object;
   }

   /**
    * {@inheritDoc}
    */
   public T getObjectModel()
   {
      return model;
   }

}
