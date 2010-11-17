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
 * @version $Id: EverrestServletContextInitializer.java 76 2010-10-26 10:43:52Z
 *          andrew00x $
 */
public class EverrestServletContextInitializer
{
   public static final String EVERREST_SCAN_COMPONENTS = "org.everrest.scan.components";

   public static final String EVERREST_SCAN_SKIP_PACKAGES = "org.everrest.scan.skip.packages";

   public static final String JAXRS_APPLICATION = "javax.ws.rs.Application";

   private static final Logger LOG = Logger.getLogger(EverrestServletContextInitializer.class);

   protected final ServletContext sctx;

   public EverrestServletContextInitializer(ServletContext sctx)
   {
      this.sctx = sctx;
   }

   /**
    * Try get application's FQN from context-param javax.ws.rs.Application and
    * instantiate it. If such parameter is not specified then scan web
    * application's folders WEB-INF/classes and WEB-INF/lib for classes which
    * contains JAX-RS annotations. Interesting for three annotations
    * {@link Path}, {@link Provider} and {@link Filter} .
    *
    * @return instance of javax.ws.rs.core.Application
    * @throws IOException if any i/o errors occur
    */
   public Application getApplication()
   {
      Application application = null;
      String applicationFQN = getParameter(JAXRS_APPLICATION);
      boolean scan = false;
      String scanParameter = getParameter(EVERREST_SCAN_COMPONENTS);
      if (scanParameter != null)
      {
         scan = Boolean.parseBoolean(scanParameter);
      }
      if (applicationFQN != null)
      {
         if (scan)
         {
            String msg = "Scan of JAX-RS components is disabled cause to specified 'javax.ws.rs.Application'.";
            LOG.warn(msg);
         }
         {
            try
            {
               Class<?> cl = Thread.currentThread().getContextClassLoader().loadClass(applicationFQN.trim());
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
      }
      else if (scan)
      {
         try
         {
            URL classes = WarUrlFinder.findWebInfClassesPath(sctx);
            URL[] libs = WarUrlFinder.findWebInfLibClasspaths(sctx);
            AnnotationDB annotationDB = new AnnotationDB();
            List<String> skip = new ArrayList<String>();
            String sskip = sctx.getInitParameter(EVERREST_SCAN_SKIP_PACKAGES);
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
      String httpMethodOverrideParameter = getParameter(EverrestConfiguration.EVERREST_HTTP_METHOD_OVERRIDE);
      if (httpMethodOverrideParameter != null)
      {
         config.setHttpMethodOverride(Boolean.parseBoolean(httpMethodOverrideParameter));
      }
      String normalizeUriParameter = getParameter(EverrestConfiguration.EVERREST_NORMALIZE_URI);
      if (normalizeUriParameter != null)
      {
         config.setNormalizeUri(Boolean.parseBoolean(normalizeUriParameter));
      }
      String securityParameter = getParameter(EverrestConfiguration.EVERREST_CHECK_SECURITY);
      if (securityParameter != null)
      {
         config.setCheckSecurity(Boolean.parseBoolean(securityParameter));
      }
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
      return sctx.getInitParameter(name);
   }

}
