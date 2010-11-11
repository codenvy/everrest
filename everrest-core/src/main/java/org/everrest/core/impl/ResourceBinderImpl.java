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

import org.everrest.core.ApplicationContext;
import org.everrest.core.ComponentLifecycleScope;
import org.everrest.core.ObjectFactory;
import org.everrest.core.PerRequestObjectFactory;
import org.everrest.core.ResourceBinder;
import org.everrest.core.ResourcePublicationException;
import org.everrest.core.SingletonObjectFactory;
import org.everrest.core.impl.resource.AbstractResourceDescriptorImpl;
import org.everrest.core.impl.resource.ResourceDescriptorValidator;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.resource.ResourceDescriptorVisitor;
import org.everrest.core.uri.UriPattern;
import org.everrest.core.util.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class ResourceBinderImpl implements ResourceBinder
{

   /**
    * Name of property which may contains resource expiration date. Date
    * expected as string representation of java.util.Date in long format.
    */
   public static final String RESOURCE_EXPIRED = "org.everrest.resource.expiration.date";

   /** Logger. */
   private static final Logger LOG = Logger.getLogger(ResourceBinderImpl.class);

   /** Resource's comparator. */
   protected static final Comparator<ObjectFactory<AbstractResourceDescriptor>> RESOURCE_COMPARATOR =
      new Comparator<ObjectFactory<AbstractResourceDescriptor>>()
      {
         /**
          * Compare two ResourceClass for order.
          *
          * @param o1 first ResourceClass to be compared
          * @param o2 second ResourceClass to be compared
          * @return positive , zero or negative dependent of {@link UriPattern}
          *         comparison
          * @see Comparator#compare(Object, Object)
          * @see UriPattern
          * @see UriPattern#URIPATTERN_COMPARATOR
          */
         public int compare(ObjectFactory<AbstractResourceDescriptor> o1, ObjectFactory<AbstractResourceDescriptor> o2)
         {
            return UriPattern.URIPATTERN_COMPARATOR.compare(o1.getObjectModel().getUriPattern(), o2.getObjectModel()
               .getUriPattern());
         }
      };

   protected boolean cleanerStop = false;

   protected class ResourceCleaner implements Runnable
   {

      private final int cleanerDelay;

      /**
       * @param cleanerDelay cleaner process delay in seconds
       */
      public ResourceCleaner(int cleanerDelay)
      {
         this.cleanerDelay = cleanerDelay;
      }

      public void run()
      {
         while (!cleanerStop)
         {
            try
            {
               Thread.sleep(cleanerDelay * 1000L);
            }
            catch (InterruptedException e)
            {
               ;
            }
            if (!cleanerStop)
            {
               processResources();
            }
         }
      }

      protected void processResources()
      {
         if (LOG.isDebugEnabled())
         {
            LOG.debug("Start resource cleaner");
         }

         synchronized (rootResources)
         {
            for (Iterator<ObjectFactory<AbstractResourceDescriptor>> iter = rootResources.iterator(); iter.hasNext();)
            {
               ObjectFactory<AbstractResourceDescriptor> next = iter.next();
               List<String> str = next.getObjectModel().getProperty(RESOURCE_EXPIRED);
               long expirationDate = -1;
               if (str != null && str.size() > 0)
               {
                  try
                  {
                     expirationDate = Long.parseLong(str.get(0));
                  }
                  catch (NumberFormatException e)
                  {
                     ;
                  }
               }
               if (expirationDate > 0 && expirationDate < System.currentTimeMillis())
               {
                  iter.remove();
                  for (ResourceListener listener : resourceListeners)
                  {
                     listener.resourceRemoved(next.getObjectModel());
                  }
                  if (LOG.isDebugEnabled())
                  {
                     LOG.debug("Remove expired resource: " + next.getObjectModel());
                  }
               }
            }
         }
      }
   }

   /** Root resource descriptors. */
   protected final List<ObjectFactory<AbstractResourceDescriptor>> rootResources =
      new ArrayList<ObjectFactory<AbstractResourceDescriptor>>();

   /** Validator. */
   protected final ResourceDescriptorVisitor rdv = ResourceDescriptorValidator.getInstance();

   /** Resource listeners. */
   protected final List<ResourceListener> resourceListeners = new ArrayList<ResourceListener>();

   public ResourceBinderImpl()
   {
      // Initialize RuntimeDelegate instance
      // This is first component in life cycle what needs.
      // TODO better solution to initialize RuntimeDelegate
      RuntimeDelegate rd = new RuntimeDelegateImpl();
      RuntimeDelegate.setInstance(rd);
   }

   public void addResource(final Class<?> resourceClass, MultivaluedMap<String, String> properties)
   {
      Path path = resourceClass.getAnnotation(Path.class);
      if (path == null)
      {
         throw new ResourcePublicationException("Resource class " + resourceClass.getName()
            + " it is not root resource. " + "Path annotation javax.ws.rs.Path is not specified for this class.");
      }
      try
      {
         AbstractResourceDescriptor descriptor =
            new AbstractResourceDescriptorImpl(resourceClass, ComponentLifecycleScope.PER_REQUEST);
         // validate AbstractResourceDescriptor
         descriptor.accept(rdv);
         if (properties != null)
            descriptor.getProperties().putAll(properties);
         addResource(new PerRequestObjectFactory<AbstractResourceDescriptor>(descriptor));
      }
      catch (Exception e)
      {
         throw new ResourcePublicationException(e.getMessage());
      }
   }

   public void addResource(final Object resource, MultivaluedMap<String, String> properties)
   {
      Path path = resource.getClass().getAnnotation(Path.class);
      if (path == null)
      {
         throw new ResourcePublicationException("Resource class " + resource.getClass().getName()
            + " it is not root resource. " + "Path annotation javax.ws.rs.Path is not specified for this class.");
      }
      try
      {
         AbstractResourceDescriptor descriptor =
            new AbstractResourceDescriptorImpl(resource.getClass(), ComponentLifecycleScope.SINGLETON);
         // validate AbstractResourceDescriptor
         descriptor.accept(rdv);
         if (properties != null)
            descriptor.getProperties().putAll(properties);
         addResource(new SingletonObjectFactory<AbstractResourceDescriptor>(descriptor, resource));
      }
      catch (Exception e)
      {
         throw new ResourcePublicationException(e.getMessage());
      }
   }

   public void addResource(final ObjectFactory<AbstractResourceDescriptor> resourceFactory)
   {
      UriPattern pattern = resourceFactory.getObjectModel().getUriPattern();
      synchronized (rootResources)
      {
         for (ObjectFactory<AbstractResourceDescriptor> resource : rootResources)
         {
            if (resource.getObjectModel().getUriPattern().equals(resourceFactory.getObjectModel().getUriPattern()))
            {
               if (resource.getObjectModel().getObjectClass() == resourceFactory.getObjectModel().getObjectClass())
               {
                  LOG.warn("Resource " + resourceFactory.getObjectModel().getObjectClass().getName()
                     + " already registered.");
               }
               else
               {
                  throw new ResourcePublicationException("Resource class "
                     + resourceFactory.getObjectModel().getObjectClass().getName()
                     + " can't be registered. Resource class " + resource.getObjectModel().getObjectClass().getName()
                     + " with the same pattern " + pattern + " already registered.");
               }
            }
         }
         rootResources.add(resourceFactory);
         Collections.sort(rootResources, RESOURCE_COMPARATOR);
         for (ResourceListener listener : resourceListeners)
         {
            listener.resourceAdded(resourceFactory.getObjectModel());
         }
         if (LOG.isDebugEnabled())
            LOG.debug("Add resource: " + resourceFactory.getObjectModel());
      }
   }

   /**
    * Register new resource listener.
    *
    * @param listener listener
    * @see ResourceListener
    */
   public void addResourceListener(ResourceListener listener)
   {
      resourceListeners.add(listener);
      if (LOG.isDebugEnabled())
         LOG.debug("Resource listener added: " + listener);
   }

   /**
    * Clear the list of ResourceContainer description.
    */
   public void clear()
   {
      synchronized (rootResources)
      {
         rootResources.clear();
      }
   }

   /**
    * Get root resource matched to <code>requestPath</code>.
    *
    * @param requestPath request path
    * @param parameterValues see {@link ApplicationContext#getParameterValues()}
    * @return root resource matched to <code>requestPath</code> or
    *         <code>null</code>
    */
   public ObjectFactory<AbstractResourceDescriptor> getMatchedResource(String requestPath, List<String> parameterValues)
   {
      ObjectFactory<AbstractResourceDescriptor> resourceFactory = null;
      synchronized (rootResources)
      {
         for (ObjectFactory<AbstractResourceDescriptor> resource : rootResources)
         {
            if (resource.getObjectModel().getUriPattern().match(requestPath, parameterValues))
            {
               // all times will at least 1
               int len = parameterValues.size();
               // If capturing group contains last element and this element is
               // neither null nor '/' then ResourceClass must contains at least one
               // sub-resource method or sub-resource locator.
               if (parameterValues.get(len - 1) != null && !parameterValues.get(len - 1).equals("/"))
               {
                  int subresnum =
                     resource.getObjectModel().getSubResourceMethods().size()
                        + resource.getObjectModel().getSubResourceLocators().size();
                  if (subresnum == 0)
                     continue;
               }
               resourceFactory = resource;
               break;
            }
         }
      }
      return resourceFactory;
   }

   /**
    * {@inheritDoc}
    */
   public List<ObjectFactory<AbstractResourceDescriptor>> getResources()
   {
      return rootResources;
   }

   /**
    * {@inheritDoc}
    */
   public int getSize()
   {
      return rootResources.size();
   }

   public ObjectFactory<AbstractResourceDescriptor> removeResource(Class<?> clazz)
   {
      ObjectFactory<AbstractResourceDescriptor> resource = null;
      synchronized (rootResources)
      {
         for (Iterator<ObjectFactory<AbstractResourceDescriptor>> iter = rootResources.iterator(); iter.hasNext()
            && resource == null;)
         {
            ObjectFactory<AbstractResourceDescriptor> next = iter.next();
            Class<?> resourceClass = next.getObjectModel().getObjectClass();
            if (clazz.equals(resourceClass))
            {
               iter.remove();
               resource = next;
               break;
            }
         }
         if (resource != null)
         {
            for (ResourceListener listener : resourceListeners)
            {
               listener.resourceRemoved(resource.getObjectModel());
            }
            if (LOG.isDebugEnabled())
               LOG.debug("Remove resource: " + resource.getObjectModel());
         }
      }
      return resource;
   }

   public ObjectFactory<AbstractResourceDescriptor> removeResource(String path)
   {
      ObjectFactory<AbstractResourceDescriptor> resource = null;
      UriPattern pattern = new UriPattern(path);
      synchronized (rootResources)
      {
         for (Iterator<ObjectFactory<AbstractResourceDescriptor>> iter = rootResources.iterator(); iter.hasNext()
            && resource == null;)
         {
            ObjectFactory<AbstractResourceDescriptor> next = iter.next();
            UriPattern resourcePattern = next.getObjectModel().getUriPattern();
            if (pattern.equals(resourcePattern))
            {
               iter.remove();
               resource = next;
               break;
            }
         }
         if (resource != null)
         {
            for (ResourceListener listener : resourceListeners)
            {
               listener.resourceRemoved(resource.getObjectModel());
            }
            if (LOG.isDebugEnabled())
               LOG.debug("Remove resource: " + resource.getObjectModel());
         }
      }
      return resource;
   }

}
