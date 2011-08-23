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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
   private static class ManyJobsPolicy implements RejectedExecutionHandler
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
            throw new RejectedExecutionException(
               "Can't accept new asynchronous request. Too many asynchronous jobs in progress. ");
         delegate.rejectedExecution(r, executor);
      }
   }

   private static final Logger log = Logger.getLogger(AsynchronousJobPool.class);

   private static int counter;

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
         config = new EverrestConfiguration();

      this.poolSize = config.getAsynchronousPoolSize();
      this.queueSize = config.getAsynchronousQueueSize();
      this.maxCacheSize = config.getAsynchronousCacheSize();
      this.jobTimeout = config.getAsynchronousJobTimeout();

      RejectedExecutionHandler delegateHandler;

      try
      {
         Field defaultHandlerField = ThreadPoolExecutor.class.getDeclaredField("defaultHandler");
         defaultHandlerField.setAccessible(true);
         delegateHandler = (RejectedExecutionHandler)defaultHandlerField.get(null);
      }
      catch (Exception ignored)
      {
         delegateHandler = new ThreadPoolExecutor.AbortPolicy();
      }

      this.pool =
         new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(
            queueSize), new ManyJobsPolicy(delegateHandler));

      // TODO Use something more flexible (cache strategy) for setup cache behavior. 
      this.jobs = Collections.synchronizedMap(new LinkedHashMap<String, AsynchronousJob>()
      {
         @Override
         protected boolean removeEldestEntry(Entry<String, AsynchronousJob> eldest)
         {
            if (size() > maxCacheSize)
               return true;
            AsynchronousJob job = eldest.getValue();
            if (job.getExpirationDate() < System.currentTimeMillis())
            {
               job.cancel(true);
               return true;
            }
            return false;
         }
      });
   }

   /**
    * @see javax.ws.rs.ext.ContextResolver#getContext(java.lang.Class)
    */
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
      final String jobId = nextId();
      final Method method = resourceMethod.getMethod();
      AsynchronousJob job = new AsynchronousJob(new Callable<Object>()
      {
         @Override
         public Object call()
         {
            try
            {
               return method.invoke(resource, params);
            }
            catch (InvocationTargetException e)
            {
               Throwable cause = e.getCause();
               if (WebApplicationException.class == cause.getClass())
                  return ((WebApplicationException)cause).getResponse();
               return fromThrowable(cause);
            }
            catch (IllegalArgumentException e)
            {
               return fromThrowable(e);
            }
            catch (IllegalAccessException e)
            {
               return fromThrowable(e);
            }
            catch (Throwable e)
            {
               return fromThrowable(e);
            }
         }
      }, jobId, jobTimeout, TimeUnit.MINUTES, resourceMethod);

      try
      {
         pool.execute(job);
      }
      catch (RejectedExecutionException e)
      {
         throw new AsynchronousJobRejectedException(e.getMessage());
      }

      jobs.put(jobId, job);

      if (log.isDebugEnabled())
         log.debug("Add asynchronous job, ID " + jobId);

      return jobId;
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
            job.cancel(true);
         return true;
      }
      return false;
   }

   public void stop()
   {
      pool.shutdown();
      try
      {
         if (!pool.awaitTermination(5, TimeUnit.SECONDS))
            pool.shutdownNow();
      }
      catch (InterruptedException e)
      {
         pool.shutdownNow();
         Thread.currentThread().interrupt();
      }
   }

   private Response fromThrowable(Throwable t)
   {
      if (log.isDebugEnabled())
         log.error(t.getMessage(), t);
      String msg = t.getMessage();
      if (msg != null)
         return Response.serverError().entity(msg).type(MediaType.TEXT_PLAIN).build();
      return Response.serverError().build();
   }

   private synchronized String nextId()
   {
      return Integer.toString(++counter);
   }
}