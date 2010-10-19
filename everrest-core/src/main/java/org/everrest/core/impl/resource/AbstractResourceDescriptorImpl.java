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

import org.everrest.core.BaseObjectModel;
import org.everrest.core.ComponentLifecycleScope;
import org.everrest.core.ConstructorDescriptor;
import org.everrest.core.FieldInjector;
import org.everrest.core.impl.ConstructorDescriptorImpl;
import org.everrest.core.impl.FieldInjectorImpl;
import org.everrest.core.impl.header.MediaTypeHelper;
import org.everrest.core.impl.method.MethodParameterImpl;
import org.everrest.core.impl.method.ParameterHelper;
import org.everrest.core.method.MethodParameter;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.resource.ResourceDescriptorVisitor;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.resource.ResourceMethodMap;
import org.everrest.core.resource.SubResourceLocatorDescriptor;
import org.everrest.core.resource.SubResourceLocatorMap;
import org.everrest.core.resource.SubResourceMethodDescriptor;
import org.everrest.core.resource.SubResourceMethodMap;
import org.everrest.core.uri.UriPattern;
import org.everrest.core.util.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: AbstractResourceDescriptorImpl.java 292 2009-10-19 07:03:07Z
 *          aparfonov $
 */
public class AbstractResourceDescriptorImpl extends BaseObjectModel implements AbstractResourceDescriptor
{

   /** Logger. */
   private static final Logger LOG = Logger.getLogger(AbstractResourceDescriptorImpl.class);

   /** PathValue. */
   private final PathValue path;

   /** UriPattern. */
   private final UriPattern uriPattern;

   /** Sub-resource methods. Sub-resource method has path annotation. */
   private final SubResourceMethodMap subResourceMethods;

   /** Sub-resource locators. Sub-resource locator has path annotation. */
   private final SubResourceLocatorMap subResourceLocators;

   /** Resource methods. Resource method has not own path annotation. */
   private final ResourceMethodMap<ResourceMethodDescriptor> resourceMethods;

   /** ConstructorDescriptor. */
   private final List<ConstructorDescriptor> constructors;

   /** Resource class fields. */
   private final List<FieldInjector> fields;

   /**
    * Constructs new instance of AbstractResourceDescriptor without path
    * (sub-resource).
    *
    * @param resourceClass resource class
    */
   public AbstractResourceDescriptorImpl(Class<?> resourceClass, ComponentLifecycleScope scope)
   {
      this(resourceClass.getAnnotation(Path.class), resourceClass, scope);
   }

   /**
    * @param path the path value
    * @param resourceClass resource class
    * @param scope resource scope
    * @see ComponentLifecycleScope
    */
   private AbstractResourceDescriptorImpl(Path path, Class<?> resourceClass, ComponentLifecycleScope scope)
   {
      super(resourceClass);
      if (path != null)
      {
         this.path = new PathValue(path.value());
         uriPattern = new UriPattern(path.value());
      }
      else
      {
         this.path = null;
         uriPattern = null;
      }

      this.constructors = new ArrayList<ConstructorDescriptor>();
      this.fields = new ArrayList<FieldInjector>();
      if (scope == ComponentLifecycleScope.PER_REQUEST)
      {
         for (Constructor<?> constructor : resourceClass.getConstructors())
         {
            constructors.add(new ConstructorDescriptorImpl(resourceClass, constructor));
         }
         if (constructors.size() == 0)
         {
            String msg = "Not found accepted constructors for resource class " + resourceClass.getName();
            throw new RuntimeException(msg);
         }
         // Sort constructors in number parameters order
         if (constructors.size() > 1)
         {
            Collections.sort(constructors, ConstructorDescriptorImpl.CONSTRUCTOR_COMPARATOR);
         }
         // process field
         for (java.lang.reflect.Field jfield : resourceClass.getDeclaredFields())
         {
            fields.add(new FieldInjectorImpl(resourceClass, jfield));
         }
         Class<?> sc = resourceClass.getSuperclass();
         while (sc != Object.class)
         {
            for (java.lang.reflect.Field jfield : sc.getDeclaredFields())
            {
               int modif = jfield.getModifiers();
               // TODO process fields with package visibility.
               if (Modifier.isPublic(modif) || Modifier.isProtected(modif))
               {
                  FieldInjector inj = new FieldInjectorImpl(resourceClass, jfield);
                  // Skip not annotated field. They will be not injected from container.
                  if (inj.getAnnotation() != null)
                  {
                     fields.add(new FieldInjectorImpl(resourceClass, jfield));
                  }
               }
            }
            sc = sc.getSuperclass();
         }
      }

      this.resourceMethods = new ResourceMethodMap<ResourceMethodDescriptor>();
      this.subResourceMethods = new SubResourceMethodMap();
      this.subResourceLocators = new SubResourceLocatorMap();
      processMethods();
   }

