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
package org.everrest.core.resource;

import org.everrest.core.ConstructorDescriptor;
import org.everrest.core.FieldInjector;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.provider.ProviderDescriptor;

/**
 * Can be used for validation next resource descriptors
 * {@link AbstractResourceDescriptor}, {@link ResourceMethodDescriptor},
 * {@link SubResourceMethodDescriptor}, {@link SubResourceLocatorDescriptor},
 * {@link ConstructorDescriptor}, {@link FieldInjector},
 * {@link ProviderDescriptor}, {@link FilterDescriptor}.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ResourceDescriptorVisitor.java 285 2009-10-15 16:21:30Z aparfonov $
 */
public interface ResourceDescriptorVisitor
{

   /**
    * @param ard See {@link AbstractResourceDescriptor}
    */
   void visitAbstractResourceDescriptor(AbstractResourceDescriptor ard);

   /**
    * @param rmd See {@link ResourceMethodDescriptor}
    */
   void visitResourceMethodDescriptor(ResourceMethodDescriptor rmd);

   /**
    * @param srmd See {@link SubResourceMethodDescriptor}
    */
   void visitSubResourceMethodDescriptor(SubResourceMethodDescriptor srmd);

   /**
    * @param srld See {@link SubResourceLocatorDescriptor}
    */
   void visitSubResourceLocatorDescriptor(SubResourceLocatorDescriptor srld);

   /**
    * @param ci ConstructorInjector
    */
   void visitConstructorInjector(ConstructorDescriptor ci);

   /**
    * @param fi FieldInjector
    */
   void visitFieldInjector(FieldInjector fi);

   /**
    * @param pd ProviderDescriptor
    */
   void visitProviderDescriptor(ProviderDescriptor pd);

   /**
    * @param fd FilterDescriptor
    */
   void visitFilterDescriptor(FilterDescriptor fd);

}
