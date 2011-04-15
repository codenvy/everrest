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
package org.everrest.core.impl.method;

import org.everrest.core.Property;

import java.lang.annotation.Annotation;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ParameterResolverFactory.java 292 2009-10-19 07:03:07Z
 *          aparfonov $
 */
public final class ParameterResolverFactory
{

   /** Constructor. */
   private ParameterResolverFactory()
   {
   }

   /**
    * Create parameter resolver for supplied annotation.
    * 
    * @param annotation JAX-RS annotation
    * @return ParameterResolver
    */
   @SuppressWarnings("rawtypes")
   public static ParameterResolver createParameterResolver(Annotation annotation)
   {
      Class clazz = annotation.annotationType();
      if (clazz == CookieParam.class)
         return new CookieParameterResolver((CookieParam)annotation);
      if (clazz == Context.class)
         return new ContextParameterResolver((Context)annotation);
      if (clazz == FormParam.class)
         return new FormParameterResolver((FormParam)annotation);
      if (clazz == HeaderParam.class)
         return new HeaderParameterResolver((HeaderParam)annotation);
      if (clazz == MatrixParam.class)
         return new MatrixParameterResolver((MatrixParam)annotation);
      if (clazz == PathParam.class)
         return new PathParameterResolver((PathParam)annotation);
      if (clazz == QueryParam.class)
         return new QueryParameterResolver((QueryParam)annotation);
      if (clazz == Property.class)
         return new PropertyResolver((Property)annotation);
      //      if (clazz == Inject.class)
      //         return new InjectableProvider((Inject)annotation);
      return null;
   }

}
