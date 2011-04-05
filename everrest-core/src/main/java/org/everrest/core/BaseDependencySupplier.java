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

package org.everrest.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public abstract class BaseDependencySupplier implements DependencySupplier
{
   protected final Class<? extends Annotation> injectAnnotationClass;

   public BaseDependencySupplier(Class<? extends Annotation> injectAnnotationClass)
   {
      if (injectAnnotationClass == null)
         throw new IllegalArgumentException("Inject annotation class may not be null. ");
      this.injectAnnotationClass = injectAnnotationClass;
   }

   public BaseDependencySupplier()
   {
      this(javax.inject.Inject.class);
   }

   /**
    * {@inheritDoc}
    */
   public final Object getComponent(Parameter parameter)
   {
      boolean injectable = false;
      if (parameter instanceof FieldInjector)
      {
         for (Annotation a : parameter.getAnnotations())
         {
            if (injectAnnotationClass.isInstance(a))
            {
               injectable = true;
               break;
            }
         }
      }
      else
      {
         // Annotation required for fields only.
         injectable = true;
      }
      if (injectable)
      {
         Class<?> parameterClass = parameter.getParameterClass();
         if (isProvider(parameterClass))
            return getProvider(parameter.getGenericType());
         return getComponent(parameterClass);
      }
      return null;
   }

   /**
    * Check is <code>clazz</code> is javax.inject.Provider (not subclass of it).
    * 
    * @param clazz class to be checked
    * @return <code>true</code> if <code>clazz</code> is javax.inject.Provider and
    *         <code>false</code> otherwise
    */
   protected final boolean isProvider(Class<?> clazz)
   {
      return javax.inject.Provider.class == clazz;
   }

   /**
    * Get Provider of type <code>providerType</code>.
    * 
    * @param <T> type of Provider
    * @param providerType
    * @return Provider
    */
   public javax.inject.Provider<?> getProvider(Type providerType)
   {
      if (!(providerType instanceof ParameterizedType))
         throw new RuntimeException("Cannot inject provider without type parameter. ");
      final Type actualType = ((ParameterizedType)providerType).getActualTypeArguments()[0];
      Class<?> componentType;
      if (actualType instanceof Class)
         componentType = (Class<?>)actualType;
      else if (actualType instanceof ParameterizedType)
         componentType = (Class<?>)((ParameterizedType)actualType).getRawType();
      else
         throw new RuntimeException("Unsupported type " + actualType + ". ");
      final Class<?> fcomponentType = componentType;
      
      // javax.inject.Provider#get() may return null. Such behavior may be unexpected by caller.
      // May be overridden if back-end (e.g. IoC container) provides better solution. 
      return new javax.inject.Provider<Object>() {
         @Override
         public Object get()
         {
            return getComponent(fcomponentType);
         }
      };
   }
}
