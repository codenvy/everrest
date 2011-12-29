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

import org.everrest.core.BaseObjectModel;
import org.everrest.core.ComponentLifecycleScope;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.impl.resource.PathValue;
import org.everrest.core.resource.ResourceDescriptorVisitor;
import org.everrest.core.uri.UriPattern;

import javax.ws.rs.Path;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class FilterDescriptorImpl extends BaseObjectModel implements FilterDescriptor
{

   /** @see PathValue */
   private final PathValue path;

   /** @see UriPattern */
   private final UriPattern uriPattern;

   /**
    * @param filterClass filter class
    * @param scope filter scope
    * @see ComponentLifecycleScope
    */
   public FilterDescriptorImpl(Class<?> filterClass, ComponentLifecycleScope scope)
   {
      super(filterClass, scope);
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
      StringBuilder sb = new StringBuilder("[ FilterDescriptorImpl: ");
      sb.append("path: ");
      sb.append(getPathValue());
      sb.append("; filter class: ");
      sb.append(getObjectClass());
      sb.append("; ");
      sb.append(getConstructorDescriptors());
      sb.append("; ");
      sb.append(getFieldInjectors());
      sb.append(" ]");
      return sb.toString();
   }

}
