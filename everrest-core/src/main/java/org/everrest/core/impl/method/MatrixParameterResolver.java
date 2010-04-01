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

import java.util.List;

import javax.ws.rs.MatrixParam;
import javax.ws.rs.core.PathSegment;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: MatrixParameterResolver.java 285 2009-10-15 16:21:30Z aparfonov $
 */
public class MatrixParameterResolver extends ParameterResolver<MatrixParam>
{

   /**
    * See {@link MatrixParam}.
    */
   private final MatrixParam matrixParam;

   /**
    * @param matrixParam MatrixParam
    */
   MatrixParameterResolver(MatrixParam matrixParam)
   {
      this.matrixParam = matrixParam;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object resolve(org.everrest.core.Parameter parameter, ApplicationContext context)
      throws Exception
   {
      String param = matrixParam.value();
      TypeProducer typeProducer =
         ParameterHelper.createTypeProducer(parameter.getParameterClass(), parameter.getGenericType());
      List<PathSegment> pathSegments = context.getUriInfo().getPathSegments(!parameter.isEncoded());

      PathSegment pathSegment = pathSegments.get(pathSegments.size() - 1);

      return typeProducer.createValue(param, pathSegment.getMatrixParameters(), parameter.getDefaultValue());
   }

}
