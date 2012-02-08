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
package org.everrest.core.impl.resource;

import org.everrest.core.method.MethodParameter;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.resource.ResourceDescriptorVisitor;
import org.everrest.core.resource.ResourceMethodDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import javax.ws.rs.core.MediaType;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ResourceMethodDescriptorImpl.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
 */
public class ResourceMethodDescriptorImpl implements ResourceMethodDescriptor
{
   /** This method will be invoked. */
   private final Method method;

   /** HTTP request method designator. */
   private final String httpMethod;

   /** List of method's parameters. See {@link MethodParameter} . */
   private final List<MethodParameter> parameters;

   /**
    * Parent resource for this method resource, in other words class which
    * contains this method.
    */
   private final AbstractResourceDescriptor parentResource;

   /**
    * List of media types which this method can consume. See
    * {@link javax.ws.rs.Consumes} .
    */
   private final List<MediaType> consumes;

   /**
    * List of media types which this method can produce. See
    * {@link javax.ws.rs.Produces} .
    */
   private final List<MediaType> produces;

   private final Annotation[] additional;

   /**
    * Constructs new instance of {@link ResourceMethodDescriptor}.
    *
    * @param method See {@link Method}
    * @param httpMethod HTTP request method designator
    * @param parameters list of method parameters. See {@link MethodParameter}
    * @param parentResource parent resource for this method
    * @param consumes list of media types which this method can consume
    * @param produces list of media types which this method can produce
    * @param additional set of additional (not JAX-RS annotations)
    */
   ResourceMethodDescriptorImpl(Method method,
                                String httpMethod,
                                List<MethodParameter> parameters,
                                AbstractResourceDescriptor parentResource,
                                List<MediaType> consumes,
                                List<MediaType> produces,
                                Annotation[] additional)
   {
      this.method = method;
      this.httpMethod = httpMethod;
      this.parameters = parameters;
      this.parentResource = parentResource;
      this.consumes = consumes;
      this.produces = produces;
      this.additional = additional;
   }

   /** {@inheritDoc} */
   public Method getMethod()
   {
      return method;
   }

   /** {@inheritDoc} */
   public List<MethodParameter> getMethodParameters()
   {
      return parameters;
   }

   /** {@inheritDoc} */
   public AbstractResourceDescriptor getParentResource()
   {
      return parentResource;
   }

   /** {@inheritDoc} */
   public void accept(ResourceDescriptorVisitor visitor)
   {
      visitor.visitResourceMethodDescriptor(this);
   }

   /** {@inheritDoc} */
   public List<MediaType> consumes()
   {
      return consumes;
   }

   /** {@inheritDoc} */
   public String getHttpMethod()
   {
      return httpMethod;
   }

   /** {@inheritDoc} */
   public List<MediaType> produces()
   {
      return produces;
   }

   /** {@inheritDoc} */
   public Class<?> getResponseType()
   {
      return getMethod().getReturnType();
   }

   /** {@inheritDoc} */
   public Annotation[] getAnnotations()
   {
      return additional;
   }

   /** {@inheritDoc} */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder("[ ResourceMethodDescriptorImpl: ");
      sb.append("resource: ");
      sb.append(getParentResource());
      sb.append("; HTTP method: ");
      sb.append(getHttpMethod());
      sb.append("; produces media type: ");
      sb.append(produces());
      sb.append("; consumes media type: ");
      sb.append(consumes());
      sb.append("; return type: ");
      sb.append(getResponseType());
      sb.append("] ]");
      return sb.toString();
   }
}
