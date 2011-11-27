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

import org.everrest.core.Filter;
import org.everrest.core.impl.header.MediaTypeHelper;
import org.everrest.core.uri.UriPattern;
import org.exoplatform.container.ConcurrentPicoContainer;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoRegistrationException;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DefaultComponentAdapterFactory;
import org.picocontainer.defaults.DuplicateComponentKeyRegistrationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@SuppressWarnings("serial")
public class RestfulContainer extends ConcurrentPicoContainer implements Providers
{
   public RestfulContainer()
   {
      this(new DefaultComponentAdapterFactory(), null);
   }

   public RestfulContainer(PicoContainer parent)
   {
      this(new DefaultComponentAdapterFactory(), parent);
   }

   public RestfulContainer(ComponentAdapterFactory factory, PicoContainer parent)
   {
      super(wrapComponentAdapterFactory(factory), parent);
   }

   private static ComponentAdapterFactory wrapComponentAdapterFactory(ComponentAdapterFactory componentAdapterFactory)
   {
      return new RestfulComponentAdapterFactory(componentAdapterFactory);
   }

   private volatile Map<Key, ComponentAdapter> restToComponentAdapters = new HashMap<Key, ComponentAdapter>();
   private final Lock lock = new ReentrantLock();

   private static final class Key
   {
      final AnnotationSummary annotations;
      final Type type;

      Key(AnnotationSummary annotations, Type type)
      {
         this.annotations = annotations;
         this.type = type;
      }

      Key(AnnotationSummary annotations)
      {
         this(annotations, null);
      }

      @Override
      public int hashCode()
      {
         int hash = 7;
         hash = hash * 31 + annotations.hashCode();
         hash = hash * 31 + (type == null ? 0 : type.hashCode());
         return hash;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
         {
            return true;
         }
         if (obj == null || getClass() != obj.getClass())
         {
            return false;
         }
         Key other = (Key)obj;
         if (!annotations.equals(other.annotations))
         {
            return false;
         }
         if (type == null)
         {
            if (other.type != null)
            {
               return false;
            }
         }
         else if (!type.equals(other.type))
         {
            return false;
         }
         return true;
      }
   }

   private static interface AnnotationSummary
   {
   }

   private enum EmptyAnnotationSummary implements AnnotationSummary {
      INSTANCE;
   }

   private static final class ResourceAnnotationSummary implements AnnotationSummary
   {
      private final UriPattern uriPattern;

      ResourceAnnotationSummary(UriPattern uriPattern)
      {
         this.uriPattern = uriPattern;
      }

      @Override
      public int hashCode()
      {
         int hash = 7;
         hash = hash * 31 + uriPattern.hashCode();
         return hash;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
         {
            return true;
         }
         if (obj == null || obj.getClass() != getClass())
         {
            return false;
         }
         return uriPattern.equals(((ResourceAnnotationSummary)obj).uriPattern);
      }
   }

   private static final class ProviderAnnotationSummary implements AnnotationSummary
   {
      private final Set<MediaType> consumes;
      private final Set<MediaType> produces;

      ProviderAnnotationSummary(Set<MediaType> consumes, Set<MediaType> produces)
      {
         this.consumes = consumes;
         this.produces = produces;
      }

      @Override
      public int hashCode()
      {
         int hash = 7;
         hash = hash * 31 + (consumes == null ? 0 : consumes.hashCode());
         hash = hash * 31 + (produces == null ? 0 : produces.hashCode());
         return hash;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
         {
            return true;
         }
         if (obj == null || getClass() != obj.getClass())
         {
            return false;
         }
         ProviderAnnotationSummary other = (ProviderAnnotationSummary)obj;
         if (consumes == null)
         {
            if (other.consumes != null)
            {
               return false;
            }
         }
         else if (!consumes.equals(other.consumes))
         {
            return false;
         }
         if (produces == null)
         {
            if (other.produces != null)
            {
               return false;
            }
         }
         else if (!produces.equals(other.produces))
         {
            return false;
         }
         return true;
      }
   }

   private static AnnotationSummary makeAnnotationSummary(Class<?> type, Class<?> itype)
   {
      if (type.isAnnotationPresent(Path.class))
      {
         return new ResourceAnnotationSummary(new UriPattern(type.getAnnotation(Path.class).value()));
      }
      else if (type.isAnnotationPresent(Provider.class))
      {
         // @Consumes makes sense for MessageBodyReader ONLY
         Set<MediaType> consumes = MessageBodyReader.class == itype //
            ? new HashSet<MediaType>(MediaTypeHelper.createConsumesList(type.getAnnotation(Consumes.class))) //
            : null;
         // @Produces makes sense for MessageBodyWriter or ContextResolver
         Set<MediaType> produces = ContextResolver.class == itype || MessageBodyWriter.class == itype//
         ? new HashSet<MediaType>(MediaTypeHelper.createProducesList(type.getAnnotation(Produces.class))) //
            : null;
         return new ProviderAnnotationSummary(consumes, produces);
      }
      else if (type.isAnnotationPresent(Filter.class))
      {
         return EmptyAnnotationSummary.INSTANCE;
      }
      throw new IllegalArgumentException();
   }

