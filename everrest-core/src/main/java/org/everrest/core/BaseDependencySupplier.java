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

   protected final DependencyNameDetector nameDetector;

   public BaseDependencySupplier(Class<? extends Annotation> injectAnnotationClass, DependencyNameDetector nameDetector)
   {
      if (injectAnnotationClass == null)
      {
         throw new IllegalArgumentException("Inject annotation class may not be null. ");
      }
      this.injectAnnotationClass = injectAnnotationClass;
      this.nameDetector = nameDetector;
   }

   public BaseDependencySupplier(Class<? extends Annotation> injectAnnotationClass)
   {
      if (injectAnnotationClass == null)
      {
         throw new IllegalArgumentException("Inject annotation class may not be null. ");
      }
      this.injectAnnotationClass = injectAnnotationClass;
      nameDetector = null;
   }

   public BaseDependencySupplier()
   {
      this(javax.inject.Inject.class, new DependencyNameDetector()
      {
         @Override
         public String getName(Parameter parameter)
         {
            for (Annotation a : parameter.getAnnotations())
            {
               if (javax.inject.Named.class.isInstance(a))
               {
                  String name = ((javax.inject.Named)a).value();
                  if (!name.isEmpty())
                  {
                     return name;
                  }
               }
            }
            return null;
         }
      });
   }

   /** {@inheritDoc} */
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
         String name = nameDetector != null ? nameDetector.getName(parameter) : null;
         if (name != null)
         {
            return getComponentByName(name);
         }
         Class<?> parameterClass = parameter.getParameterClass();
         if (isProvider(parameterClass))
         {
            return getProvider(parameter.getGenericType());
         }
         return getComponent(parameterClass);
      }
      return null;
   }

   /**
    * Get instance of dependency by name. This is optional capability so by default this method returns
    * <code>null</code>.
    * Override it if back-end (e.g. IoC container) supports getting components by key (name).
    *
    * @param name of dependency
    * @return object of required type or null if instance described by
    *         <code>name</code> may not be produced
    * @throws RuntimeException if any error occurs while creating instance
    * of <code>name</code>
    */
   public Object getComponentByName(String name)
   {
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
    * @param providerType parameterized javax.inject.Provider type
    * @return Provider the instance of javax.inject.Provider for specified <code>providerType</code>
    */
   public javax.inject.Provider<?> getProvider(Type providerType)
   {
      if (!(providerType instanceof ParameterizedType))
      {
         throw new RuntimeException("Cannot inject provider without type parameter. ");
      }
      if (((ParameterizedType)providerType).getRawType() != javax.inject.Provider.class)
      {
         throw new RuntimeException("Type " + providerType + " is not javax.inject.Provider. ");
      }
      final Type actualType = ((ParameterizedType)providerType).getActualTypeArguments()[0];
      final Class<?> componentType;
      if (actualType instanceof Class)
      {
         componentType = (Class<?>)actualType;
      }
      else if (actualType instanceof ParameterizedType)
      {
         componentType = (Class<?>)((ParameterizedType)actualType).getRawType();
      }
      else
      {
         throw new RuntimeException("Unsupported type " + actualType + ". ");
      }

      // javax.inject.Provider#get() may return null. Such behavior may be unexpected by caller.
      // May be overridden if back-end (e.g. IoC container) provides better solution. 
      return new javax.inject.Provider<Object>()
      {
         @Override
         public Object get()
         {
            return getComponent(componentType);
         }
      };
   }
}
