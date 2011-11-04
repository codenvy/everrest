/**
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.everrest.spring;

import org.everrest.core.DependencySupplier;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.FileCollectorDestroyer;
import org.everrest.core.impl.InternalException;
import org.everrest.core.impl.LifecycleComponent;
import org.everrest.core.servlet.EverrestApplication;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;

/**
 * HandlerMapping for EverrestProcessor.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class EverrestHandlerMapping implements HandlerMapping, BeanFactoryPostProcessor
{
   private static final class LifecycleDestroyer implements org.springframework.context.Lifecycle
   {
      private final List<WeakReference<Object>> toDestroy;
      private final AtomicBoolean started = new AtomicBoolean(true);

      private LifecycleDestroyer(List<WeakReference<Object>> toDestroy)
      {
         this.toDestroy = toDestroy;
      }

      @Override
      public void start()
      {
      }

      @Override
      public void stop()
      {
         if (toDestroy != null && toDestroy.size() > 0)
         {
            RuntimeException exception = null;
            for (WeakReference<Object> ref : toDestroy)
            {
               Object o = ref.get();
               if (o != null)
               {
                  try
                  {
                     new LifecycleComponent(o).destroy();
                  }
                  catch (WebApplicationException e)
                  {
                     if (exception == null)
                        exception = e;
                  }
                  catch (InternalException e)
                  {
                     if (exception == null)
                        exception = e;
                  }
               }
            }
            toDestroy.clear();
            started.set(false);
            if (exception != null)
               throw exception;
         }
         else
         {
            started.set(false);
         }
      }

      @Override
      public boolean isRunning()
      {
         return started.get();
      }
   }

   private static final class SpringFileCollectorDestroyer extends FileCollectorDestroyer implements
      org.springframework.context.Lifecycle
   {
      protected final AtomicBoolean started = new AtomicBoolean(true);

      @Override
      public void start()
      {
      }

      @Override
      public boolean isRunning()
      {
         return started.get();
      }

      @Override
      public void stop()
      {
         stopFileCollector();
      }
   }

   protected EverrestProcessor processor;
   protected ResourceBinder resources;
   protected ApplicationProviderBinder providers;
   protected EverrestConfiguration configuration;

   protected EverrestHandlerMapping()
   {
   }

   /**
    * @param resources resources
    * @param providers providers
    * @param dependencies dependency resolver
    */
   public EverrestHandlerMapping(ResourceBinder resources, ApplicationProviderBinder providers,
      DependencySupplier dependencies)
   {
      this(resources, providers, new EverrestConfiguration(), dependencies);
   }

   /**
    * @param resources resources
    * @param providers providers
    * @param configuration EverRest framework configuration
    * @param dependencies dependency resolver
    */
   public EverrestHandlerMapping(ResourceBinder resources, ApplicationProviderBinder providers,
      EverrestConfiguration configuration, DependencySupplier dependencies)
   {
      this.resources = resources;
      this.providers = providers;
      this.configuration = configuration;
      this.processor = new EverrestProcessor(resources, providers, dependencies, configuration, null);
   }

   /**
    * {@inheritDoc}
    */
   public HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception
   {
      return new HandlerExecutionChain(getProcessor());
   }

   protected EverrestProcessor getProcessor()
   {
      return processor;
   }

   /**
    * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
    */
   @Override
   public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
   {
      EverrestApplication everrest = new EverrestApplication(configuration);
      Set<Object> singletons = everrest.getSingletons();
      // Do not prevent GC remove objects if they are removed somehow from ResourceBinder or ProviderBinder.
      // NOTE We provider life cycle control ONLY for components loaded via Application and do nothing for components
      // obtained from container. Container must take care about its components.  
      List<WeakReference<Object>> l = new ArrayList<WeakReference<Object>>(singletons.size());
      for (Object o : singletons)
         l.add(new WeakReference<Object>(o));
      beanFactory.registerSingleton("org.everrest.lifecycle.Singletons", new LifecycleDestroyer(l));
      beanFactory.registerSingleton("org.everrest.lifecycle.FileCollectorDestroyer", makeFileCollectorDestroyer());
      processor.addApplication(everrest);
   }

   protected org.springframework.context.Lifecycle makeFileCollectorDestroyer()
   {
      return new SpringFileCollectorDestroyer();
   }
}
