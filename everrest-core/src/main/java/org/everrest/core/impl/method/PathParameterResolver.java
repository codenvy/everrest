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

import org.everrest.core.ApplicationContext;
import org.everrest.core.method.TypeProducer;

import javax.ws.rs.PathParam;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: PathParameterResolver.java 285 2009-10-15 16:21:30Z aparfonov $
 */
public class PathParameterResolver extends ParameterResolver<PathParam>
{

   /**
    * See {@link PathParam}.
    */
   private final PathParam pathParam;

   /**
    * @param pathParam PathParam
    */
   PathParameterResolver(PathParam pathParam)
   {
      this.pathParam = pathParam;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object resolve(org.everrest.core.Parameter parameter, ApplicationContext context)
      throws Exception
   {
      String param = this.pathParam.value();
      TypeProducer typeProducer =
         ParameterHelper.createTypeProducer(parameter.getParameterClass(), parameter.getGenericType());
      return typeProducer.createValue(param, context.getPathParameters(!parameter.isEncoded()), parameter
         .getDefaultValue());
   }

}
