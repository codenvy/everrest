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

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyResourceLoader;

import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationPublisher;
import org.everrest.core.impl.ProviderBinder;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: GroovyApplicationPublisher.java 76 2010-10-26 10:43:52Z
 *          andrew00x $
 */
public class GroovyApplicationPublisher extends ApplicationPublisher
{

   private final GroovyClassLoader groovyClassLoader;

   public GroovyApplicationPublisher(ResourceBinder resources, ProviderBinder providers,
      GroovyClassLoader groovyClassLoader)
   {
      super(resources, providers);
      this.groovyClassLoader = groovyClassLoader;
   }

   @Override
   public void publish(Application application)
   {
      // Process Java resources.
      super.publish(application);
      // Process Groovy resources.
      if (application instanceof GroovyApplication)
      {
         Set<String> scripts = ((GroovyApplication)application).getScripts();
         if (scripts != null)
         {
            GroovyResourceLoader resourceLoader = groovyClassLoader.getResourceLoader();
            for (String name : scripts)
            {
               try
               {
                  URL url = resourceLoader.loadGroovySource(name);
                  Class<?> clazz = groovyClassLoader.parseClass(createCodeSource(url, name));
                  //System.out.println("\t\t"+clazz.getName());
                  resolver.addPerRequest(clazz);
               }
               catch (IOException e)
               {
                  throw new RuntimeException(e.getMessage(), e);
               }
            }
         }
      }
   }

   protected GroovyCodeSource createCodeSource(URL url, String name) throws IOException
   {
      GroovyCodeSource gcs =
         new GroovyCodeSource(url.openStream(), name == null ? url.toString() : name, "/groovy/script/jaxrs");
      gcs.setCachable(false);
      return gcs;
   }

}
