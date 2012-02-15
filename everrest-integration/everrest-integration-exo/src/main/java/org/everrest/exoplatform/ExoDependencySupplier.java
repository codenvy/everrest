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
package org.everrest.exoplatform;

import org.everrest.core.BaseDependencySupplier;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.picocontainer.ComponentAdapter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

/**
 * Get instance of requested type from ExoContainer.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class ExoDependencySupplier extends BaseDependencySupplier
{
   /** @see org.everrest.core.BaseDependencySupplier#getProvider(java.lang.reflect.Type) */
   @Override
   public javax.inject.Provider<?> getProvider(Type providerType)
   {
      if (!(providerType instanceof ParameterizedType))
      {
         throw new RuntimeException("Cannot inject provider without type parameter. ");
      }
      Type actualType = ((ParameterizedType)providerType).getActualTypeArguments()[0];
      return getProvider(ExoContainerContext.getCurrentContainer(), actualType);
   }

   @SuppressWarnings({"rawtypes"})
   private javax.inject.Provider<?> getProvider(final ExoContainer container, final Type entryType)
   {
      List injectionProviders = container.getComponentInstancesOfType(javax.inject.Provider.class);
      if (injectionProviders != null && !injectionProviders.isEmpty())
      {
         for (Iterator i = injectionProviders.iterator(); i.hasNext(); )
         {
            javax.inject.Provider provider = (javax.inject.Provider)i.next();
            try
            {
               Type injectedType = provider.getClass().getMethod("get").getGenericReturnType();
               if (entryType.equals(injectedType))
               {
                  return provider;
               }
            }
            catch (NoSuchMethodException ignored)
            {
               // Never happen since class implements javax.inject.Provider.
            }
         }
      }

      // Create javax.inject.Provider if instance of requested type may be produced by ExoContainer.
      if (entryType instanceof Class<?>)
      {
         final ComponentAdapter componentAdapter = container.getComponentAdapterOfType((Class<?>)entryType);
         if (componentAdapter != null)
         {
            return new javax.inject.Provider<Object>()
            {
               @Override
               public Object get()
               {
                  return componentAdapter.getComponentInstance(container);
               }
            };
         }
      }

      return null;
   }

   /** @see org.everrest.core.DependencySupplier#getComponent(java.lang.Class) */
   @Override
   public Object getComponent(Class<?> type)
   {
      javax.inject.Provider<?> provider = getProvider(ExoContainerContext.getCurrentContainer(), type);
      if (provider != null)
      {
         return provider.get();
      }
      return null;
   }

   /** @see org.everrest.core.BaseDependencySupplier#getComponentByName(java.lang.String) */
   @Override
   public Object getComponentByName(String name)
   {
      return ExoContainerContext.getCurrentContainer().getComponentInstance(name);
   }
}
