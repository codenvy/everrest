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

import org.codehaus.groovy.control.CompilationFailedException;
import org.everrest.core.Filter;
import org.everrest.core.servlet.EverrestServletContextInitializer;
import org.everrest.core.util.Logger;
import org.everrest.groovy.DefaultGroovyResourceLoader;
import org.everrest.groovy.GroovyClassLoaderProvider;
import org.everrest.groovy.ScriptFinder;
import org.everrest.groovy.ScriptFinderFactory;
import org.everrest.groovy.SourceFile;
import org.everrest.groovy.URLFilter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: GroovyEverrestServletContextInitializer.java 77 2010-10-26
 *          15:20:15Z andrew00x $
 */
public class GroovyEverrestServletContextInitializer extends EverrestServletContextInitializer
{

   public static final String EVERREST_GROOVY_ROOT_RESOURCES = "org.everrest.groovy.root.resources";

   public static final String EVERREST_GROOVY_APPLICATION = "org.everrest.groovy.Application";

   public static final String EVERREST_GROOVY_SCAN_COMPONENTS = "org.everrest.groovy.scan.components";

   private static final Logger LOG = Logger.getLogger(GroovyEverrestServletContextInitializer.class);

   protected GroovyClassLoaderProvider classLoaderProvider;

   protected final URL[] groovyClassPath;

   public GroovyEverrestServletContextInitializer(ServletContext sctx)
   {
      super(sctx);
      String inputRootResources = getParameter(GroovyEverrestServletContextInitializer.EVERREST_GROOVY_ROOT_RESOURCES);
      Set<URL> rootResources = new LinkedHashSet<URL>();
      if (inputRootResources != null)
      {
         try
         {
            for (String s : inputRootResources.split(","))
               rootResources.add(new URL(s.trim()));
         }
         catch (MalformedURLException e)
         {
            throw new RuntimeException(e);
         }
      }
      this.groovyClassPath = rootResources.toArray(new URL[rootResources.size()]);
      this.classLoaderProvider = new GroovyClassLoaderProvider();
      try
      {
         classLoaderProvider.getGroovyClassLoader().setResourceLoader(new DefaultGroovyResourceLoader(groovyClassPath));
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   @Override
   public Application getApplication()
   {
      String groovyApplicationFQN = getParameter(EVERREST_GROOVY_APPLICATION);
      Application groovyApplication = null;
      boolean scan = false;
      String scanParameter = getParameter(EVERREST_GROOVY_SCAN_COMPONENTS);
      if (scanParameter != null)
      {
         scan = Boolean.parseBoolean(scanParameter);
      }
      if (groovyApplicationFQN != null)
      {
         if (scan)
         {
            String msg =
               "Scan of Groovy JAX-RS components is disabled cause to specified 'org.everrest.groovy.Application'.";
            LOG.warn(msg);
         }

         Class<?> applicationClass;
         try
         {
            applicationClass = classLoaderProvider.getGroovyClassLoader().loadClass(groovyApplicationFQN, true, false);
            groovyApplication = (Application)applicationClass.newInstance();
         }
         catch (CompilationFailedException e)
         {
            throw new RuntimeException(e);
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
            URLFilter filter = new URLFilter()
            {
               public boolean accept(URL url)
               {
                  return url.getPath().endsWith(".groovy");
               }
            };

            final Set<Class<?>> scanned = new HashSet<Class<?>>();
            Class[] jaxrsAnnotations = new Class[]{Path.class, Provider.class, Filter.class};
            for (URL path : groovyClassPath)
            {
               String protocol = path.getProtocol();
               ScriptFinder finder = ScriptFinderFactory.getScriptFinder(protocol);
               if (finder != null)
               {
                  Set<URL> scripts = finder.find(filter, path);
                  if (scripts != null && scripts.size() > 0)
                  {
                     /*for (URL script : scripts)
                     {
                        InputStream in = null;
                        try
                        {
                           in = script.openStream();
                           GroovyCodeSource gcs = new GroovyCodeSource(in, script.toString(), "/groovy/script/jaxrs");
                           gcs.setCachable(false);
                           Class<?> clazz = classLoaderProvider.getGroovyClassLoader().parseClass(gcs);
                           if (findAnnotation(clazz, jaxrsAnnotations))
                           {
                              boolean added = scanned.add(clazz);
                              if (added)
                              {
                                 if (LOG.isDebugEnabled())
                                    LOG.debug("Add script from URL: " + script);
                              }
                              else
                              {
                                 LOG.warn("Skip duplicated class: " + clazz);
                              }
                           }
                        }
                        finally
                        {
                           if (in != null)
                           {
                              try
                              {
                                 in.close();
                              }
                              catch (IOException e)
                              {
                                 LOG.error("Error occurs when tried to close stream, " + e.getMessage());
                              }
                           }
                        }
                     }*/

                     SourceFile[] files = new SourceFile[scripts.size()];
                     int i = 0;
                     for (URL script : scripts)
                        files[i++] = new SourceFile(script);
                     Class[] classes = classLoaderProvider.getGroovyClassLoader().parseClasses(files);
                     for (int j = 0; j < classes.length; j++)
                     {
                        Class clazz = classes[j];
                        if (findAnnotation(clazz, jaxrsAnnotations))
                        {
                           boolean added = scanned.add(clazz);
                           if (added)
                           {
                              if (LOG.isDebugEnabled())
                                 LOG.debug("Add class : " + clazz);
                           }
                           else
                           {
                              LOG.warn("Skip duplicated class: " + clazz);
                           }
                        }
                     }
                  }
               }
               else
               {
                  String msg =
                     "Skip URL : " + path + ". Protocol '" + protocol
                        + "' is not supported for scan JAX-RS components. ";
                  LOG.warn(msg);
               }
            }
            groovyApplication = new Application()
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
      return groovyApplication;
   }

   private boolean findAnnotation(Class<?> clazz, Class<? extends Annotation>... annClasses)
   {
      for (Class<? extends Annotation> ac : annClasses)
      {
         if (clazz.getAnnotation(ac) != null)
         {
            return true;
         }
      }
      return false;
   }

}