   /**
    * @see org.exoplatform.container.ConcurrentPicoContainer#registerComponent(org.picocontainer.ComponentAdapter)
    */
   @Override
   public ComponentAdapter registerComponent(ComponentAdapter componentAdapter)
      throws DuplicateComponentKeyRegistrationException
   {
      if (componentAdapter instanceof RestfulComponentAdapter)
      {
         Class<?> type = componentAdapter.getComponentImplementation();
         ParameterizedType[] implementedInterfaces =
            ((RestfulComponentAdapter)componentAdapter).getImplementedInterfaces();
         lock.lock();
         try
         {
            Map<Key, ComponentAdapter> copy = new HashMap<Key, ComponentAdapter>(restToComponentAdapters);
            if (implementedInterfaces.length > 0)
            {
               for (int i = 0; i < implementedInterfaces.length; i++)
               {
                  ParameterizedType genericInterface = implementedInterfaces[i];
                  Class<?> rawType = (Class<?>)genericInterface.getRawType();
                  ComponentAdapter previous =
                     copy.put(new Key(makeAnnotationSummary(type, rawType), genericInterface), componentAdapter);
                  if (previous != null)
                  {
                     throw new PicoRegistrationException("Cannot register component " + componentAdapter
                        + " because already registered component " + previous);
                  }
               }
            }
            else
            {
               ComponentAdapter previous = copy.put(new Key(makeAnnotationSummary(type, null)), componentAdapter);
               if (previous != null)
               {
                  throw new PicoRegistrationException("Cannot register component " + componentAdapter
                     + " because already registered component " + previous);
               }
            }
            super.registerComponent(componentAdapter);
            restToComponentAdapters = copy;
            return componentAdapter;
         }
         finally
         {
            lock.unlock();
         }
      }
      else
      {
         return super.registerComponent(componentAdapter);
      }
   }

   /**
    * @see org.exoplatform.container.ConcurrentPicoContainer#registerComponentInstance(java.lang.Object,
    *      java.lang.Object)
    */
   @Override
   public ComponentAdapter registerComponentInstance(Object componentKey, Object componentInstance)
      throws PicoRegistrationException
   {
      if (RestfulComponentAdapter.isRestfulComponent(componentInstance))
      {
         ComponentAdapter componentAdapter = new RestfulComponentAdapter(componentKey, componentInstance);
         registerComponent(componentAdapter);
         return componentAdapter;
      }
      return super.registerComponentInstance(componentKey, componentInstance);
   }

   /**
    * @see org.exoplatform.container.ConcurrentPicoContainer#unregisterComponent(java.lang.Object)
    */
   @Override
   public ComponentAdapter unregisterComponent(Object componentKey)
   {
      ComponentAdapter componentAdapter = super.unregisterComponent(componentKey);
      if (componentAdapter instanceof RestfulComponentAdapter)
      {
         Class<?> type = componentAdapter.getComponentImplementation();
         ParameterizedType[] implementedInterfaces =
            ((RestfulComponentAdapter)componentAdapter).getImplementedInterfaces();
         List<Key> keys;
         if (implementedInterfaces.length > 0)
         {
            keys = new ArrayList<RestfulContainer.Key>(implementedInterfaces.length);
            for (int i = 0; i < implementedInterfaces.length; i++)
            {
               ParameterizedType genericInterface = implementedInterfaces[i];
               Class<?> rawType = (Class<?>)genericInterface.getRawType();
               keys.add(new Key(makeAnnotationSummary(type, rawType), genericInterface));
            }
         }
         else
         {
            keys = Collections.singletonList(new Key(makeAnnotationSummary(type, null)));
         }

         lock.lock();
         try
         {
            Map<Key, ComponentAdapter> copy = new HashMap<Key, ComponentAdapter>(restToComponentAdapters);
            copy.keySet().removeAll(keys);
            restToComponentAdapters = copy;
         }
         finally
         {
            lock.unlock();
         }
      }
      return componentAdapter;
   }

   //

