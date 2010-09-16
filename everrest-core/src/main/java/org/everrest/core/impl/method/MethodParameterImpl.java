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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Describes the method's parameter.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: MethodParameterImpl.java 285 2009-10-15 16:21:30Z aparfonov $
 */
public class MethodParameterImpl implements org.everrest.core.method.MethodParameter
{

   /**
    * External annotations for parameter, external it mind some other then
    * contains in {@link ParameterHelper#METHOD_PARAMETER_ANNOTATIONS_MAP}.
    */
   private final Annotation[] additional;

   /**
    * One of annotations from
    * {@link ParameterHelper#METHOD_PARAMETER_ANNOTATIONS_MAP}.
    */
   private final Annotation annotation;

   /**
    * Parameter type. See
    * {@link java.lang.reflect.Method#getGenericParameterTypes()} .
    */
   private final Type type;

   /**
    * Parameter class. See {@link java.lang.reflect.Method#getParameterTypes()}
    */
   private final Class<?> clazz;

   /**
    * Default value for this parameter, default value can be used if there is
    * not found required parameter in request. See
    * {@link javax.ws.rs.DefaultValue}.
    */
   private final String defaultValue;

   /**
    * See {@link javax.ws.rs.Encoded}.
    */
   private final boolean encoded;

   /**
    * Constructs new instance of MethodParameter.
    * 
    * @param annotation see {@link #annotation}
    * @param additional see {@link #additional}
    * @param clazz parameter class
    * @param type generic parameter type
    * @param defaultValue default value for parameter. See
    *        {@link javax.ws.rs.DefaultValue}.
    * @param encoded true if parameter must not be decoded false otherwise
    */
   public MethodParameterImpl(Annotation annotation, Annotation[] additional, Class<?> clazz, Type type,
      String defaultValue, boolean encoded)
   {
      this.annotation = annotation;
      this.additional = additional;
      this.clazz = clazz;
      this.type = type;
      this.defaultValue = defaultValue;
      this.encoded = encoded;
   }

   /**
    * {@inheritDoc}
    */
   public Annotation[] getAnnotations()
   {
      return additional;
   }

   /**
    * {@inheritDoc}
    */
   public Annotation getAnnotation()
   {
      return annotation;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isEncoded()
   {
      return encoded;
   }

   /**
    * {@inheritDoc}
    */
   public String getDefaultValue()
   {
      return defaultValue;
   }

   /**
    * {@inheritDoc}
    */
   public Type getGenericType()
   {
      return type;
   }

   /**
    * {@inheritDoc}
    */
   public Class<?> getParameterClass()
   {
      return clazz;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder("[ MethodParameter: ");
      sb.append("annotation: " + getAnnotation()).append("; type: " + getParameterClass()).append(
         "; generic-type: " + getGenericType()).append("; default-value: " + getDefaultValue()).append(
         "; encoded: " + isEncoded()).append(" ]");
      return sb.toString();
   }

}
