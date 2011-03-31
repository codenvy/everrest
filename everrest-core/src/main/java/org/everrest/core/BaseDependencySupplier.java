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
            return getProvider(parameterClass, parameter.getGenericType());
         return getComponent(parameterClass/*, parameter.getGenericType()*/);
      }
      return null;
   }

   /**
    * Check is <code>clazz</code> is Provider (not subclass of it),
    * e.g. javax.inject.Provider. Override it if other type of Provider is in use.
    * 
    * @param clazz class to be checked
    * @return <code>true</code> if <code>clazz</code> is Provider and
    *         <code>false</code> otherwise
    */
   protected boolean isProvider(Class<?> clazz)
   {
      return javax.inject.Provider.class == clazz;
   }

   /**
    * Provider for type <code>parameterClass</code>.
    * 
    * @param <T> type of Provider
    * @param parameterClass 
    * @param genericType
    * @return Provider
    */
   protected <T> javax.inject.Provider<T> getProvider(Class<T> parameterClass, Type genericType)
   {
      if (genericType == null || !(genericType instanceof ParameterizedType))
         throw new RuntimeException("Cannot inject provider without type parameter. ");

      final Type actualType = ((ParameterizedType)genericType).getActualTypeArguments()[0];
      Class<?> rawType;
      if (actualType instanceof Class)
         rawType = (Class<?>)actualType;
      else if (actualType instanceof ParameterizedType)
         rawType = (Class<?>)((ParameterizedType)actualType).getRawType();
      else
         // TODO improve
         throw new RuntimeException("Unsupported type " + actualType + ". ");
      final Class<?> frawType = rawType;

      return new javax.inject.Provider<T>()
      {
         @SuppressWarnings("unchecked")
         @Override
         public T get()
         {
            return (T)getComponent(frawType/*, actualType*/);
         }
      };
   }

   //   /**
   //    * Get instance of type <code>parameterClass</code>, e.g. obtain instance
   //    * from IoC container. NOTE : In this implementation only <code>parameterClass</code>
   //    * used to find appropriate instance, see {@link #getComponent(Class)}.
   //    * 
   //    * @param <T> type of component
   //    * @param parameterClass
   //    * @param genericType
   //    * @return instance of type <code>parameterClass</code> or <code>null</code>
   //    *         if there is no instances for such type
   //    * @throws RuntimeException if instance cannot be produced cause to error
   //    *         while providing an instance
   //    */
   //   @SuppressWarnings("unchecked")
   //   protected <T> T getComponent(Class<T> parameterClass, Type genericType)
   //   {
   //      return (T)getComponent(parameterClass);
   //   }
}
