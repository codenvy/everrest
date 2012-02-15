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

import org.everrest.core.impl.InternalException;
import org.everrest.core.resource.ResourceMethodDescriptor;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ws.rs.WebApplicationException;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class AsynchronousJob
{
   private final String jobId;
   private final long expirationDate;
   private final ResourceMethodDescriptor method;
   private final Future<Object> future;
   private final Map<String, Object> context;

   protected AsynchronousJob(String jobId,
                             Future<Object> future,
                             long expirationDate,
                             ResourceMethodDescriptor method)
   {
      this.future = future;
      this.jobId = jobId;
      this.expirationDate = expirationDate;
      this.method = method;
      context = new HashMap<String, Object>();
   }

   public String getJobId()
   {
      return jobId;
   }

   public long getExpirationDate()
   {
      return expirationDate;
   }

   public ResourceMethodDescriptor getResourceMethod()
   {
      return method;
   }

   public boolean isDone()
   {
      return future.isDone();
   }

   public boolean cancel()
   {
      return future.cancel(true);
   }

   /**
    * Get result of job. If job is not done yet this method throws IllegalStateException.
    * Before call this method caller must check is job done or not with method {@link #isDone()} .
    *
    * @return result
    * @throws IllegalStateException if job is not done yet
    */
   public Object getResult() throws IllegalStateException
   {
      if (!isDone())
      {
         throw new IllegalStateException("Job is not done yet. ");
      }

      Object result;
      try
      {
         result = future.get();
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

   /**
    * The storage for context attributes.
    *
    * @return map never <code>null</code>
    */
   public Map<String, Object> getContext()
   {
      return context;
   }
}