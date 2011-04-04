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

package org.everrest.sample.exoplatform;

import org.everrest.exoplatform.servlet.EverrestExoContextListener;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.StandaloneContainer;

import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class BookServiceBootstrap extends EverrestExoContextListener
{
   private StandaloneContainer container;

   /**
    * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
    */
   @Override
   public void contextDestroyed(ServletContextEvent sce)
   {
      if (container != null)
         container.stop();
   }

   /**
    * @see org.everrest.exoplatform.servlet.EverrestExoContextListener#getContainer(javax.servlet.ServletContext)
    */
   @Override
   protected ExoContainer getContainer(ServletContext servletContext)
   {
      try
      {
         URL config = servletContext.getResource("/WEB-INF/classes/conf/exo-configuration.xml");
         StandaloneContainer.setConfigurationURL(config.toString());
         container = StandaloneContainer.getInstance();
         return container;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error of StandaloneContainer initialization. ", e);
      }
   }
}
