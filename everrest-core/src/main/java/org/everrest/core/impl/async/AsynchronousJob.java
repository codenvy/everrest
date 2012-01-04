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

import org.everrest.core.resource.ResourceMethodDescriptor;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class AsynchronousJob extends FutureTask<Object>
{
   private final String jobId;
   private final long expirationDate;
   private final ResourceMethodDescriptor method;

   protected AsynchronousJob(Callable<Object> callable,
                             String jobId,
                             long timeout,
                             TimeUnit unit,
                             ResourceMethodDescriptor method)
   {
      super(callable);
      this.jobId = jobId;
      this.expirationDate = System.currentTimeMillis() + unit.toMillis(timeout);
      this.method = method;
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
}