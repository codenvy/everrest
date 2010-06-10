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

import org.everrest.core.ComponentLifecycleScope;
import org.everrest.core.ConstructorDescriptor;
import org.everrest.core.FieldInjector;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.impl.resource.PathValue;
import org.everrest.core.resource.ResourceDescriptorVisitor;
import org.everrest.core.uri.UriPattern;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.Path;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: FilterDescriptorImpl.java -1 $
 */
public class FilterDescriptorImpl implements FilterDescriptor
{

   /**
    * Filter class.
    */
   private final Class<?> filterClass;

   /**
    * @see PathValue
    */
   private final PathValue path;

   /**
    * @see UriPattern
    */
   private final UriPattern uriPattern;

   /**
    * Filter class constructors.
    * 
    * @see ConstructorDescriptor
    */
   private final List<ConstructorDescriptor> constructors;

   /**
    * Filter class fields.
    */
   private final List<FieldInjector> fields;

   /**
    * @param filterClass filter class
    * @param scope filter scope
    * @see ComponentLifecycleScope
    */
   public FilterDescriptorImpl(Class<?> filterClass, ComponentLifecycleScope scope)
   {
      final Path p = filterClass.getAnnotation(Path.class);
      if (p != null)
      {
         this.path = new PathValue(p.value());
         this.uriPattern = new UriPattern(p.value());
      }
      else
      {
         this.path = null;
         this.uriPattern = null;
      }

      this.filterClass = filterClass;

      this.constructors = new ArrayList<ConstructorDescriptor>();
      this.fields = new ArrayList<FieldInjector>();
      if (scope == ComponentLifecycleScope.PER_REQUEST)
      {
         for (Constructor<?> constructor : filterClass.getConstructors())
         {
            constructors.add(new ConstructorDescriptorImpl(filterClass, constructor));
         }
         if (constructors.size() == 0)
         {
            String msg = "Not found accepted constructors for filter class " + filterClass.getName();
            throw new RuntimeException(msg);
         }
         // Sort constructors in number parameters order
         if (constructors.size() > 1)
         {
            Collections.sort(constructors, ConstructorDescriptorImpl.CONSTRUCTOR_COMPARATOR);
         }
         // process field
         for (java.lang.reflect.Field jfield : filterClass.getDeclaredFields())
         {
            fields.add(new FieldInjectorImpl(filterClass, jfield));
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void accept(ResourceDescriptorVisitor visitor)
   {
      visitor.visitFilterDescriptor(this);
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
   public Class<?> getObjectClass()
   {
      return filterClass;
   }

   /**
    * {@inheritDoc}
    */
   public PathValue getPathValue()
   {
      return path;
   }

   /**
    * {@inheritDoc}
    */
   public UriPattern getUriPattern()
   {
      return uriPattern;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuffer sb = new StringBuffer("[ FilterDescriptorImpl: ");
      sb.append("path: " + getPathValue() + "; ").append("filter class: " + getObjectClass() + "; ").append(
         getConstructorDescriptors() + "; ").append(getFieldInjectors()).append(" ]");
      return sb.toString();
   }

}
