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

import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
final class ApplicationConfiguration extends Application
{
   private final String applicationName;
   private final Application application;

   ApplicationConfiguration(String applicationName, Application application)
   {
      this.applicationName = applicationName;
      this.application = application;
   }

   /**
    * @see javax.ws.rs.core.Application#getClasses()
    */
   @Override
   public Set<Class<?>> getClasses()
   {
      return application.getClasses();
   }

   /**
    * @see javax.ws.rs.core.Application#getSingletons()
    */
   @Override
   public Set<Object> getSingletons()
   {
      return application.getSingletons();
   }

   /**
    * @return the applicationName unique identifier of JAX-RS Application. Identifier may be used to create Application
    *         specific invocation context for Resources delivered via this <code>application</code>, e.g. use Providers
    *         delivered with Application instead of embedded Providers (javax.ws.rs.ext.MessageBodyReader,
    *         javax.ws.rs.ext.MessageBodyWriter, etc) with for same purpose
    */
   public String getApplicationName()
   {
      return applicationName;
   }
}
