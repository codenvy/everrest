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
 * Container intended for decoupling third part code and everrest framework. Components and adapters are searchable by
 * annotation and type or by annotation only. Components (implementation class or instance) annotated with JAX-RS
 * annotations &#64;Path and &#64;Provider should be registered in container as usually. After registration if class
 * annotated with {@link javax.ws.rs.Path} it may be retrieved by method {@link #getMatchedResource(String, List)}. If
 * class annotated with {@link javax.ws.rs.ext.Provider} and implement one of JAX-RS extension interfaces instance of
 * component may be retrieved by corresponded methods of {@link javax.ws.rs.ext.Providers} interface. E.g:
 * <p>
 * Suppose we have a resource class org.example.MyResource annotated with &#64;Path(&quot;my-resource&quot;) and base
 * URL is <code>http://example.com/</code>:
 * </p>
 * 
 * <pre>
 * &#064;Path(&quot;my-resource&quot;)
 * public class MyResource
 * {
 *    &#064;GET
 *    &#064;Path(&quot;{id}&quot;)
 *    public String get()
 *    {
 *    ...
 *    }
 * }
 * </pre>
 * 
 * Need register resource in container.
 * 
 * <pre>
 * RestfulContainer container = ...
 * container.registerComponentImplementation(MyResource.class);
 * </pre>
 * 
 * Suppose we have a GET request for <code>http://example.com/my-resource/101</code>. We need to find resource matched
 * to relative path <code>/my-resource/101</code>.
 * 
 * <pre>
 * List&lt;String&gt; paramValues = new ArrayList&lt;String&gt;();
 * Object resource = container.getMatchedResource(&quot;/my-resource/101&quot;, paramValues);
 * ...
 * </pre>
 * 
 * Resource should be the instance of org.example.MyResource.class. Container supports injection JAX-RS runtime
 * information into a class field or constructor parameters with annotation {@link javax.ws.rs.core.Context}.
 * <p>
 * <span style="font-weight: bold">NOTE</span> Methods <code>registerXXX</code> of this container may throws
 * {@link PicoRegistrationException} if component violates the restrictions of framework, e.g. if resource with the same
 * URI pattern or provider with the same purpose already registered in this container.
 * </p>
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 * 
 * @see javax.ws.rs.core.Context
 * @see MessageBodyReader
 * @see MessageBodyWriter
 * @see ContextResolver
 * @see ExceptionMapper
 */
@SuppressWarnings("serial")
public class RestfulContainer extends ConcurrentPicoContainer implements Providers
{
   /*public static RestfulContainer getInstance(String name)
   {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      RestfulContainer restfulContainer = (RestfulContainer)container.getComponentInstance(name);
      if (restfulContainer == null)
      {
         restfulContainer = new RestfulContainer();
         container.registerComponentInstance(name, restfulContainer);
      }
      return restfulContainer;
   }*/

   public RestfulContainer()
   {
      this(new DefaultComponentAdapterFactory(), null);
   }

   protected RestfulContainer(PicoContainer parent)
   {
      this(new DefaultComponentAdapterFactory(), parent);
   }

   protected RestfulContainer(ComponentAdapterFactory factory, PicoContainer parent)
   {
      super(wrapComponentAdapterFactory(factory), parent);
   }

   private static ComponentAdapterFactory wrapComponentAdapterFactory(ComponentAdapterFactory componentAdapterFactory)
   {
      return new RestfulComponentAdapterFactory(componentAdapterFactory);
   }

   private volatile Map<Key, ComponentAdapter> restToComponentAdapters = new HashMap<Key, ComponentAdapter>();
   private final Lock lock = new ReentrantLock();

   private static final class ProviderKey implements Key
   {
      private final Type type;
      private final Set<MediaType> consumes;
      private final Set<MediaType> produces;

      ProviderKey(Set<MediaType> consumes, Set<MediaType> produces, Type type)
      {
         this.consumes = consumes;
         this.produces = produces;
         this.type = type;
      }

      @Override
      public int hashCode()
      {
         int hash = 7;
         hash = hash * 31 + (consumes == null ? 0 : consumes.hashCode());
         hash = hash * 31 + (produces == null ? 0 : produces.hashCode());
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
         ProviderKey other = (ProviderKey)obj;
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

   private static final class ResourceKey implements Key
   {
      private final UriPattern uriPattern;

      ResourceKey(UriPattern uriPattern)
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
         return uriPattern.equals(((ResourceKey)obj).uriPattern);
      }
   }

   /**
    * Use such key in <code>restToComponentAdapters</code> to avoid duplicate restful components. It is not enough to
    * use just keys of component adapters since some resources or providers may be not unique for the rest framework,
    * e.g. :
    * <ul>
    * <li>resource classes may be different but may have the same or matched value of &#64;Path annotation</li>
    * <li>two ExceptionMapper may process the same type of Exception</li>
    * <li>...</li>
    * </ul>
    */
   private static interface Key
   {
   }

   private static List<Key> makeKeys(RestfulComponentAdapter componentAdapter)
   {
      Class<?> type = componentAdapter.getComponentImplementation();
      if (type.isAnnotationPresent(Filter.class))
      {
         // TODO
      }
      else if (type.isAnnotationPresent(Path.class))
      {
         List<Key> keys = new ArrayList<Key>(1);
         keys.add(new ResourceKey(new UriPattern(type.getAnnotation(Path.class).value())));
         return keys;
      }
      else if (type.isAnnotationPresent(Provider.class))
      {
         ParameterizedType[] implementedInterfaces =
            ((RestfulComponentAdapter)componentAdapter).getImplementedInterfaces();
         List<Key> keys = new ArrayList<Key>(implementedInterfaces.length);
         for (int i = 0; i < implementedInterfaces.length; i++)
         {
            ParameterizedType genericInterface = implementedInterfaces[i];
            Class<?> rawType = (Class<?>)genericInterface.getRawType();
            // @Consumes makes sense for MessageBodyReader ONLY
            Set<MediaType> consumes = MessageBodyReader.class == rawType //
               ? new HashSet<MediaType>(MediaTypeHelper.createConsumesList(type.getAnnotation(Consumes.class))) //
               : null;
            // @Produces makes sense for MessageBodyWriter or ContextResolver
            Set<MediaType> produces = ContextResolver.class == rawType || MessageBodyWriter.class == rawType//
            ? new HashSet<MediaType>(MediaTypeHelper.createProducesList(type.getAnnotation(Produces.class))) //
               : null;
            keys.add(new ProviderKey(consumes, produces, genericInterface));
         }
         return keys;
      }
      return Collections.emptyList();
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
         List<Key> keys = makeKeys((RestfulComponentAdapter)componentAdapter);
         if (keys.size() > 0)
         {
            lock.lock();
            try
            {
               Map<Key, ComponentAdapter> copy = new HashMap<Key, ComponentAdapter>(restToComponentAdapters);
               for (Key key : keys)
               {
                  ComponentAdapter previous = copy.put(key, componentAdapter);
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
      }
      return super.registerComponent(componentAdapter);
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
         List<Key> keys = makeKeys((RestfulComponentAdapter)componentAdapter);
         if (keys.size() > 0)
         {
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
    * Get ComponentAdapter root resource matched to <code>requestPath</code>.
    * 
    * @param requestPath request path
    * @param parameterValues list for placing values of URI templates
    * @return root resource matched to <code>requestPath</code> or <code>null</code>
    */
   public final ComponentAdapter getMatchedResource(String requestPath, List<String> parameterValues)
   {
      return ComponentsFinder.findResource(this, requestPath, parameterValues);
   }

   // -------- Providers --------

   /**
    * @see javax.ws.rs.ext.Providers#getMessageBodyReader(java.lang.Class, java.lang.reflect.Type,
    *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
    */
   @Override
   public final <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type, Type genericType, Annotation[] annotations,
      MediaType mediaType)
   {
      return (MessageBodyReader<T>)ComponentsFinder.findReader(this, type, genericType, annotations, mediaType);
   }

   /**
    * @see javax.ws.rs.ext.Providers#getMessageBodyWriter(java.lang.Class, java.lang.reflect.Type,
    *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
    */
   @Override
   public final <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type, Type genericType, Annotation[] annotations,
      MediaType mediaType)
   {
      return (MessageBodyWriter<T>)ComponentsFinder.findWriter(this, type, genericType, annotations, mediaType);
   }

   /**
    * @see javax.ws.rs.ext.Providers#getExceptionMapper(java.lang.Class)
    */
   @Override
   public final <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> type)
   {
      return ComponentsFinder.findExceptionMapper(this, type);
   }

   /**
    * @see javax.ws.rs.ext.Providers#getContextResolver(java.lang.Class, javax.ws.rs.core.MediaType)
    */
   @Override
   public final <T> ContextResolver<T> getContextResolver(Class<T> contextType, MediaType mediaType)
   {
      return ComponentsFinder.findContextResolver(this, contextType, mediaType);
   }
}
