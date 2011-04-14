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

import org.everrest.core.ComponentLifecycleScope;
import org.everrest.core.impl.resource.AbstractResourceDescriptorImpl;

/**
 * Resource descriptor that can provide information about the Application, through which it was delivered.
 * Need to know that to be able use specified set of Providers. Provides delivered via JAX-RS Application
 * always has an advantage over embedded Providers ({@link javax.ws.rs.ext.MessageBodyReader},
 * {@link javax.ws.rs.ext.MessageBodyWriter}, {@link javax.ws.rs.ext.ExceptionMapper}, etc).
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class ApplicationResource extends AbstractResourceDescriptorImpl
{
   private final String applicationName;

   public ApplicationResource(String applicationName, Class<?> resourceClass, ComponentLifecycleScope scope)
   {
      super(resourceClass, scope);
      this.applicationName = applicationName;
   }

   /**
    * @return identifier of application-supplied subclass of {@link javax.ws.rs.core.Application}
    *         via this component was delivered
    */
   public String getApplicationName()
   {
      return applicationName;
   }
}
