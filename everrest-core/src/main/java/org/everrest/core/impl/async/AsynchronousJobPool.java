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

import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.util.Logger;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PreDestroy;
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

   private static final Logger LOG = Logger.getLogger(AsynchronousJobPool.class);

   private static final AtomicLong jobNumber = new AtomicLong(1);

   /** Number of threads to serve asynchronous jobs. */
   private final int poolSize;

   /** Maximum number of task in queue. */
   private final int queueSize;

   /** When timeout (in minutes) reached then an asynchronous operation may be removed from the pool. */
   private final int jobTimeout;

   /** Max cache size. */
   private final int maxCacheSize;

   private final ExecutorService pool;

   private final Map<String, AsynchronousJob> jobs;

   @SuppressWarnings("serial")
   public AsynchronousJobPool(EverrestConfiguration config)
   {
      if (config == null)
      {
         config = new EverrestConfiguration();
      }

      this.poolSize = config.getAsynchronousPoolSize();
      this.queueSize = config.getAsynchronousQueueSize();
      this.maxCacheSize = config.getAsynchronousCacheSize();
      this.jobTimeout = config.getAsynchronousJobTimeout();

      this.pool =
         new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(
            queueSize), new ManyJobsPolicy(new ThreadPoolExecutor.AbortPolicy()));

      // TODO Use something more flexible (cache strategy) for setup cache behavior. 
      this.jobs = Collections.synchronizedMap(new LinkedHashMap<String, AsynchronousJob>()
      {
         @Override
         protected boolean removeEldestEntry(Entry<String, AsynchronousJob> eldest)
         {
            AsynchronousJob job = eldest.getValue();
            if (size() > maxCacheSize || job.getExpirationDate() < System.currentTimeMillis())
            {
               job.cancel(true);
               return true;
            }
            return false;
         }
      });
   }

   /** @see javax.ws.rs.ext.ContextResolver#getContext(java.lang.Class) */
   @Override
   public AsynchronousJobPool getContext(Class<?> type)
   {
      return this;
   }

   /**
    * @param resource object that contains resource method
    * @param resourceMethod resource or sub-resource method to invoke
    * @param params method parameters
    * @return id assigned to add asynchronous job
    * @throws AsynchronousJobRejectedException if this task cannot be added to pool
    */
   public String addJob(final Object resource, final ResourceMethodDescriptor resourceMethod, final Object[] params)
      throws AsynchronousJobRejectedException
   {
      AsynchronousJob job = new AsynchronousJob(
         newCallable(resource, resourceMethod.getMethod(), params),
         Long.toString(jobNumber.getAndIncrement()),
         jobTimeout,
         TimeUnit.MINUTES,
         resourceMethod
      );

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

      return jobId;
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

   public boolean removeJob(String jobId, boolean stopJob)
   {
      AsynchronousJob job = jobs.remove(jobId);
      if (job != null)
      {
         if (stopJob)
         {
            job.cancel(true);
         }
         return true;
      }
      return false;
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