   /**
    * {@inheritDoc}
    */
   public void accept(ResourceDescriptorVisitor visitor)
   {
      visitor.visitAbstractResourceDescriptor(this);
   }

   /**
    * {@inheritDoc}
    */
   public List<ConstructorDescriptor> getConstructorDescriptors()
   {
      return constructors;
   }

   /**
    * {@inheritDoc}
    */
   public List<FieldInjector> getFieldInjectors()
   {
      return fields;
   }

   /**
    * {@inheritDoc}
    */
   public PathValue getPathValue()
   {
      return path;
   }

   /**
    * {@inheritDoc}
    */
   public ResourceMethodMap<ResourceMethodDescriptor> getResourceMethods()
   {
      return resourceMethods;
   }

   /**
    * {@inheritDoc}
    */
   public SubResourceLocatorMap getSubResourceLocators()
   {
      return subResourceLocators;
   }

   /**
    * {@inheritDoc}
    */
   public SubResourceMethodMap getSubResourceMethods()
   {
      return subResourceMethods;
   }

   /**
    * {@inheritDoc}
    */
   public UriPattern getUriPattern()
   {
      return uriPattern;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isRootResource()
   {
      return path != null;
   }

   /**
    * Process method of resource and separate them to three types Resource
    * Methods, Sub-Resource Methods and Sub-Resource Locators.
    */
   protected void processMethods()
   {
      Class<?> resourceClass = getObjectClass();

      for (Method method : resourceClass.getDeclaredMethods())
      {
         for (Annotation a : method.getAnnotations())
         {
            Class<?> ac = a.annotationType();
            if (!Modifier.isPublic(method.getModifiers())
               && (ac == CookieParam.class || ac == Consumes.class || ac == Context.class || ac == DefaultValue.class
                  || ac == Encoded.class || ac == FormParam.class || ac == HeaderParam.class || ac == MatrixParam.class
                  || ac == Path.class || ac == PathParam.class || ac == Produces.class || ac == QueryParam.class || ac
                  .getAnnotation(HttpMethod.class) != null))
            {

               LOG.warn("Non-public method at resource " + toString() + " annotated with JAX-RS annotation: " + a);
            }
         }
      }

      for (Method method : resourceClass.getMethods())
      {
         Path subPath = getMethodAnnotation(method, resourceClass, Path.class, false);
         HttpMethod httpMethod = getMethodAnnotation(method, resourceClass, HttpMethod.class, true);

         if (subPath != null || httpMethod != null)
         {
            List<MethodParameter> params = createMethodParametersList(resourceClass, method);

            // Need only one type annotation at the moment
            Annotation security = getSecurityAnnotation(method, resourceClass);
            Annotation[] additional = security != null ? new Annotation[]{security} : new Annotation[0];

            if (httpMethod != null)
            {
               Produces p = getMethodAnnotation(method, resourceClass, Produces.class, false);
               if (p == null)
                  p = resourceClass.getAnnotation(Produces.class); // from resource
               // class
               List<MediaType> produces = MediaTypeHelper.createProducesList(p);

               Consumes c = getMethodAnnotation(method, resourceClass, Consumes.class, false);
               if (c == null)
                  c = resourceClass.getAnnotation(Consumes.class); // from resource
               // class
               List<MediaType> consumes = MediaTypeHelper.createConsumesList(c);

               if (subPath == null)
               {
                  // resource method
                  ResourceMethodDescriptor res =
                     new ResourceMethodDescriptorImpl(method, httpMethod.value(), params, this, consumes, produces,
                        additional);
                  ResourceMethodDescriptor exist =
                     findMethodResourceMediaType(resourceMethods.getList(httpMethod.value()), res.consumes(), res
                        .produces());
                  if (exist == null)
                  {
                     resourceMethods.add(httpMethod.value(), res);
                  }
                  else
                  {
                     String msg =
                        "Two resource method " + res + " and " + exist
                           + " with the same HTTP method, consumes and produces found.";
                     throw new RuntimeException(msg);
                  }
               }
               else
               {
                  // sub-resource method
                  SubResourceMethodDescriptor subRes =
                     new SubResourceMethodDescriptorImpl(new PathValue(subPath.value()), method, httpMethod.value(),
                        params, this, consumes, produces, additional);
                  SubResourceMethodDescriptor exist = null;
                  ResourceMethodMap<SubResourceMethodDescriptor> rmm =
                     subResourceMethods.getMethodMap(subRes.getUriPattern());
                  // rmm is never null, empty map instead

                  List<SubResourceMethodDescriptor> l = rmm.getList(httpMethod.value());
                  exist =
                     (SubResourceMethodDescriptor)findMethodResourceMediaType(l, subRes.consumes(), subRes.produces());
                  if (exist == null)
                  {
                     rmm.add(httpMethod.value(), subRes);
                  }
                  else
                  {
                     String msg =
                        "Two sub-resource method " + subRes + " and " + exist
                           + " with the same HTTP method, path, consumes and produces found.";
                     throw new RuntimeException(msg);
                  }
               }
            }
            else
            {
               if (subPath != null)
               {
                  // sub-resource locator
                  SubResourceLocatorDescriptor loc =
                     new SubResourceLocatorDescriptorImpl(new PathValue(subPath.value()), method, params, this,
                        additional);
                  if (!subResourceLocators.containsKey(loc.getUriPattern()))
                  {
                     subResourceLocators.put(loc.getUriPattern(), loc);
                  }
                  else
                  {
                     String msg =
                        "Two sub-resource locators " + loc + " and " + subResourceLocators.get(loc.getUriPattern())
                           + " with the same path found.";
                     throw new RuntimeException(msg);
                  }
               }
            }
         }
      }
      int resMethodCount = resourceMethods.size() + subResourceMethods.size() + subResourceLocators.size();
      if (resMethodCount == 0)
      {
         String msg =
            "Not found any resource methods, sub-resource methods" + " or sub-resource locators in "
               + resourceClass.getName();
         throw new RuntimeException(msg);
      }

      // End method processing.
      // Start HEAD and OPTIONS resolving, see JAX-RS (JSR-311) specification
      // section 3.3.5
      resolveHeadRequest();
      resolveOptionsRequest();

      resourceMethods.sort();
      subResourceMethods.sort();
      // sub-resource locators already sorted
   }

   /**
    * Create list of {@link MethodParameter} .
    *
    * @param resourceClass class
    * @param method See {@link Method}
    * @return list of {@link MethodParameter}
    */
   protected List<MethodParameter> createMethodParametersList(Class<?> resourceClass, Method method)
   {
      Class<?>[] parameterClasses = method.getParameterTypes();
      if (parameterClasses.length == 0)
         return java.util.Collections.emptyList();

      Type[] parameterGenTypes = method.getGenericParameterTypes();
      Annotation[][] annotations = method.getParameterAnnotations();

      List<MethodParameter> params = new ArrayList<MethodParameter>(parameterClasses.length);
      for (int i = 0; i < parameterClasses.length; i++)
      {
         String defaultValue = null;
         Annotation annotation = null;
         boolean encoded = false;

         List<String> allowedAnnotation = ParameterHelper.RESOURCE_METHOD_PARAMETER_ANNOTATIONS;

         for (Annotation a : annotations[i])
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
                     "JAX-RS annotations on one of method parameters of resource " + toString() + ", method "
                        + method.getName() + " are equivocality. " + "Annotations: " + annotation + " and " + a
                        + " can't be applied to one parameter.";
                  throw new RuntimeException(msg);
               }

            }
            else if (ac == Encoded.class)
            {
               encoded = true;
            }
            else if (ac == DefaultValue.class)
            {
               defaultValue = ((DefaultValue)a).value();
            }
            else
            {
               LOG.warn("Method parameter of resource " + toString() + ", method " + method.getName()
                  + " contains unknown or not valid JAX-RS annotation " + a.toString() + ". It will be ignored.");
            }
         }

