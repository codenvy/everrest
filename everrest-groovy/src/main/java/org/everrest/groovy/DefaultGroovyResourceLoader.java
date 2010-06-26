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

package org.everrest.groovy;

import groovy.lang.GroovyResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: DefaultGroovyResourceLoader.java 2680 2010-06-22 11:43:00Z
 *          aparfonov $
 */
public class DefaultGroovyResourceLoader implements GroovyResourceLoader
{

   protected URL[] roots;

   protected Map<String, URL> resources = Collections.synchronizedMap(new HashMap<String, URL>());

   public DefaultGroovyResourceLoader(URL[] roots) throws MalformedURLException
   {
      this.roots = new URL[roots.length];
      for (int i = 0; i < roots.length; i++)
      {
         String str = roots[i].toString();
         if (str.charAt(str.length() - 1) != '/')
         {
            this.roots[i] = new URL(str + '/');
         }
         else
         {
            this.roots[i] = roots[i];
         }
      }
   }

   public DefaultGroovyResourceLoader(URL root) throws MalformedURLException
   {
      this(new URL[]{root});
   }

   /**
    * {@inheritDoc}
    */
   public final URL loadGroovySource(String classname) throws MalformedURLException
   {
      final String filename = classname.replace('.', '/') + ".groovy";
      try
      {
         return AccessController.doPrivileged(new PrivilegedExceptionAction<URL>()
         {
            public URL run() throws Exception
            {
               return getResource(filename);
            }
         });
      }
      catch (PrivilegedActionException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof Error)
            throw (Error)cause;
         if (cause instanceof RuntimeException)
            throw (RuntimeException)cause;
         throw (MalformedURLException)cause;
      }
   }

   protected URL getResource(String filename) throws MalformedURLException
   {
      filename = filename.intern();
      URL resource = null;
      synchronized (filename)
      {
         resource = resources.get(filename);
         boolean inCache = resource != null;
         for (URL root : roots)
         {
            if (resource == null)
            {
               resource = new URL(root, filename);
            }
            try
            {
               InputStream script = resource.openStream();
               script.close();
               break;
            }
            catch (IOException e)
            {
               resource = null;
            }
         }
         if (resource != null)
         {
            resources.put(filename, resource);
         }
         else if (inCache)
         {
            // Remove from map if resource is unreachable
            resources.remove(filename);
         }
      }

      return resource;
   }

}
