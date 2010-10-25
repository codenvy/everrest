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
package org.everrest.core.servlet;

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.util.Logger;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class StandaloneRestServlet extends HttpServlet
{

   private static final long serialVersionUID = -8234561611241680339L;

   private static final Logger LOG = Logger.getLogger(StandaloneRestServlet.class.getName());

   protected EverrestProcessor processor;

   protected ServletConfig servletConfig;

   public void init(ServletConfig servletConfig)
   {
      this.servletConfig = servletConfig;
      processor = (EverrestProcessor)servletConfig.getServletContext().getAttribute(EverrestProcessor.class.getName());
   }

   public void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException,
      ServletException
   {
      EnvironmentContext env = new EnvironmentContext();
      env.put(HttpServletRequest.class, httpRequest);
      env.put(HttpServletResponse.class, httpResponse);
      env.put(ServletConfig.class, servletConfig);
      env.put(ServletContext.class, servletConfig.getServletContext());
      try
      {
         ServletContainerRequest request = new ServletContainerRequest(httpRequest);
         ContainerResponse response = new ContainerResponse(new ServletContainerResponseWriter(httpResponse));
         processor.process(request, response, env);
      }
      catch (IOException ioe)
      {
         if (ioe.getClass().getName().equals("org.apache.catalina.connector.ClientAbortException"))
         {
            if (LOG.isDebugEnabled())
            {
               LOG.debug(ioe.getMessage(), ioe);
            }
         }
         else
         {
            throw ioe;
         }
      }
      catch (Exception e)
      {
         LOG.error(e.getMessage(), e);
         throw new ServletException(e);
      }
   }

}
