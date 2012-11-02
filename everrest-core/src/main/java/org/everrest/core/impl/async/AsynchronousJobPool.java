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
package org.everrest.core.impl.async;

import org.everrest.core.ApplicationContext;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.ContainerRequest;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.InternalException;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.tools.DummySecurityContext;
import org.everrest.core.tools.EmptyInputStream;
import org.everrest.core.tools.SecurityContextRequest;
import org.everrest.core.tools.SimplePrincipal;
import org.everrest.core.util.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PreDestroy;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Pool of asynchronous jobs.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@Provider
public class AsynchronousJobPool implements ContextResolver<AsynchronousJobPool>
{
   public static class ManyJobsPolicy implements RejectedExecutionHandler
   {
      private final RejectedExecutionHandler delegate;

      public ManyJobsPolicy(RejectedExecutionHandler delegate)
      {
         this.delegate = delegate;
      }

      @Override
      public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
      {
         if (executor.getPoolSize() >= executor.getCorePoolSize())
         {
            throw new RejectedExecutionException(
               "Can't accept new asynchronous request. Too many asynchronous jobs in progress. ");
         }
         delegate.rejectedExecution(r, executor);
      }
   }

   /** Logger. */
   private static final Logger LOG = Logger.getLogger(AsynchronousJobPool.class);

   /** Generator for unique job ID . */
   private static final AtomicLong jobIdGenerator = new AtomicLong(1);

   private static String nextJobId()
   {
      return Long.toString(jobIdGenerator.getAndIncrement());
   }

   /** When timeout (in minutes) reached then an asynchronous job may be removed from the pool. */
   private final int jobTimeout;

   /** Max cache size. */
   private final int maxCacheSize;

   private final ExecutorService pool;

   /** Asynchronous jobs cache. */
   private final Map<String, AsynchronousJob> jobs;

   private final CopyOnWriteArrayList<AsynchronousJobListener> jobListeners;

   @SuppressWarnings("serial")
   public AsynchronousJobPool(EverrestConfiguration config)
   {
      if (config == null)
      {
         config = new EverrestConfiguration();
      }

      this.maxCacheSize = config.getAsynchronousCacheSize();
      this.jobTimeout = config.getAsynchronousJobTimeout();

      /* Number of threads to serve asynchronous jobs. */
      int poolSize = config.getAsynchronousPoolSize();
      /* Maximum number of task in queue. */
      int queueSize = config.getAsynchronousQueueSize();

      this.pool = new ThreadPoolExecutor(
         poolSize,
         poolSize,
         0L,
         TimeUnit.MILLISECONDS,
         new LinkedBlockingQueue<Runnable>(queueSize),
         new ManyJobsPolicy(new ThreadPoolExecutor.AbortPolicy())
      );

      // TODO Use something more flexible (cache strategy) for setup cache behavior. 
      this.jobs = Collections.synchronizedMap(new LinkedHashMap<String, AsynchronousJob>()
      {
         @Override
         protected boolean removeEldestEntry(Entry<String, AsynchronousJob> eldest)
         {
            AsynchronousJob job = eldest.getValue();
            if (size() > maxCacheSize || job.getExpirationDate() < System.currentTimeMillis())
            {
               job.cancel();
               return true;
            }
            return false;
         }
      });

      this.jobListeners = new CopyOnWriteArrayList<AsynchronousJobListener>();
   }

   /** @see javax.ws.rs.ext.ContextResolver#getContext(java.lang.Class) */
   @Override
   public AsynchronousJobPool getContext(Class<?> type)
   {
      return this;
   }

   /**
    * @param resource
    *    object that contains resource method
    * @param resourceMethod
    *    resource or sub-resource method to invoke
    * @param params
    *    method parameters
    * @return asynchronous job
    * @throws AsynchronousJobRejectedException
    *    if this task cannot be added to pool
    */
   public AsynchronousJob addJob(Object resource,
                                 ResourceMethodDescriptor resourceMethod,
                                 Object[] params) throws AsynchronousJobRejectedException
   {
      AsynchronousFuture job = new AsynchronousFuture(
         nextJobId(),
         newCallable(resource, resourceMethod.getMethod(), params),
         System.currentTimeMillis() + jobTimeout * 60 * 1000,
         resourceMethod
      );

      ApplicationContext context = ApplicationContextImpl.getCurrent();
      GenericContainerRequest request = context.getContainerRequest();
      Principal principal = request.getUserPrincipal();

      // Create copy of request. Need to keep 'Accept' headers to be able determine MessageBodyWriter which can be
      // used to serialize result of method invocation. Do not copy entity stream. This stream is empty any way.
      Principal copyPrincipal = null;
      if (principal != null)
      {
         copyPrincipal = new SimplePrincipal(principal.getName());
      }

      ContainerRequest copyRequest = new SecurityContextRequest(
         request.getMethod(),
         request.getRequestUri(),
         request.getBaseUri(),
         new EmptyInputStream(),
         request.getRequestHeaders(),
         new DummySecurityContext(copyPrincipal));
      job.getContext().put("org.everrest.async.request", copyRequest);
      // Save current set of providers. In some environments they can be resource specific.
      job.getContext().put("org.everrest.async.providers", context.getProviders());

      try
      {
         pool.execute(job);
      }
      catch (RejectedExecutionException e)
      {
         throw new AsynchronousJobRejectedException(e.getMessage());
      }

      String jobId = job.getJobId();
      jobs.put(jobId, job);

      if (LOG.isDebugEnabled())
      {
         LOG.debug("Add asynchronous job, ID " + jobId);
      }

      return job;
   }

