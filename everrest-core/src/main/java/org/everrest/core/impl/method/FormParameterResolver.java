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
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.method.TypeProducer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: FormParameterResolver.java 285 2009-10-15 16:21:30Z aparfonov $
 */
public class FormParameterResolver extends ParameterResolver<FormParam>
{

   /**
    * Form generic type.
    */
   private static final Type FORM_TYPE = (ParameterizedType)MultivaluedMapImpl.class.getGenericInterfaces()[0];

   /**
    * See {@link FormParam}.
    */
   private final FormParam formParam;

   /**
    * @param formParam FormParam
    */
   FormParameterResolver(FormParam formParam)
   {
      this.formParam = formParam;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public Object resolve(org.everrest.core.Parameter parameter, ApplicationContext context) throws Exception
   {
      String param = this.formParam.value();
      TypeProducer typeProducer =
         ParameterHelper.createTypeProducer(parameter.getParameterClass(), parameter.getGenericType());

      MediaType conetentType = context.getHttpHeaders().getMediaType();
      MessageBodyReader reader =
         context.getProviders().getMessageBodyReader(MultivaluedMap.class, FORM_TYPE, null, conetentType);
      if (reader == null)
         throw new IllegalStateException("Can't find appropriate entity reader for entity type "
            + MultivaluedMap.class.getName() + " and content-type " + conetentType);

      MultivaluedMap<String, String> form =
         (MultivaluedMap<String, String>)reader.readFrom(MultivaluedMap.class, FORM_TYPE, null, conetentType, context
            .getHttpHeaders().getRequestHeaders(), context.getContainerRequest().getEntityStream());
      return typeProducer.createValue(param, form, parameter.getDefaultValue());
   }

}