   /**
    * Retrieve all the component adapters for types annotated with <code>annotation</code> inside this container. The
    * component adapters from the parent container are not returned.
    * 
    * @param annotation the annotation type
    * @return a collection containing all the ComponentAdapters for types annotated with <code>annotation</code> inside
    *         this container.
    */
   @SuppressWarnings("unchecked")
   public List<ComponentAdapter> getComponentAdapters(Class<? extends Annotation> annotation)
   {
      Collection<ComponentAdapter> adapters = getComponentAdapters();
      if (adapters.size() > 0)
      {
         List<ComponentAdapter> result = new ArrayList<ComponentAdapter>();
         for (ComponentAdapter a : adapters)
         {
            if (a.getComponentImplementation().isAnnotationPresent(annotation))
            {
               result.add(a);
            }
         }
         return result;
      }
      return Collections.emptyList();
   }

   /**
    * Retrieve all the component adapters of the specified type and annotated with <code>annotation</code> inside this
    * container. The component adapters from the parent container are not returned.
    * 
    * @param componentType the type of component
    * @param annotation the annotation type
    * @return a collection containing all the ComponentAdapters of the specified type and annotated with
    *         <code>annotation</code> inside this container.
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   public List<ComponentAdapter> getComponentAdaptersOfType(Class componentType, Class<? extends Annotation> annotation)
   {
      List<ComponentAdapter> adapters = getComponentAdaptersOfType(componentType);
      if (adapters.size() > 0)
      {
         List<ComponentAdapter> result = new ArrayList<ComponentAdapter>();
         for (ComponentAdapter a : adapters)
         {
            if (a.getComponentImplementation().isAnnotationPresent(annotation))
            {
               result.add(a);
            }
         }
         return result;
      }
      return Collections.emptyList();
   }

   /**
    * Retrieve all the component instances of the specified type and annotated with <code>annotation</code>.
    * 
    * @param componentType the type of component
    * @param annotation the annotation type
    * @return a collection of components
    */
   public <T> List<T> getComponentsOfType(Class<T> componentType, Class<? extends Annotation> annotation)
   {
      List<?> instances = getComponentInstancesOfType(componentType);
      if (instances.size() > 0)
      {
         List<T> result = new ArrayList<T>();
         for (Object o : instances)
         {
            if (o.getClass().isAnnotationPresent(annotation))
            {
               result.add(componentType.cast(o));
            }
         }
         return result;
      }
      return Collections.emptyList();
   }

   /**
    * Retrieve all the component instances annotated with <code>annotation</code>.
    * 
    * @param annotation the annotation type
    * @return a collection of components
    */
   public List<Object> getComponents(Class<? extends Annotation> annotation)
   {
      List<?> instances = getComponentInstances();
      if (instances.size() > 0)
      {
         List<Object> result = new ArrayList<Object>();
         for (Object o : instances)
         {
            if (o.getClass().isAnnotationPresent(annotation))
            {
               result.add(o);
            }
         }
         return result;
      }
      return Collections.emptyList();
   }

   // ------- Resources --------

   /**
    * Get root resource matched to <code>requestPath</code>.
    * 
    * @param requestPath request path
    * @param parameterValues list for placing values of URI templates
    * @return root resource matched to <code>requestPath</code> or <code>null</code>
    */
   public Object getMatchedResource(String requestPath, List<String> parameterValues)
   {
      return ComponentsFinder.findResource(this, requestPath, parameterValues);
   }

   // -------- Providers --------

   /**
    * @see javax.ws.rs.ext.Providers#getMessageBodyReader(java.lang.Class, java.lang.reflect.Type,
    *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
    */
   @Override
   public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type, Type genericType, Annotation[] annotations,
      MediaType mediaType)
   {
      return (MessageBodyReader<T>)ComponentsFinder.findReader(this, type, genericType, annotations, mediaType);
   }

   /**
    * @see javax.ws.rs.ext.Providers#getMessageBodyWriter(java.lang.Class, java.lang.reflect.Type,
    *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
    */
   @Override
   public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type, Type genericType, Annotation[] annotations,
      MediaType mediaType)
   {
      return (MessageBodyWriter<T>)ComponentsFinder.findWriter(this, type, genericType, annotations, mediaType);
   }

   /**
    * @see javax.ws.rs.ext.Providers#getExceptionMapper(java.lang.Class)
    */
   @Override
   public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> type)
   {
      return ComponentsFinder.findExceptionMapper(this, type);
   }

   /**
    * @see javax.ws.rs.ext.Providers#getContextResolver(java.lang.Class, javax.ws.rs.core.MediaType)
    */
   @Override
   public <T> ContextResolver<T> getContextResolver(Class<T> contextType, MediaType mediaType)
   {
      return ComponentsFinder.findContextResolver(this, contextType, mediaType);
   }
}
