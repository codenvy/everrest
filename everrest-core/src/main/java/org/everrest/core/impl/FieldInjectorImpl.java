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

import org.everrest.common.util.Logger;
import org.everrest.core.ApplicationContext;
import org.everrest.core.FieldInjector;
import org.everrest.core.impl.method.ParameterHelper;
import org.everrest.core.impl.method.ParameterResolver;
import org.everrest.core.impl.method.ParameterResolverFactory;
import org.everrest.core.resource.ResourceDescriptorVisitor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: FieldInjectorImpl.java -1 $
 */
public class FieldInjectorImpl implements FieldInjector
{

   /**
    * Logger.
    */
   private static final Logger LOG = Logger.getLogger(FieldInjectorImpl.class);

   /**
    * All annotations including JAX-RS annotation.
    */
   private final Annotation[] annotations;

   /**
    * JAX-RS annotation.
    */
   private final Annotation annotation;

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

   /** See {@link java.lang.reflect.Field} . */
   private final java.lang.reflect.Field jfield;

   /**
    * @param resourceClass class that contains field <tt>jfield</tt>
    * @param jfield java.lang.reflect.Field
    */
   public FieldInjectorImpl(Class<?> resourceClass, java.lang.reflect.Field jfield)
   {

      this.jfield = jfield;
      this.annotations = jfield.getDeclaredAnnotations();

      Annotation annotation = null;
      String defaultValue = null;
      boolean encoded = false;

      // is resource provider
      boolean provider = resourceClass.getAnnotation(Provider.class) != null;
      List<String> allowedAnnotation;
      if (provider)
         allowedAnnotation = ParameterHelper.PROVIDER_FIELDS_ANNOTATIONS;
      else
         allowedAnnotation = ParameterHelper.RESOURCE_FIELDS_ANNOTATIONS;

      for (Annotation a : annotations)
      {
         Class<?> ac = a.annotationType();

         if (allowedAnnotation.contains(ac.getName()))
         {
            if (annotation == null)
            {
               annotation = a;
            }
            else
            {
               String msg =
                  "JAX-RS annotations on one of fields " + jfield.toString() + " are equivocality. Annotations: "
                     + annotation.toString() + " and " + a.toString() + " can't be applied to one field.";
               throw new RuntimeException(msg);
            }

            // @Encoded has not sense for Provider. Provider may use only @Context
            // annotation for fields
         }
         else if (ac == Encoded.class && !provider)
         {
            encoded = true;
            // @Default has not sense for Provider. Provider may use only @Context
            // annotation for fields
         }
         else if (ac == DefaultValue.class && !provider)
         {
            defaultValue = ((DefaultValue)a).value();
         }
         else
         {
            LOG.warn("Field " + jfield.toString() + " contains unknown or not allowed JAX-RS annotation "
               + a.toString() + ". It will be ignored.");
         }
      }

      this.defaultValue = defaultValue;
      this.annotation = annotation;
      this.encoded = encoded || resourceClass.getAnnotation(Encoded.class) != null;

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
   public Annotation[] getAnnotations()
   {
      return annotations;
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
   public Class<?> getParameterClass()
   {
      return jfield.getType();
   }

   /**
    * {@inheritDoc}
    */
   public Type getGenericType()
   {
      return jfield.getGenericType();
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
   public String getName()
   {
      return jfield.getName();
   }

   /**
    * {@inheritDoc}
    */
   public void inject(Object resource, ApplicationContext context)
   {
      if (annotation != null)
      {
         ParameterResolver<?> pr = ParameterResolverFactory.createParameterResolver(annotation);
         try
         {
            if (!Modifier.isPublic(jfield.getModifiers()))
               jfield.setAccessible(true);

            jfield.set(resource, pr.resolve(this, context));
         }
         catch (Throwable e)
         {

            Class<?> ac = annotation.annotationType();
            if (ac == MatrixParam.class || ac == QueryParam.class || ac == PathParam.class)
               throw new WebApplicationException(e, Response.status(Response.Status.NOT_FOUND).build());

            throw new WebApplicationException(e, Response.status(Response.Status.BAD_REQUEST).build());
         }
      }
      else
      {
         if (context.getDependencySupplier() != null)
         {
            Object tmp = context.getDependencySupplier().getComponent(this);
            if (tmp != null)
            {
               try
               {
                  if (!Modifier.isPublic(jfield.getModifiers()))
                     jfield.setAccessible(true);
                  jfield.set(resource, tmp);
               }
               catch (Throwable e)
               {
                  throw new WebApplicationException(e, Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
               }
            }
         }

      }

   }

   /**
    * {@inheritDoc}
    */
   public void accept(ResourceDescriptorVisitor visitor)
   {
      visitor.visitFieldInjector(this);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder("[ FieldInjectorImpl: ");
      sb.append("annotation: " + getAnnotation()).append("; type: " + getParameterClass()).append(
         "; generic-type : " + getGenericType()).append("; default-value: " + getDefaultValue()).append(
         "; encoded: " + isEncoded()).append(" ]");
      return sb.toString();
   }

}
