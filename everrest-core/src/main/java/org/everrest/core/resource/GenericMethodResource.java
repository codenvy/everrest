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

import org.everrest.core.method.MethodParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Abstraction for method in resource, this essence is common for
 * {@link ResourceMethodDescriptor}, {@link SubResourceMethodDescriptor},
 * {@link SubResourceLocatorDescriptor} .
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public interface GenericMethodResource
{

   /**
    * @return See {@link Method}
    */
   Method getMethod();

   /**
    * @return List of method parameters
    */
   List<MethodParameter> getMethodParameters();

   /**
    * @return parent resource descriptor
    */
   AbstractResourceDescriptor getParentResource();

   /**
    * @return Java type returned by method, see {@link #getMethod()}
    */
   Class<?> getResponseType();

   /**
    * Get set or additional (not JAX-RS specific) annotation. Set of annotations
    * in implementation specific and it is not guaranteed this method will
    * return all annotations applied to the method.
    *
    * @return addition annotation
    */
   Annotation[] getAnnotations();
}
