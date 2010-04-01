/*
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.everrest.env.exo.servlet;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.naming.InitialContextInitializer;

import java.net.MalformedURLException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by The eXo Platform SAS .<br/> Servlet context initializer that
 * initializes standalone container at the context startup time. To activate
 * this your web.xml have to be configured like:<br/>
 * &lt;listener&gt;<br/>
 * &lt;listener-class&gt;org.exoplatform.frameworks.web.common.StandaloneContainerInitializedListener&lt;/listener-class&gt;<br/>
 * &lt;/listener&gt;<br/>
 * You may also specify an URL to the
 * configuration.xml stored the configuration for StandaloneContainer as
 * servlet's init parameter called 'org.exoplatform.container.standalone.config'
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady
 *         Azarenkov</a>
 * @version $Id: StandaloneContainerInitializedListener.java 6739 2006-07-04
 *          14:34:49Z gavrikvetal $
 */

public class StandaloneContainerInitializedListener implements ServletContextListener
{

   /**
    * org.exoplatform.container.standalone.config
    */
   private static final String CONF_URL_PARAMETER = "org.exoplatform.container.standalone.config";

   /**
    * Container.
    */
   private StandaloneContainer container;

   /**
    * {@inheritDoc}
    */
   public void contextInitialized(ServletContextEvent event)
   {
      String configurationURL = event.getServletContext().getInitParameter(CONF_URL_PARAMETER);
      try
      {
         StandaloneContainer.addConfigurationURL(configurationURL);
      }
      catch (MalformedURLException e1)
      {
      }

      try
      {
         container = StandaloneContainer.getInstance(Thread.currentThread().getContextClassLoader());

         // Patch for tomcat InitialContext
         InitialContextInitializer ic =
            (InitialContextInitializer)container.getComponentInstanceOfType(InitialContextInitializer.class);

         if (ic != null)
            ic.recall();

         event.getServletContext().setAttribute("org.exoplatform.frameworks.web.eXoContainer", container);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

   }

   /**
    * {@inheritDoc}
    */
   public void contextDestroyed(ServletContextEvent event)
   {
      // container.stop();
   }
}
