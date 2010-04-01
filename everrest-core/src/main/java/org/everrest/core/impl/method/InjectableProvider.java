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
package org.everrest.core.impl.method;

import org.everrest.core.ApplicationContext;
import org.everrest.core.Inject;
import org.everrest.core.Parameter;

/**
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: InjectableProvider.java 436 2009-10-28 06:47:29Z aparfonov $
 * @deprecated
 */
public class InjectableProvider extends ParameterResolver<Inject>
{

   InjectableProvider(Inject inject)
   {
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object resolve(Parameter parameter, ApplicationContext context) throws Exception
   {
      if (context.getDependencySupplier() != null)
         return context.getDependencySupplier().getComponent(parameter);

      return null;
   }

}
