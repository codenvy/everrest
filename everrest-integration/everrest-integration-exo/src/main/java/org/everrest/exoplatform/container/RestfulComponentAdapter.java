/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.everrest.exoplatform.container;

import org.everrest.core.ApplicationContext;
import org.everrest.core.BaseDependencySupplier;
import org.everrest.core.ComponentLifecycleScope;
import org.everrest.core.DependencySupplier;
import org.everrest.core.Filter;
import org.everrest.core.ObjectFactory;
import org.everrest.core.ObjectModel;
import org.everrest.core.PerRequestObjectFactory;
import org.everrest.core.SingletonObjectFactory;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.FilterDescriptorImpl;
import org.everrest.core.impl.provider.ProviderDescriptorImpl;
import org.everrest.core.impl.resource.AbstractResourceDescriptorImpl;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.PicoVisitor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * Implementation of {@link ComponentAdapter} for providing instance of JAX-RS resource of provider.
 * 
 * @author <a href="andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class RestfulComponentAdapter implements ComponentAdapter
{
   public static boolean isRestfulComponent(Object classOrInstance)
   {
      Class<?> clazz = (classOrInstance instanceof Class) ? (Class<?>)classOrInstance : classOrInstance.getClass();
      return clazz.isAnnotationPresent(Path.class) //
         || clazz.isAnnotationPresent(Provider.class) //
         || clazz.isAnnotationPresent(Filter.class);
   }

   private final Class<?> clazz;
   private final Object componentKey;
   private final ObjectFactory<ObjectModel> factory;
   private final ParameterizedType[] implementedInterfaces;

   /**
    * Create new RestfulComponentAdapter for specified componentKey and class or instance.
    * 
    * @param componentKey the key of component
    * @param classOrInstance the class or instance of JAX-RS resource or provider
    * @throws NullPointerException if <code>componentKey</code> or <code>classOrInstance</code> is <code>null</code>
    * @throws IllegalArgumentException if <code>classOrInstance</code> has neither {@link Path}, {@link Provider} nor
    *            {@link Filter}.
    */
   public RestfulComponentAdapter(Object componentKey, Object classOrInstance)
   {
      if (componentKey == null || classOrInstance == null)
      {
         throw new NullPointerException();
      }

      this.componentKey = componentKey;

      ComponentLifecycleScope lifecycle;
      if (classOrInstance instanceof Class)
      {
         clazz = (Class<?>)classOrInstance;
         lifecycle = ComponentLifecycleScope.PER_REQUEST;
      }
      else
      {
         clazz = classOrInstance.getClass();
         lifecycle = ComponentLifecycleScope.SINGLETON;
      }

      implementedInterfaces = getImplementedInterfaces(clazz);

      ObjectModel objectModel;
      if (clazz.isAnnotationPresent(Path.class))
      {
         objectModel = new AbstractResourceDescriptorImpl(clazz, lifecycle);
      }
      else if (clazz.isAnnotationPresent(Provider.class))
      {
         objectModel = new ProviderDescriptorImpl(clazz, lifecycle);
      }
      else if (clazz.isAnnotationPresent(Filter.class))
      {
         objectModel = new FilterDescriptorImpl(clazz, lifecycle);
      }
      else
      {
         throw new IllegalArgumentException("Incorrect type or instance " + clazz + ". ");
      }

      factory =
         ComponentLifecycleScope.SINGLETON == lifecycle ? new SingletonObjectFactory<ObjectModel>(objectModel,
            classOrInstance) : new PerRequestObjectFactory<ObjectModel>(objectModel);
   }

   public ObjectModel getObjectModel()
   {
      return factory.getObjectModel();
   }

   ParameterizedType[] getImplementedInterfaces()
   {
      return implementedInterfaces;
   }

   /**
    * @see org.picocontainer.ComponentAdapter#getComponentInstance(org.picocontainer.PicoContainer)
    */
   @Override
   public Object getComponentInstance(PicoContainer container) throws PicoInitializationException,
      PicoIntrospectionException
   {
      ApplicationContext context = ApplicationContextImpl.getCurrent();
      if (context == null)
      {
         throw new IllegalStateException("ApplicationContext is not initialized. ");
      }
      DependencySupplier dependencies = context.getDependencySupplier();
      try
      {
         context.setDependencySupplier(new ContainerDependencySupplier(container, dependencies));
         return factory.getInstance(context);
      }
      finally
      {
         context.setDependencySupplier(dependencies);
      }
   }

   /**
    * @see org.picocontainer.ComponentAdapter#verify(org.picocontainer.PicoContainer)
    */
   @Override
   public void verify(PicoContainer container) throws PicoIntrospectionException
   {
   }

   /**
    * @see org.picocontainer.ComponentAdapter#accept(org.picocontainer.PicoVisitor)
    */
   @Override
   public void accept(PicoVisitor visitor)
   {
      visitor.visitComponentAdapter(this);
   }

   /**
    * @see org.picocontainer.ComponentAdapter#getComponentKey()
    */
   @Override
   public Object getComponentKey()
   {
      return componentKey;
   }

   /**
    * @see org.picocontainer.ComponentAdapter#getComponentImplementation()
    */
   @SuppressWarnings("rawtypes")
   @Override
   public Class getComponentImplementation()
   {
      return clazz;
   }

   @Override
   public String toString()
   {
      return "RestfulComponentAdapter [" + getComponentKey() + "]";
   }

   @SuppressWarnings("rawtypes")
   private static final Class[] KNOWN_INTERFACES = new Class[]{MessageBodyReader.class, MessageBodyWriter.class,
      ExceptionMapper.class, ContextResolver.class};

   private static ParameterizedType[] getImplementedInterfaces(Class<?> type)
   {
      if (type.isAnnotationPresent(Provider.class))
      {
         List<ParameterizedType> implementedInterfaces = new ArrayList<ParameterizedType>();
         for (int i = 0; i < KNOWN_INTERFACES.length; i++)
         {
            ParameterizedType impl = getGenericInterface(KNOWN_INTERFACES[i], type);
            if (impl != null)
            {
               implementedInterfaces.add(impl);
            }
         }

         if (implementedInterfaces.size() == 0)
         {
            throw new IllegalArgumentException("Type " + type
               + " annotatited with @javax.ws.rs.ext.Provider but does not implement any of the interfaces: "
               + Arrays.toString(KNOWN_INTERFACES));
         }

         return implementedInterfaces.toArray(new ParameterizedType[implementedInterfaces.size()]);
      }
      return new ParameterizedType[0];
   }

   private static ParameterizedType getGenericInterface(Class<?> itype, Class<?> type)
   {
      for (Type t : type.getGenericInterfaces())
      {
         if (t instanceof ParameterizedType)
         {
            ParameterizedType parameterized = (ParameterizedType)t;
            if (itype == parameterized.getRawType())
            {
               return parameterized;
            }
         }
      }
      Class<?> sc = type.getSuperclass();
      if (sc != null && sc != Object.class)
      {
         return getGenericInterface(itype, sc);
      }
      return null;
   }

   //

   private static class ContainerDependencySupplier extends BaseDependencySupplier
   {
      private final PicoContainer container;
      private final DependencySupplier delegate;

      public ContainerDependencySupplier(PicoContainer container, DependencySupplier dependencies)
      {
         this.container = container;
         this.delegate = dependencies;
      }

      /**
       * @see org.everrest.core.DependencySupplier#getComponent(java.lang.Class)
       */
      @Override
      public Object getComponent(Class<?> type)
      {
         Object object = container.getComponentInstanceOfType(type);
         if (object != null)
         {
            return object;
         }
         if (delegate != null)
         {
            return delegate.getComponent(type);
         }
         return null;
      }
   }
}
