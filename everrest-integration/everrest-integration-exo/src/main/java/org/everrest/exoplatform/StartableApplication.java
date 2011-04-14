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
package org.everrest.exoplatform;

import org.everrest.core.Filter;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResponseFilter;
import org.everrest.core.method.MethodInvokerFilter;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.Startable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * Purpose of this component is deliver all JAX-RS components lookups in ExoContainer (instances of classes annotated
 * with {@link Path}, {@link Provider} and {@link Filter}). All components considered as singleton Resources and
 * Providers, see {@link #getSingletons()}.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class StartableApplication extends Application implements Startable
{
   private final ExoContainer container;

   private final Set<Class<?>> cls = Collections.emptySet();

   private final Set<Object> singletons = new HashSet<Object>();

   public StartableApplication(ExoContainerContext containerContext)
   {
      container = containerContext.getContainer();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Set<Class<?>> getClasses()
   {
      return cls;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Set<Object> getSingletons()
   {
      return singletons;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   @Override
   public void start()
   {
      Collection adapters = container.getComponentAdapters();
      if (adapters != null && !adapters.isEmpty())
      {
         // Assume all components loaded from ExoContainer are singleton (it is common behavior for ExoContainer).
         // If need more per-request component then use javax.ws.rs.core.Application for deploy.
         for (Iterator iter = adapters.iterator(); iter.hasNext();)
         {
            ComponentAdapter cadapter = (ComponentAdapter)iter.next();
            Class clazz = cadapter.getComponentImplementation();
            if (clazz.getAnnotation(Provider.class) != null)
            {
               if (ContextResolver.class.isAssignableFrom(clazz))
                  singletons.add(cadapter.getComponentInstance(container));
               if (ExceptionMapper.class.isAssignableFrom(clazz))
                  singletons.add(cadapter.getComponentInstance(container));
               if (MessageBodyReader.class.isAssignableFrom(clazz))
                  singletons.add(cadapter.getComponentInstance(container));
               if (MessageBodyWriter.class.isAssignableFrom(clazz))
                  singletons.add(cadapter.getComponentInstance(container));
            }
            else if (clazz.getAnnotation(Filter.class) != null)
            {
               if (MethodInvokerFilter.class.isAssignableFrom(clazz))
                  singletons.add(cadapter.getComponentInstance(container));
               if (RequestFilter.class.isAssignableFrom(clazz))
                  singletons.add(cadapter.getComponentInstance(container));
               if (ResponseFilter.class.isAssignableFrom(clazz))
                  singletons.add(cadapter.getComponentInstance(container));
            }
            else if (clazz.getAnnotation(Path.class) != null)
            {
               singletons.add(cadapter.getComponentInstance(container));
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void stop()
   {
   }
}
