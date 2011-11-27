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

import org.everrest.core.impl.header.MediaTypeHelper;
import org.everrest.core.provider.ProviderDescriptor;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.uri.UriPattern;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.AbstractPicoVisitor;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
abstract class ComponentsFinder extends AbstractPicoVisitor
{
   final Class<? extends Annotation> annotation;

   ComponentsFinder(Class<? extends Annotation> annotation)
   {
      this.annotation = annotation;
   }

   /**
    * @see org.picocontainer.PicoVisitor#visitComponentAdapter(org.picocontainer.ComponentAdapter)
    */
   @Override
   public final void visitComponentAdapter(ComponentAdapter componentAdapter)
   {
      checkTraversal();
   }

   /**
    * @see org.picocontainer.PicoVisitor#visitParameter(org.picocontainer.Parameter)
    */
   @Override
   public final void visitParameter(Parameter parameter)
   {
      checkTraversal();
   }

   final static class ResourceFinder extends ComponentsFinder
   {
      private final String requestPath;
      private final List<String> parameterValues;
      private final List<ComponentAdapter> components = new ArrayList<ComponentAdapter>();

      ResourceFinder(Class<? extends Annotation> annotation, String requestPath, List<String> parameterValues)
      {
         super(annotation);
         this.requestPath = requestPath;
         this.parameterValues = parameterValues;
      }

      /**
       * @see org.picocontainer.PicoVisitor#traverse(java.lang.Object)
       */
      @Override
      public Object traverse(Object container)
      {
         components.clear();
         try
         {
            super.traverse(container);
            Map<UriPattern, ComponentAdapter> matched = new HashMap<UriPattern, ComponentAdapter>();
            for (ComponentAdapter adapter : components)
            {
               if (adapter instanceof RestfulComponentAdapter)
               {
                  AbstractResourceDescriptor resource =
                     (AbstractResourceDescriptor)((RestfulComponentAdapter)adapter).getObjectModel();
                  if (resource.getUriPattern().match(requestPath, parameterValues))
                  {
                     String tail = parameterValues.get(parameterValues.size() - 1);
                     if (tail == null //
                        || "/".equals(tail) //
                        || (resource.getSubResourceMethods().size() + resource.getSubResourceLocators().size()) > 0)
                     {
                        matched.put(resource.getUriPattern(), adapter);
                     }
                  }
               }
            }
            if (matched.isEmpty())
            {
               return null;
            }
            else if (matched.size() == 1)
            {
               ComponentAdapter componentAdapter = matched.values().iterator().next();
               matched.clear();
               return componentAdapter.getComponentInstance((PicoContainer)container);
            }
            else
            {
               UriPattern[] keys = matched.keySet().toArray(new UriPattern[matched.size()]);
               Arrays.sort(keys, UriPattern.URIPATTERN_COMPARATOR);
               ComponentAdapter componentAdapter = matched.get(keys[0]);
               matched.clear();
               return componentAdapter.getComponentInstance((PicoContainer)container);
            }
         }
         finally
         {
            components.clear();
         }
      }

      /**
       * @see org.picocontainer.PicoVisitor#visitContainer(org.picocontainer.PicoContainer)
       */
      @Override
      public void visitContainer(PicoContainer pico)
      {
         checkTraversal();
         components.addAll(((RestfulContainer)pico).getComponentAdapters(annotation));
      }
   }

   final static class WriterFinder<T> extends ComponentsFinder
   {
      private final Class<T> entityType;
      private final Type genericEntityType;
      private final MediaType mediaType;
      private final Annotation[] annotations;
      private final List<ComponentAdapter> components = new ArrayList<ComponentAdapter>();

      WriterFinder(Class<? extends Annotation> annotation, Class<T> entityType, Type genericEntityType,
         Annotation[] annotations, MediaType mediaType)
      {
         super(annotation);
         this.entityType = entityType;
         this.genericEntityType = genericEntityType;
         this.annotations = annotations;
         this.mediaType = mediaType;
      }