   private class AsynchronousFuture extends FutureTask<Object> implements AsynchronousJob
   {
      private final String jobId;
      private final long expirationDate;
      private final ResourceMethodDescriptor method;
      private final Map<String, Object> context;

      protected AsynchronousFuture(String jobId,
                                   Callable<Object> callable,
                                   long expirationDate,
                                   ResourceMethodDescriptor method)
      {
         super(callable);
         this.jobId = jobId;
         this.expirationDate = expirationDate;
         this.method = method;
         context = new HashMap<String, Object>();
      }

      @Override
      protected void done()
      {
         for (AsynchronousJobListener l : jobListeners)
         {
            l.done(this);
         }
      }

      @Override
      public String getJobId()
      {
         return jobId;
      }

      @Override
      public long getExpirationDate()
      {
         return expirationDate;
      }

      @Override
      public ResourceMethodDescriptor getResourceMethod()
      {
         return method;
      }

      @Override
      public boolean isDone()
      {
         return super.isDone();
      }

      @Override
      public boolean cancel()
      {
         return super.cancel(true);
      }

      @Override
      public Object getResult() throws IllegalStateException
      {
         if (!isDone())
         {
            throw new IllegalStateException("Job is not done yet. ");
         }

         Object result;
         try
         {
            result = super.get();
         }
         catch (InterruptedException e)
         {
            // We already check the Future is done.
            throw new InternalException(e);
         }
         catch (ExecutionException e)
         {
            Throwable cause = e.getCause();
            if (cause instanceof InvocationTargetException)
            {
               cause = ((InvocationTargetException)cause).getTargetException();
               if (cause instanceof WebApplicationException)
               {
                  throw (WebApplicationException)cause;
               }
            }
            throw new InternalException(cause);
         }

         return result;
      }

      @Override
      public Map<String, Object> getContext()
      {
         return context;
      }
   }

   protected Callable<Object> newCallable(final Object resource, final Method method, final Object[] params)
   {
      return new Callable<Object>()
      {
         @Override
         public Object call() throws Exception
         {
            return method.invoke(resource, params);
         }
      };
   }

   public AsynchronousJob getJob(String jobId)
   {
      return jobs.get(jobId);
   }

   public AsynchronousJob removeJob(String jobId)
   {
      AsynchronousJob job = jobs.remove(jobId);
      if (!(job == null || job.isDone()))
      {
         job.cancel();
      }
      return job;
   }

   public List<AsynchronousJob> getAll()
   {
      Collection<AsynchronousJob> all = jobs.values();
      List<AsynchronousJob> copy = new ArrayList<AsynchronousJob>(all.size());
      copy.addAll(all);
      return copy;
   }

   /**
    * Register new listener if it is not registered yet.
    *
    * @param listener
    *    listener
    * @return <code>true</code> if new listener registered and <code>false</code> otherwise.
    * @see AsynchronousJobListener
    */
   public boolean registerListener(AsynchronousJobListener listener)
   {
      return jobListeners.addIfAbsent(listener);
   }

   /**
    * Unregister listener.
    *
    * @param listener
    *    listener to unregister
    * @return <code>true</code> if listener unregistered and <code>false</code> otherwise.
    * @see AsynchronousJobListener
    */
   public boolean unregisterListener(AsynchronousJobListener listener)
   {
      return jobListeners.remove(listener);
   }

   @PreDestroy
   public void stop()
   {
      pool.shutdown();
      try
      {
         if (!pool.awaitTermination(5, TimeUnit.SECONDS))
         {
            pool.shutdownNow();
         }
      }
      catch (InterruptedException e)
      {
         pool.shutdownNow();
         Thread.currentThread().interrupt();
      }
   }
}