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

package org.everrest.groovy.servlet;

import groovy.lang.GroovyClassLoader;

import org.everrest.core.servlet.EverrestServletContextInitializer;
import org.everrest.core.util.Logger;
import org.everrest.groovy.GroovyApplication;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: GroovyEverrestServletContextInitializer.java 77 2010-10-26
 *          15:20:15Z andrew00x $
 */
public class GroovyEverrestServletContextInitializer extends EverrestServletContextInitializer
{

   public static final String EVERREST_GROOVY_ROOT_RESOURCES = "org.everrest.groovy.root.resources";

   private static final Logger LOG = Logger.getLogger(GroovyEverrestServletContextInitializer.class);

   private final GroovyClassLoader groovyClassLoader;

   public GroovyEverrestServletContextInitializer(ServletContext sctx, GroovyClassLoader groovyClassLoader)
   {
      super(sctx);
      this.groovyClassLoader = groovyClassLoader;
   }

   @Override
   public Application getApplication()
   {
      String applicationFQN = getParameter(JAXRS_APPLICATION);
      Application application = null;
      boolean scan = true;
      String scanParameter = getParameter(EVERREST_SCAN_COMPONENTS);
      if (scanParameter != null)
      {
         scan = Boolean.parseBoolean(getParameter(EVERREST_SCAN_COMPONENTS));
      }
      if (applicationFQN != null)
      {
         if (scan)
         {
            String msg = "Scan of JAX-RS components is disabled cause to specified 'javax.ws.rs.Application'.";
            LOG.warn(msg);
         }
         try
         {
            Class<?> applicationClass = groovyClassLoader.loadClass(applicationFQN, true, false);
            application = (Application)applicationClass.newInstance();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e.getMessage(), e);
         }
      }
      else if (scan)
      {
         try
         {
            final Set<Class<?>> scanned = scanWebApplication();
            application = new GroovyApplication()
            {
               @Override
               public Set<Class<?>> getClasses()
               {
                  return scanned;
               }

               @Override
               public Set<String> getScripts()
               {
                  // TODO scan for Groovy components on local file system.
                  return Collections.emptySet();
               }
            };
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }

      }
      return application;
   }

}