         encoded = encoded || resourceClass.getAnnotation(Encoded.class) != null;

         MethodParameter mp =
            new MethodParameterImpl(annotation, annotations[i], parameterClasses[i], parameterGenTypes[i],
               defaultValue, encoded);
         params.add(mp);
      }

      return params;
   }

   /**
    * According to JSR-311:
    * <p>
    * On receipt of a HEAD request an implementation MUST either: 1. Call method
    * annotated with request method designation for HEAD or, if none present, 2.
    * Call method annotated with a request method designation GET and discard
    * any returned entity.
    * </p>
    */
   protected void resolveHeadRequest()
   {

      List<ResourceMethodDescriptor> getRes = resourceMethods.get(HttpMethod.GET);
      if (getRes == null || getRes.size() == 0)
         return; // nothing to do, there is not 'GET' methods

      // If there is no methods for 'HEAD' anyway never return null.
      // Instead null empty List will be returned.
      List<ResourceMethodDescriptor> headRes = resourceMethods.getList(HttpMethod.HEAD);

      for (ResourceMethodDescriptor rmd : getRes)
      {
         if (findMethodResourceMediaType(headRes, rmd.consumes(), rmd.produces()) == null)
            headRes.add(new ResourceMethodDescriptorImpl(rmd.getMethod(), HttpMethod.HEAD, rmd.getMethodParameters(),
               this, rmd.consumes(), rmd.produces(), rmd.getAnnotations()));
      }
      for (ResourceMethodMap<SubResourceMethodDescriptor> rmm : subResourceMethods.values())
      {
         List<SubResourceMethodDescriptor> getSubres = rmm.get(HttpMethod.GET);
         if (getSubres == null || getSubres.size() == 0)
            continue; // nothing to do, there is not 'GET' methods

         // If there is no methods for 'HEAD' anyway never return null.
         // Instead null empty List will be returned.
         List<SubResourceMethodDescriptor> headSubres = rmm.getList(HttpMethod.HEAD);

         Iterator<SubResourceMethodDescriptor> i = getSubres.iterator();
         while (i.hasNext())
         {
            SubResourceMethodDescriptor srmd = i.next();
            if (findMethodResourceMediaType(headSubres, srmd.consumes(), srmd.produces()) == null)
            {
               headSubres.add(new SubResourceMethodDescriptorImpl(srmd.getPathValue(), srmd.getMethod(),
                  HttpMethod.HEAD, srmd.getMethodParameters(), this, srmd.consumes(), srmd.produces(), srmd
                     .getAnnotations()));
            }
         }
      }
   }

   /**
    * According to JSR-311:
    * <p>
    * On receipt of a OPTIONS request an implementation MUST either: 1. Call
    * method annotated with request method designation for OPTIONS or, if none
    * present, 2. Generate an automatic response using the metadata provided by
    * the JAX-RS annotations on the matching class and its methods.
    * </p>
    */
   protected void resolveOptionsRequest()
   {
      List<ResourceMethodDescriptor> o = resourceMethods.getList("OPTIONS");
      if (o.size() == 0)
      {
         List<MethodParameter> mps = Collections.emptyList();
         List<MediaType> consumes = MediaTypeHelper.DEFAULT_TYPE_LIST;
         List<MediaType> produces = new ArrayList<MediaType>(1);
         produces.add(MediaTypeHelper.WADL_TYPE);
         o.add(new OptionsRequestResourceMethodDescriptorImpl(null, "OPTIONS", mps, this, consumes, produces,
            new Annotation[0]));
      }
      // TODO need process sub-resources ?
   }

   /**
    * Get all method with at least one annotation which has annotation
    * <i>annotation</i>. It is useful for annotation {@link javax.ws.rs.GET},
    * etc. All HTTP method annotations has annotation {@link HttpMethod}.
    *
    * @param <T> annotation type
    * @param m method
    * @param annotation annotation class
    * @return list of annotation
    */
   protected <T extends Annotation> T getMetaAnnotation(Method m, Class<T> annotation)
   {
      for (Annotation a : m.getAnnotations())
      {
         T endPoint = null;
         if ((endPoint = a.annotationType().getAnnotation(annotation)) != null)
            return endPoint;
      }
      return null;
   }

   /**
    * Tries to get JAX-RS annotation on method from the root resource class's
    * superclass or implemented interfaces.
    *
    * @param <T> annotation type
    * @param method method for discovering
    * @param resourceClass class that contains discovered method
    * @param annotationClass annotation type what we are looking for
    * @param metaAnnotation false if annotation should be on method and true in
    *        method should contain annotations that has supplied annotation
    * @return annotation from class or its ancestor or null if nothing found
    */
   protected <T extends Annotation> T getMethodAnnotation(Method method, Class<?> resourceClass,
      Class<T> annotationClass, boolean metaAnnotation)
   {

      T annotation = null;
      if (metaAnnotation)
         annotation = getMetaAnnotation(method, annotationClass);
      else
         annotation = method.getAnnotation(annotationClass);

      if (annotation == null)
      {

         Method inhMethod = null;

         try
         {
            inhMethod = resourceClass.getSuperclass().getMethod(method.getName(), method.getParameterTypes());
         }
         catch (NoSuchMethodException e)
         {
            for (Class<?> intf : resourceClass.getInterfaces())
            {
               try
               {

                  Method tmp = intf.getMethod(method.getName(), method.getParameterTypes());
                  if (inhMethod == null)
                  {
                     inhMethod = tmp;
                  }
                  else
                  {
                     String msg =
                        "JAX-RS annotation on method " + inhMethod.getName() + " of resource " + toString()
                           + " is equivocality.";
                     throw new RuntimeException(msg);
                  }
               }
               catch (NoSuchMethodException exc)
               {
               }
            }
         }

         if (inhMethod != null)
         {
            if (metaAnnotation)
               annotation = getMetaAnnotation(inhMethod, annotationClass);
            else
               annotation = inhMethod.getAnnotation(annotationClass);
         }
      }

      return annotation;
   }

   /**
    * Check is collection of {@link ResourceMethodDescriptor} already contains
    * ResourceMethodDescriptor with the same media types.
    *
    * @param rmds {@link Set} of {@link ResourceMethodDescriptor}
    * @param consumes resource method consumed media type
    * @param produces resource method produced media type
    * @return ResourceMethodDescriptor or null if nothing found
    */
   protected <T extends ResourceMethodDescriptor> ResourceMethodDescriptor findMethodResourceMediaType(List<T> rmds,
      List<MediaType> consumes, List<MediaType> produces)
   {

      ResourceMethodDescriptor matched = null;
      for (T rmd : rmds)
      {
         if (rmd.consumes().size() != consumes.size())
            return null;
         if (rmd.produces().size() != produces.size())
            return null;
         for (MediaType c1 : rmd.consumes())
         {
            boolean eq = false;
            for (MediaType c2 : consumes)
            {
               if (c1.equals(c2))
               {
                  eq = true;
                  break;
               }
            }
            if (!eq)
               return null;
         }

         for (MediaType p1 : rmd.produces())
         {
            boolean eq = false;
            for (MediaType p2 : produces)
            {
               if (p1.equals(p2))
               {
                  eq = true;
                  break;
               }
            }
            if (!eq)
               return null;
         }

         matched = rmd; // matched resource method
         break;
      }

      return matched;
   }

   /**
    * Get security annotation (DenyAll, RolesAllowed, PermitAll) from
    * <code>method</code> or class <code>clazz</class> which contains method.
    * Supper class or implemented interfaces will be also checked. Annotation
    * on method has the advantage on annotation on class or interface.
    *
    * @param method method to be checked for security annotation
    * @param clazz class which contains <code>method</code>
    * @return one of security annotation or <code>null</code> is no such
    *         annotation found
    * @see DenyAll
    * @see RolesAllowed
    * @see PermitAll
    */
   @SuppressWarnings("unchecked")
   private <T extends Annotation> T getSecurityAnnotation(java.lang.reflect.Method method, Class<?> clazz)
   {
      Class<T>[] aclasses = new Class[]{DenyAll.class, RolesAllowed.class, PermitAll.class};
      T a = getAnnotation(method, aclasses);
      if (a == null)
      {
         a = getAnnotation(clazz, aclasses);
         if (a == null)
         {
            Class<?> superclass = clazz.getSuperclass();
            java.lang.reflect.Method inhMethod = null;
            try
            {
               inhMethod = superclass.getMethod(method.getName(), method.getParameterTypes());
               a = getAnnotation(inhMethod, aclasses);
            }
            catch (NoSuchMethodException e)
            {
            }
            if (a == null)
            {
               a = getAnnotation(superclass, aclasses);
               if (a == null)
               {
                  Class<?>[] interfaces = clazz.getInterfaces();
                  for (int k = 0; a == null && k < interfaces.length; k++)
                  {
                     try
                     {
                        inhMethod = interfaces[k].getMethod(method.getName(), method.getParameterTypes());
                        a = getAnnotation(inhMethod, aclasses);
                     }
                     catch (NoSuchMethodException exc)
                     {
                     }
                     if (a == null)
                     {
                        a = getAnnotation(interfaces[k], aclasses);
                     }
                  }
               }
            }
         }
      }
      return a;
   }

   private <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T>[] aclass)
   {
      T a = null;
      for (int i = 0; a == null && i < aclass.length; i++)
      {
         a = clazz.getAnnotation(aclass[i]);
      }
      return a;
   }

   private <T extends Annotation> T getAnnotation(java.lang.reflect.Method method, Class<T>[] aclass)
   {
      T a = null;
      for (int i = 0; a == null && i < aclass.length; i++)
      {
         a = method.getAnnotation(aclass[i]);
      }
      return a;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder("[ AbstractResourceDescriptorImpl: ");
      sb.append("path: " + getPathValue()).append("; isRootResource: " + isRootResource()).append(
         "; class: " + getObjectClass()).append(" ]");
      return sb.toString();
   }

}
