/*
 * Copyright (C) 2011 eXo Platform SAS.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Implementation of LifecycleComponent.LifecycleMethodStrategy that uses {@link PostConstruct} and {@link PreDestroy}
 * annotation to find "initialize" and "destroy" methods.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public final class AnnotatedLifecycleMethodStrategy implements LifecycleComponent.LifecycleMethodStrategy
{
   private static class MethodFilter
   {
      private final Class<? extends Annotation> annotation;

      MethodFilter(Class<? extends Annotation> annotation)
      {
         this.annotation = annotation;
      }

      /**
       * Check is method may be used as PostConstruct/PreDestroy method. There are some limitations according to
       * requirements usage of {@link PostConstruct} and {@link PreDestroy} annotation :
       * <ul>
       * <li>Method is annotated with {@link #annotation}.</li>
       * <li>Method must not be static.</li>
       * <li>Method has not have any parameters.</li>
       * <li>The return type of the method must be void.</li>
       * <li>Method must not throw checked exception.</li>
       * </ul>
       * 
       * @param m the method
       * @return <code>true</code> if method is matched to requirements above and false otherwise
       * @see PostConstruct
       * @see PreDestroy
       */
      boolean accept(Method m)
      {
         return m.isAnnotationPresent(annotation) //
            && (!Modifier.isStatic(m.getModifiers())) //
            && m.getParameterTypes().length == 0 //
            && (m.getReturnType() == void.class || m.getReturnType() == Void.class) //
            && noCheckedException(m);
      }

      private boolean noCheckedException(Method m)
      {
         Class<?>[] exceptions = m.getExceptionTypes();
         if (exceptions.length > 0)
         {
            for (int i = 0; i < exceptions.length; i++)
            {
               if (!RuntimeException.class.isAssignableFrom(exceptions[i]))
                  return false;
            }
         }
         return true;
      }
   }

   private static final MethodFilter POSTCONSTRUCT_METHOD_FILTER = new MethodFilter(PostConstruct.class);
   private static final MethodFilter PREDESTROY_METHOD_FILTER = new MethodFilter(PreDestroy.class);

   /**
    * @see org.everrest.core.impl.LifecycleComponent.LifecycleMethodStrategy#invokeInitializeMethods(java.lang.Object)
    */
   @Override
   public void invokeInitializeMethods(Object o)
   {
      doInvokeLifecycleMethods(o, POSTCONSTRUCT_METHOD_FILTER);
   }

   /**
    * @see org.everrest.core.impl.LifecycleComponent.LifecycleMethodStrategy#invokeDestroyMethods(java.lang.Object)
    */
   @Override
   public void invokeDestroyMethods(Object o)
   {
      doInvokeLifecycleMethods(o, PREDESTROY_METHOD_FILTER);
   }

   @SuppressWarnings("rawtypes")
   private void doInvokeLifecycleMethods(Object o, MethodFilter filter)
   {
      for (Class cl = o.getClass(); cl != Object.class; cl = cl.getSuperclass())
      {
         Method[] methods = cl.getDeclaredMethods();
         for (int i = 0; i < methods.length; i++)
         {
            Method method = methods[i];
            if (filter.accept(method))
            {
               try
               {
                  if (!Modifier.isPublic(method.getModifiers()))
                     method.setAccessible(true);
                  method.invoke(o);
               }
               catch (IllegalArgumentException e)
               {
                  throw new InternalException(e);
               }
               catch (IllegalAccessException e)
               {
                  throw new InternalException(e);
               }
               catch (SecurityException e)
               {
                  throw new InternalException(e);
               }
               catch (InvocationTargetException e)
               {
                  Throwable t = e.getTargetException();
                  throw new InternalException(t);
               }
            }
         }
      }
   }
}