      /**
       * @see org.picocontainer.defaults.AbstractPicoVisitor#traverse(java.lang.Object)
       */
      @SuppressWarnings({"rawtypes", "unchecked"})
      @Override
      public Object traverse(Object container)
      {
         components.clear();
         try
         {
            super.traverse(container);
            MediaTypeHelper.MediaTypeRange mediaTypeRange = new MediaTypeHelper.MediaTypeRange(mediaType);
            while (mediaTypeRange.hasNext())
            {
               MediaType next = mediaTypeRange.next();
               for (ComponentAdapter adapter : components)
               {
                  if (adapter instanceof RestfulComponentAdapter)
                  {
                     ProviderDescriptor provider =
                        (ProviderDescriptor)((RestfulComponentAdapter)adapter).getObjectModel();
                     if (provider.produces().contains(next))
                     {
                        MessageBodyWriter writer =
                           (MessageBodyWriter)adapter.getComponentInstance((PicoContainer)container);
                        if (writer.isWriteable(entityType, genericEntityType, annotations, next))
                        {
                           return writer;
                        }
                     }
                  }
               }
            }
            return null;
         }
         finally
         {
            components.clear();
         }
      }

      /**
       * @see org.picocontainer.PicoVisitor#visitContainer(org.picocontainer.PicoContainer)
       */
      @Override
      public void visitContainer(PicoContainer pico)
      {
         checkTraversal();
         components.addAll(((RestfulContainer)pico).getComponentAdaptersOfType(MessageBodyWriter.class, annotation));
      }
   }

   final static class ReaderFinder<T> extends ComponentsFinder
   {
      private final Class<T> entityType;
      private final Type genericEntityType;
      private final MediaType mediaType;
      private final Annotation[] annotations;
      private final List<ComponentAdapter> components = new ArrayList<ComponentAdapter>();

      ReaderFinder(Class<? extends Annotation> annotation, Class<T> entityType, Type genericEntityType,
         Annotation[] annotations, MediaType mediaType)
      {
         super(annotation);
         this.entityType = entityType;
         this.genericEntityType = genericEntityType;
         this.annotations = annotations;
         this.mediaType = mediaType;
      }

      /**
       * @see org.picocontainer.defaults.AbstractPicoVisitor#traverse(java.lang.Object)
       */
      @SuppressWarnings({"rawtypes", "unchecked"})
      @Override
      public Object traverse(Object container)
      {
         components.clear();
         try
         {
            super.traverse(container);
            MediaTypeHelper.MediaTypeRange mediaTypeRange = new MediaTypeHelper.MediaTypeRange(mediaType);
            while (mediaTypeRange.hasNext())
            {
               MediaType next = mediaTypeRange.next();
               for (ComponentAdapter adapter : components)
               {
                  if (adapter instanceof RestfulComponentAdapter)
                  {
                     ProviderDescriptor provider =
                        (ProviderDescriptor)((RestfulComponentAdapter)adapter).getObjectModel();
                     if (provider.consumes().contains(next))
                     {
                        MessageBodyReader reader =
                           (MessageBodyReader)adapter.getComponentInstance((PicoContainer)container);
                        if (reader.isReadable(entityType, genericEntityType, annotations, next))
                        {
                           return reader;
                        }
                     }
                  }
               }
            }
            return null;
         }
         finally
         {
            components.clear();
         }
      }

      /**
       * @see org.picocontainer.PicoVisitor#visitContainer(org.picocontainer.PicoContainer)
       */
      @Override
      public void visitContainer(PicoContainer pico)
      {
         checkTraversal();
         components.addAll(((RestfulContainer)pico).getComponentAdaptersOfType(MessageBodyReader.class, annotation));
      }
   }

   final static class ExceptionMapperFinder<T> extends ComponentsFinder
   {
      private final Class<T> exceptionType;
      private final List<ComponentAdapter> components = new ArrayList<ComponentAdapter>();

      ExceptionMapperFinder(Class<? extends Annotation> annotation, Class<T> exceptionType)
      {
         super(annotation);
         this.exceptionType = exceptionType;
      }

      /**
       * @see org.picocontainer.defaults.AbstractPicoVisitor#traverse(java.lang.Object)
       */
      @Override
      public Object traverse(Object container)
      {
         components.clear();
         try
         {
            super.traverse(container);
            for (ComponentAdapter adapter : components)
            {
               if (adapter instanceof RestfulComponentAdapter)
               {
                  ParameterizedType[] implementedInterfaces =
                     ((RestfulComponentAdapter)adapter).getImplementedInterfaces();
                  for (int i = 0; i < implementedInterfaces.length; i++)
                  {
                     ParameterizedType genericInterface = implementedInterfaces[i];
                     if (ExceptionMapper.class == genericInterface.getRawType()
                        && exceptionType == genericInterface.getActualTypeArguments()[0])
                     {
                        return adapter.getComponentInstance((PicoContainer)container);
                     }
                  }
               }
            }
            return null;
         }
         finally
         {
            components.clear();
         }
      }

