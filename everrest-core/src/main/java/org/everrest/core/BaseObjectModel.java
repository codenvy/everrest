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
package org.everrest.core;

import org.everrest.core.impl.ConstructorDescriptorImpl;
import org.everrest.core.impl.FieldInjectorImpl;
import org.everrest.core.impl.MultivaluedMapImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

public abstract class BaseObjectModel implements ObjectModel
{

   protected final Class<?> clazz;

   /** Optional data. */
   protected MultivaluedMapImpl properties;

   /** Resource class constructors. */
   protected final List<ConstructorDescriptor> constructors;

   /** Resource class fields. */
   protected final List<FieldInjector> fields;

   public BaseObjectModel(Class<?> clazz, ComponentLifecycleScope scope)
   {
      this.clazz = clazz;
      this.constructors = new ArrayList<ConstructorDescriptor>();
      this.fields = new ArrayList<FieldInjector>();
      if (scope == ComponentLifecycleScope.PER_REQUEST)
      {
         for (Constructor<?> constructor : clazz.getConstructors())
         {
            constructors.add(new ConstructorDescriptorImpl(clazz, constructor));
         }
         if (constructors.size() == 0)
         {
            String msg = "Not found accepted constructors for provider class " + clazz.getName();
            throw new RuntimeException(msg);
         }
         // Sort constructors in number parameters order
         if (constructors.size() > 1)
         {
            Collections.sort(constructors, ConstructorDescriptorImpl.CONSTRUCTOR_COMPARATOR);
         }
         // process field
         for (java.lang.reflect.Field jfield : clazz.getDeclaredFields())
         {
            fields.add(new FieldInjectorImpl(clazz, jfield));
         }
         Package _package = clazz.getPackage();
         String resourcePackageName = _package != null ? _package.getName() : null;
         Class<?> sc = clazz.getSuperclass();
         while (sc != null && sc != Object.class)
         {
            for (java.lang.reflect.Field jfield : sc.getDeclaredFields())
            {
               int modif = jfield.getModifiers();
               Package package1 = clazz.getPackage();
               String scPackageName = package1 != null ? package1.getName() : null;
               if (!Modifier.isPrivate(modif))
               {
                  if (Modifier.isPublic(modif)
                     || Modifier.isProtected(modif)
                     || (!Modifier.isPrivate(modif) && ((resourcePackageName == null && scPackageName == null)
                     || (resourcePackageName != null && resourcePackageName.equals(scPackageName)))))
                  {
                     FieldInjector inj = new FieldInjectorImpl(clazz, jfield);
                     // Skip not annotated field. They will be not injected from container.
                     if (inj.getAnnotation() != null)
                     {
                        fields.add(new FieldInjectorImpl(clazz, jfield));
                     }
                  }
               }
            }
            sc = sc.getSuperclass();
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public Class<?> getObjectClass()
   {
      return clazz;
   }

   /**
    * {@inheritDoc}
    */
   public List<ConstructorDescriptor> getConstructorDescriptors()
   {
      return constructors;
   }

   /**
    * {@inheritDoc}
    */
   public List<FieldInjector> getFieldInjectors()
   {
      return fields;
   }

   /**
    * {@inheritDoc}
    */
   public MultivaluedMap<String, String> getProperties()
   {
      if (properties == null)
      {
         properties = new MultivaluedMapImpl();
      }
      return properties;
   }

   /**
    * {@inheritDoc}
    */
   public List<String> getProperty(String key)
   {
      if (properties != null)
      {
         return properties.get(key);
      }
      return null;
   }

}
