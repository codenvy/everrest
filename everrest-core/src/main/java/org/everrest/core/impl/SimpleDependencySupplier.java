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

import org.everrest.core.DependencySupplier;
import org.everrest.core.FieldInjector;
import org.everrest.core.Inject;
import org.everrest.core.Parameter;

import java.lang.annotation.Annotation;
import java.util.HashMap;

/**
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: SimpleDependencySupplier.java -1 $
 */
public class SimpleDependencySupplier extends HashMap<Class<?>, Object> implements DependencySupplier
{

   private static final long serialVersionUID = 8212609178539168377L;

   /**
    * {@inheritDoc}
    */
   public Object getComponent(Parameter parameter)
   {
      if (parameter instanceof FieldInjector)
      {
         for (Annotation a : parameter.getAnnotations())
         {
            // Do not process fields without annotation Inject
            if (a.annotationType() == Inject.class)
            {
               return get(parameter.getParameterClass());
            }
         }
      }
      return get(parameter.getParameterClass());
   }

}
