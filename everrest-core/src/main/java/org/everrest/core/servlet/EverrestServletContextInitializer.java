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

package org.everrest.core.servlet;

import org.everrest.core.Filter;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.util.Logger;
import org.scannotation.AnnotationDB;
import org.scannotation.WarUrlFinder;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class EverrestServletContextInitializer
{
   public static final String EVERREST_SCAN_COMPONENTS = "org.everrest.scan.components";

   public static final String EVERREST_SCAN_SKIP_PACKAGES = "org.everrest.scan.skip.packages";

   public static final String JAXRS_APPLICATION = "javax.ws.rs.Application";

   private static final Logger LOG = Logger.getLogger(EverrestServletContextInitializer.class);

   protected final ServletContext ctx;

   public EverrestServletContextInitializer(ServletContext ctx)
   {
      this.ctx = ctx;
   }

   /**
    * Try get application's FQN from context-param javax.ws.rs.Application and instantiate it. If such parameter is not
    * specified then scan web application's folders WEB-INF/classes and WEB-INF/lib for classes which contains JAX-RS
    * annotations. Interesting for three annotations {@link Path}, {@link Provider} and {@link Filter} .
    * 
    * @return instance of javax.ws.rs.core.Application
    */
   public Application getApplication()
   {
      Application application = null;
      String applicationFQN = getParameter(JAXRS_APPLICATION);
      boolean scan = getBoolean(EVERREST_SCAN_COMPONENTS, false);
      if (applicationFQN != null)
      {
         if (scan)
         {
            String msg = "Scan of JAX-RS components is disabled cause to specified 'javax.ws.rs.Application'.";
            LOG.warn(msg);
         }
         try
         {
            Class<?> cl = Thread.currentThread().getContextClassLoader().loadClass(applicationFQN);
            application = (Application)cl.newInstance();
         }
         catch (ClassNotFoundException e)
         {
            throw new RuntimeException(e);
         }
         catch (InstantiationException e)
         {
            throw new RuntimeException(e);
         }
         catch (IllegalAccessException e)
         {
            throw new RuntimeException(e);
         }
      }
      else if (scan)
      {
         try
         {
            URL classes = WarUrlFinder.findWebInfClassesPath(ctx);
            URL[] libs = WarUrlFinder.findWebInfLibClasspaths(ctx);
            AnnotationDB annotationDB = new AnnotationDB();
            List<String> skip = new ArrayList<String>();
            String sskip = ctx.getInitParameter(EVERREST_SCAN_SKIP_PACKAGES);
            if (sskip != null)
            {
               for (String s : sskip.split(","))
               {
                  skip.add(s.trim());
               }
            }
            // Disable processing of API, implementation and JAX-RS packages
            skip.add("org.everrest.core");
            skip.add("javax.ws.rs");
            annotationDB.setIgnoredPackages(skip.toArray(new String[skip.size()]));
            annotationDB.setScanFieldAnnotations(false);
            annotationDB.setScanMethodAnnotations(false);
            annotationDB.setScanParameterAnnotations(false);
            if (classes != null)
            {
               annotationDB.scanArchives(classes);
            }
            annotationDB.scanArchives(libs);
            final Set<Class<?>> scanned = new HashSet<Class<?>>();
            Map<String, Set<String>> results = annotationDB.getAnnotationIndex();
            for (String annotation : new String[]{Path.class.getName(), Provider.class.getName(),
               Filter.class.getName()})
            {
               if (results.get(annotation) != null)
               {
                  for (String fqn : results.get(annotation))
                  {
                     try
                     {
                        Class<?> cl = Thread.currentThread().getContextClassLoader().loadClass(fqn);
                        if (cl.isInterface() || Modifier.isAbstract(cl.getModifiers()))
                        {
                           LOG.info("Skip abstract class or interface " + fqn);
                           continue;
                        }
                        scanned.add(cl);
                     }
                     catch (ClassNotFoundException e)
                     {
                        throw new RuntimeException(e);
                     }
                  }
               }
            }
            application = new Application()
            {
               public Set<Class<?>> getClasses()
               {
                  return scanned;
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

   public EverrestConfiguration getConfiguration()
   {
      EverrestConfiguration config = new EverrestConfiguration();

      config.setHttpMethodOverride(getBoolean(EverrestConfiguration.EVERREST_HTTP_METHOD_OVERRIDE,
         EverrestConfiguration.defaultHttpMethodOverride));

      config.setNormalizeUri(getBoolean(EverrestConfiguration.EVERREST_NORMALIZE_URI,
         EverrestConfiguration.defaultNormalizeUri));

      config.setCheckSecurity(getBoolean(EverrestConfiguration.EVERREST_CHECK_SECURITY,
         EverrestConfiguration.defaultCheckSecurity));

      config.setAsynchronousSupported(getBoolean(EverrestConfiguration.EVERREST_ASYNCHRONOUS,
         EverrestConfiguration.defaultAsynchronousSupported));

      config.setAsynchronousPoolSize(getNumber(EverrestConfiguration.EVERREST_ASYNCHRONOUS_POOL_SIZE,
         EverrestConfiguration.defaultAsynchronousPoolSize).intValue());

      config.setAsynchronousQueueSize(getNumber(EverrestConfiguration.EVERREST_ASYNCHRONOUS_QUEUE_SIZE,
         EverrestConfiguration.defaultAsynchronousQueueSize).intValue());

      config.setAsynchronousCacheSize(getNumber(EverrestConfiguration.EVERREST_ASYNCHRONOUS_CACHE_SIZE,
         EverrestConfiguration.defaultAsynchronousCacheSize).intValue());

      config.setAsynchronousJobTimeout(getNumber(EverrestConfiguration.EVERREST_ASYNCHRONOUS_JOB_TIMEOUT,
         EverrestConfiguration.defaultAsynchronousJobTimeout).intValue());

      config.setMaxBufferSize(getNumber(EverrestConfiguration.EVERREST_MAX_BUFFER_SIZE,
         EverrestConfiguration.defaultMaxBufferSize).intValue());

      return config;
   }

   /**
    * Get parameter with specified name from servlet context initial parameters.
    * 
    * @param name parameter name
    * @return value of parameter with specified name
    */
   public String getParameter(String name)
   {
      String str = ctx.getInitParameter(name);
      if (str != null)
      {
         return str.trim();
      }
      return null;
   }

   public boolean getBoolean(String name, boolean def)
   {
      String str = getParameter(name);
      if (str != null)
      {
         return "true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str) || "on".equalsIgnoreCase(str)
            || "1".equals(str);
      }
      return def;
   }

   public Double getNumber(String name, double def)
   {
      String str = getParameter(name);
      if (str != null)
      {
         try
         {
            return Double.parseDouble(str);
         }
         catch (NumberFormatException ignored)
         {
         }
      }
      return def;
   }
}
