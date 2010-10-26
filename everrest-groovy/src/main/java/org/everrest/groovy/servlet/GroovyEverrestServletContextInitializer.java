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
import org.everrest.groovy.GroovyApplication;

import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class GroovyEverrestServletContextInitializer extends EverrestServletContextInitializer
{

   private final GroovyClassLoader groovyClassLoader;

   public GroovyEverrestServletContextInitializer(ServletContext sctx, GroovyClassLoader groovyClassLoader)
   {
      super(sctx);
      this.groovyClassLoader = groovyClassLoader;
   }

   @Override
   public Application getApplication()
   {
      String applicationLocation = getParameter(JAXRS_APPLICATION);
      InputStream applicationStream = sctx.getResourceAsStream(applicationLocation);
      Class<?> applicationClass = groovyClassLoader.parseClass(applicationStream);
      GroovyApplication application = null;
      try
      {
         application = (GroovyApplication)applicationClass.newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      return application;
   }

}