      /**
       * @see org.picocontainer.PicoVisitor#visitContainer(org.picocontainer.PicoContainer)
       */
      @Override
      public void visitContainer(PicoContainer pico)
      {
         checkTraversal();
         components.addAll(((RestfulContainer)pico).getComponentAdaptersOfType(ExceptionMapper.class, annotation));
      }
   }

   final static class ContextResolverFinder<T> extends ComponentsFinder
   {
      private final Class<T> contextType;
      private final MediaType mediaType;
      private final List<ComponentAdapter> components = new ArrayList<ComponentAdapter>();

      ContextResolverFinder(Class<? extends Annotation> annotation, Class<T> contextType, MediaType mediaType)
      {
         super(annotation);
         this.contextType = contextType;
         this.mediaType = mediaType;
      }

      /**
       * @see org.picocontainer.defaults.AbstractPicoVisitor#traverse(java.lang.Object)
       */
      @Override
      public Object traverse(Object container)
      {
         components.clear();
         try
         {
            super.traverse(container);
            MediaTypeHelper.MediaTypeRange mediaTypeRange = new MediaTypeHelper.MediaTypeRange(mediaType);
            while (mediaTypeRange.hasNext())
            {
               MediaType next = mediaTypeRange.next();
               for (ComponentAdapter adapter : components)
               {
                  if (adapter instanceof RestfulComponentAdapter)
                  {
                     ProviderDescriptor provider =
                        (ProviderDescriptor)((RestfulComponentAdapter)adapter).getObjectModel();
                     if (provider.produces().contains(next))
                     {
                        ParameterizedType[] implementedInterfaces =
                           ((RestfulComponentAdapter)adapter).getImplementedInterfaces();
                        for (int i = 0; i < implementedInterfaces.length; i++)
                        {
                           ParameterizedType genericInterface = implementedInterfaces[i];
                           Type rawType = genericInterface.getRawType();
                           Class<?> actualType;
                           try
                           {
                              actualType = (Class<?>)genericInterface.getActualTypeArguments()[0];
                           }
                           catch (ClassCastException e)
                           {
                              continue;
                           }
                           if (ContextResolver.class == rawType && contextType.isAssignableFrom(actualType))
                           {
                              return adapter.getComponentInstance((PicoContainer)container);
                           }
                        }
                     }
                  }
               }
            }
            return null;
         }
         finally
         {
            components.clear();
         }
      }

      /**
       * @see org.picocontainer.PicoVisitor#visitContainer(org.picocontainer.PicoContainer)
       */
      @Override
      public void visitContainer(PicoContainer pico)
      {
         checkTraversal();
         components.addAll(((RestfulContainer)pico).getComponentAdaptersOfType(ContextResolver.class, annotation));
      }
   }

   static Object findResource(PicoContainer pico, String requestPath, List<String> parameterValues)
   {
      return new ResourceFinder(Path.class, requestPath, parameterValues).traverse(pico);
   }

   @SuppressWarnings("unchecked")
   static <T> MessageBodyWriter<T> findWriter(PicoContainer pico, Class<T> entityType, Type genericEntityType,
      Annotation[] annotations, MediaType mediaType)
   {
      return (MessageBodyWriter<T>)new WriterFinder<T>(Provider.class, entityType, genericEntityType, annotations,
         mediaType).traverse(pico);
   }

   @SuppressWarnings("unchecked")
   static <T> MessageBodyReader<T> findReader(PicoContainer pico, Class<T> entityType, Type genericEntityType,
      Annotation[] annotations, MediaType mediaType)
   {
      return (MessageBodyReader<T>)new ReaderFinder<T>(Provider.class, entityType, genericEntityType, annotations,
         mediaType).traverse(pico);
   }

   @SuppressWarnings("unchecked")
   static <T extends Throwable> ExceptionMapper<T> findExceptionMapper(PicoContainer pico, Class<T> exceptionType)
   {
      return (ExceptionMapper<T>)new ExceptionMapperFinder<T>(Provider.class, exceptionType).traverse(pico);
   }

   @SuppressWarnings("unchecked")
   static <T> ContextResolver<T> findContextResolver(PicoContainer pico, Class<T> contextType, MediaType mediaType)
   {
      return (ContextResolver<T>)new ContextResolverFinder<T>(Provider.class, contextType, mediaType).traverse(pico);
   }
}
