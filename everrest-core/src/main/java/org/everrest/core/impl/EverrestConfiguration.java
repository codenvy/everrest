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

package org.everrest.core.impl;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class EverrestConfiguration
{
   public static final String EVERREST_HTTP_METHOD_OVERRIDE = "org.everrest.http.method.override";

   public static final String EVERREST_NORMALIZE_URI = "org.everrest.normalize.uri";

   public static final String EVERREST_CHECK_SECURITY = "org.everrest.security";

   public static final String EVERREST_ASYNCHRONOUS = "org.everrest.asynchronous";

   public static final String EVERREST_ASYNCHRONOUS_POOL_SIZE = "org.everrest.asynchronous.pool.size";

   public static final String EVERREST_ASYNCHRONOUS_QUEUE_SIZE = "org.everrest.asynchronous.queue.size";

   public static final String EVERREST_ASYNCHRONOUS_JOB_TIMEOUT = "org.everrest.asynchronous.job.timeout";

   public static boolean defaultCheckSecurity = true;

   public static boolean defaultHttpMethodOverride = true;

   public static boolean defaultNormalizeUri = false;

   public static boolean defaultAsynchronousSupported = true;

   public static int defaultAsynchronousPoolSize = 100;

   public static int defaultAsynchronousQueueSize = 100;

   public static final int defaultAsynchronousJobTimeout = 60;

   //

   protected boolean checkSecurity = defaultCheckSecurity;

   protected boolean httpMethodOverride = defaultHttpMethodOverride;

   protected boolean normalizeUri = defaultNormalizeUri;

   protected boolean asynchronousSupported = defaultAsynchronousSupported;

   protected int asynchronousPoolSize = defaultAsynchronousPoolSize;

   protected int asynchronousQueueSize = defaultAsynchronousQueueSize;

   protected int asynchronousJobTimeout = defaultAsynchronousJobTimeout;

   public boolean isCheckSecurity()
   {
      return checkSecurity;
   }

   public void setCheckSecurity(boolean checkSecurity)
   {
      this.checkSecurity = checkSecurity;
   }

   public boolean isHttpMethodOverride()
   {
      return httpMethodOverride;
   }

   public void setHttpMethodOverride(boolean httpMethodOverride)
   {
      this.httpMethodOverride = httpMethodOverride;
   }

   public boolean isNormalizeUri()
   {
      return normalizeUri;
   }

   public void setNormalizeUri(boolean normalizeUri)
   {
      this.normalizeUri = normalizeUri;
   }

   public boolean isAsynchronousSupported()
   {
      return asynchronousSupported;
   }

   public void setAsynchronousSupported(boolean asynchronousSupported)
   {
      this.asynchronousSupported = asynchronousSupported;
   }

   public int getAsynchronousPoolSize()
   {
      return asynchronousPoolSize;
   }

   public void setAsynchronousPoolSize(int asynchronousPoolSize)
   {
      this.asynchronousPoolSize = asynchronousPoolSize;
   }

   public int getAsynchronousQueueSize()
   {
      return asynchronousQueueSize;
   }

   public void setAsynchronousQueueSize(int asynchronousQueueSize)
   {
      this.asynchronousQueueSize = asynchronousQueueSize;
   }

   public int getAsynchronousJobTimeout()
   {
      return asynchronousJobTimeout;
   }

   public void setAsynchronousJobTimeout(int asynchronousJobTimeout)
   {
      this.asynchronousJobTimeout = asynchronousJobTimeout;
   }
}
