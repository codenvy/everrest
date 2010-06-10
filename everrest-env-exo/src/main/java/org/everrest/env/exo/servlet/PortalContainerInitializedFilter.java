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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.web.AbstractFilter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Created by The eXo Platform SAS .<br/>
 * Servlet Filter that is used to initialize and remove the portal container
 * from the ThreadLocal of PortalContainer, it relies on
 * PortalContainer.getCurrentInstance to retrieve the right portal container.
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class PortalContainerInitializedFilter extends AbstractFilter
{

   private static final Log LOG = ExoLogger.getLogger("PortatContainerInitializedFilter");

   /**
    * initializes PortalContainer instance.
    * 
    * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
    *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
      ServletException
   {
      ExoContainer container = getContainer();
      if (!(container instanceof PortalContainer))
      {
         container = RootContainer.getInstance().getPortalContainer(PortalContainer.DEFAULT_PORTAL_CONTAINER_NAME);
         if (container == null)
         {
            throw new ServletException("Could not initialize PortalContainer." + "Current ExoContainer is: "
               + ExoContainerContext.getCurrentContainer());
         }
      }
      PortalContainer pcontainer = (PortalContainer)container;
      try
      {
         PortalContainer.setInstance(pcontainer);
         chain.doFilter(request, response);
      }
      finally
      {
         try
         {
            PortalContainer.setInstance(null);
         }
         catch (Exception e)
         {
            LOG.warn("An error occured while cleaning the ThreadLocal", e);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void destroy()
   {
   }

}
