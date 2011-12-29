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

import org.everrest.core.ConstructorDescriptor;
import org.everrest.core.FieldInjector;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.ObjectModel;
import org.everrest.core.method.MethodParameter;
import org.everrest.core.provider.ProviderDescriptor;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.resource.ResourceDescriptor;
import org.everrest.core.resource.ResourceDescriptorVisitor;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.resource.ResourceMethodMap;
import org.everrest.core.resource.SubResourceLocatorDescriptor;
import org.everrest.core.resource.SubResourceMethodDescriptor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Validate ResourceDescriptors. @see
 * {@link ResourceDescriptor#accept(ResourceDescriptorVisitor)}.
 * <p>
 * Validation Goals:
 * <li>check number of method parameters without annotation, should be not more
 * then one at resource method or sub-resource method and no one at sub-resource
 * locator</li>
 * <li>if one of parameters at resource method or sub-resource method has
 * {@link FormParam} annotation then entity type can be only
 * MultivalueMap&lt;String, String&gt; and nothing other</li>
 * <li> {@link PathValue#getPath()} can't return empty string, it minds for root
 * resource classes, sub-resource methods and sub-resource locators can't have
 * annotation &#64;Path("")</li>
 * <li>Resource class must contains at least one resource method, sub-resource
 * method or sub-resource locator</li>
 * <p>
 * Non-Goals:
 * <li>Check does any two resource methods has the same consume and produce
 * media type. This will be done later in binding cycle</li>
 * <li>Check does any two sub-resource methods has the same consume and produce
 * media type and HTTP request method designation. This will be done later in
 * binding cycle</li>
 * <li>Check does two sub-resource locators has the same UriPattern</li>
 * <p>
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ResourceDescriptorValidator.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
 */
public class ResourceDescriptorValidator implements ResourceDescriptorVisitor
{

   /**
    * Visitor instance.
    */
   private static AtomicReference<ResourceDescriptorValidator> instance =
      new AtomicReference<ResourceDescriptorValidator>();

   /**
    * @return singleton instance of ResourceDescriptorVisitor
    */
   public static ResourceDescriptorValidator getInstance()
   {
      ResourceDescriptorValidator t = instance.get();
      if (t == null)
      {
         instance.compareAndSet(null, new ResourceDescriptorValidator());
         t = instance.get();
      }
      return t;
   }

   /**
    * Validate AbstractResourceDescriptor. AbstractResourceDescriptor is a class
    * which annotated with path annotation then it is root resource, or not
    * annotated with path then it is sub-resource. Can have also consumes and
    * produces annotation. Path annotation is required for root resource.
    * {@inheritDoc}
    */
   public void visitAbstractResourceDescriptor(AbstractResourceDescriptor ard)
   {

      if (ard.isRootResource() && ard.getPathValue().getPath().length() == 0)
      {
         String msg =
            "Resource class " + ard.getObjectClass()
               + " is root resource but path value empty, see javax.ws.rs.Path#value()";
         throw new RuntimeException(msg);
      }

      checkObjectModel(ard);

      // check all resource methods
      for (List<ResourceMethodDescriptor> l : ard.getResourceMethods().values())
      {
         for (ResourceMethodDescriptor rmd : l)
            rmd.accept(this);
      }

      // check all sub-resource methods
      for (ResourceMethodMap<SubResourceMethodDescriptor> rmm : ard.getSubResourceMethods().values())
      {
         for (List<SubResourceMethodDescriptor> l : rmm.values())
         {
            for (SubResourceMethodDescriptor rmd : l)
               rmd.accept(this);
         }
      }

      // check all sub-resource locators
      for (SubResourceLocatorDescriptor loc : ard.getSubResourceLocators().values())
      {
         loc.accept(this);
      }

   }

   /**
    * Validate ResourceMethodDescriptor. ResourceMethodDescriptor is method in
    * Resource class which has not path annotation. This method MUST have at
    * least one annotation (HTTP method, e.g. GET). {@inheritDoc}
    */
   public void visitResourceMethodDescriptor(ResourceMethodDescriptor rmd)
   {
      checkMethodParameters(rmd);
   }

   /**
    * Validate SubResourceLocatorDescriptor. SubResourceLocatorDescriptor is a
    * method which annotated with path annotation and has not HTTP method
    * annotation. This method can not directly process the request but it can
    * produces object that will handle the request. {@inheritDoc}
    */
   public void visitSubResourceLocatorDescriptor(SubResourceLocatorDescriptor srld)
   {
      if (srld.getPathValue().getPath().length() == 0)
      {
         String msg =
            "Path value is empty for method " + srld.getMethod().getName() + " in resource class "
               + srld.getParentResource().getObjectClass() + ", see javax.ws.rs.Path#value()";
         throw new RuntimeException(msg);
      }
      checkMethodParameters(srld);
   }

   /**
    * Validate SubResourceMethodDescriptor. SubResourceMethodDescriptor is a
    * method which annotated with path annotation and has HTTP method
    * annotation. This method can process the request directly. {@inheritDoc}
    */
   public void visitSubResourceMethodDescriptor(SubResourceMethodDescriptor srmd)
   {
      if (srmd.getPathValue().getPath().length() == 0)
      {
         String msg =
            "Path value is null or empty for method " + srmd.getMethod().getName() + " in resource class "
               + srmd.getParentResource().getObjectClass() + ", see javax.ws.rs.Path#value()";
         throw new RuntimeException(msg);
      }
      checkMethodParameters(srmd);
   }

   /**
    * Check method parameter for valid annotations. NOTE If a any method
    * parameter is annotated with {@link FormParam} then type of entity
    * parameter must be MultivalueMap&lt;String, String&gt;.
    * 
    * @param rmd See {@link ResourceMethodDescriptor}
    */
   private static void checkMethodParameters(ResourceMethodDescriptor rmd)
   {
      List<MethodParameter> l = rmd.getMethodParameters();
      boolean entity = false;
      boolean form = false;
      for (int i = 0; i < l.size(); i++)
      {
         // Must be only: MatrixParam, QueryParam, PathParam, HeaderParam,
         // FormParam, CookieParam, Context and only one of it at each parameter
         MethodParameter mp = l.get(i);
         if (mp.getAnnotation() == null)
         {
            if (!entity)
            {
               entity = true;
               if (form) // form already met then check type of entity
                  checkFormParam(mp.getParameterClass(), mp.getGenericType());
            }
            else
            {
               String msg =
                  "Wrong or absent annotation at parameter with index " + i + " at "
                     + rmd.getParentResource().getObjectClass() + "#" + rmd.getMethod().getName();
               throw new RuntimeException(msg);
            }

         }
         else
         {
            if (mp.getAnnotation().annotationType() == FormParam.class)
            {
               form = true;
               if (entity) // entity already met then check type of entity
                  checkFormParam(mp.getParameterClass(), mp.getGenericType());
            }
         }
      }
   }

   /**
    * Check does sub-resource locator has required annotation at method
    * parameters. Sub-resource locator can't has not annotated parameter (entity
    * parameter).
    * 
    * @param srld SubResourceLocatorDescriptor
    */
   private static void checkMethodParameters(SubResourceLocatorDescriptor srld)
   {
      List<MethodParameter> l = srld.getMethodParameters();
      for (int i = 0; i < l.size(); i++)
      {
         // Must be only: MatrixParam, QueryParam, PathParam, HeaderParam,
         // FormParam, CookieParam, Context and only one of it at each parameter
         MethodParameter mp = l.get(i);
         if (mp.getAnnotation() == null)
         {
            // not allowed to have not annotated parameters in resource locator
            String msg =
               "Wrong or absent annotation at parameter with index " + i + " at "
                  + srld.getParentResource().getObjectClass() + "#" + srld.getMethod().getName();
            throw new RuntimeException(msg);
         }
      }
   }

   /**
    * Check is supplied class MultivaluedMap&lt;String, String&gt;.
    * 
    * @param clazz class to be checked
    * @param type generic type
    * @see #checkGenericType(Type)
    */
   @SuppressWarnings("rawtypes")
   private static void checkFormParam(Class clazz, Type type)
   {
      if (MultivaluedMap.class != clazz || !checkGenericType(type))
      {
         String msg =
            "If a any method parameter is annotated with FormParam then type"
               + " of entity parameter MUST be MultivalueMap<String, String> or FormEntity";
         throw new RuntimeException(msg);
      }
   }

   /**
    * Check is supplied class parameterized as &lt;String, String&gt;.
    * 
    * @param type generic type
    * @return true if type is {@link ParameterizedType} and parameterized
    *         &lt;String, String&gt;, false otherwise
    */
   private static boolean checkGenericType(Type type)
   {
      if (type instanceof ParameterizedType)
      {
         ParameterizedType pt = (ParameterizedType)type;
         Type[] genericTypes = pt.getActualTypeArguments();
         if (genericTypes.length == 2)
         {
            try
            {
               return (String.class == genericTypes[0]) && (String.class == genericTypes[1]);
            }
            catch (ClassCastException e)
            {
               return false;
            }
         }
      }
      // not parameterized type
      // TODO must be tolerant for not parameterized type and use string as
      // default ?
      return false;
   }

   /**
    * {@inheritDoc}
    */
   public void visitConstructorInjector(ConstructorDescriptor ci)
   {
      // currently nothing to do, should be already valid
   }

   /**
    * {@inheritDoc}
    */
   public void visitFieldInjector(FieldInjector fi)
   {
      // currently nothing to do, should be already valid
   }

   /**
    * {@inheritDoc}
    */
   public void visitFilterDescriptor(FilterDescriptor fd)
   {
      checkObjectModel(fd);
   }

   /**
    * {@inheritDoc}
    */
   public void visitProviderDescriptor(ProviderDescriptor pd)
   {
      checkObjectModel(pd);
   }

   protected void checkObjectModel(ObjectModel model)
   {
      for (ConstructorDescriptor c : model.getConstructorDescriptors())
         c.accept(this);
      for (FieldInjector f : model.getFieldInjectors())
         f.accept(this);
   }

}
