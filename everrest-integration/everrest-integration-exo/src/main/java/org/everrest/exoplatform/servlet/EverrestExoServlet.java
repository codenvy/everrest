/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.everrest.exoplatform.servlet;

import org.everrest.core.RequestHandler;
import org.everrest.core.UnhandledException;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.servlet.ServletContainerRequest;
import org.everrest.core.servlet.ServletContainerResponseWriter;
import org.everrest.core.tools.WebApplicationDeclaredRoles;
import org.everrest.core.util.Logger;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.web.AbstractHttpServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Use this servlet for integration in eXo environment. If just need to use IoC container then
 * use org.everrest.core.servlet.EverrestServlet. Usage of this servlet assumes that components of EverRest framework
 * registered in ExoContainer. To do so do not use EverrestExoContextListener as bootstrap but use
 * org.everrest.exoplatform.EverrestInitializer instead and provide corresponded configuration for ExoContainer.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
@SuppressWarnings("serial")
public class EverrestExoServlet extends AbstractHttpServlet {
    private static final Logger log = Logger.getLogger(EverrestExoServlet.class);
    private WebApplicationDeclaredRoles webApplicationRoles;

    @Override
    protected void afterInit(ServletConfig config) throws ServletException {
        webApplicationRoles = new WebApplicationDeclaredRoles(getServletContext());
    }

    /**
     * @see org.exoplatform.container.web.AbstractHttpServlet#onService(org.exoplatform.container.ExoContainer,
     *      javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void onService(ExoContainer container, HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        RequestLifeCycle.begin(container);

        RequestHandler requestHandler = (RequestHandler)container.getComponentInstanceOfType(RequestHandler.class);

        EnvironmentContext env = new EnvironmentContext();
        env.put(HttpServletRequest.class, req);
        env.put(HttpServletResponse.class, res);
        env.put(ServletConfig.class, config);
        env.put(ServletContext.class, getServletContext());
        env.put(WebApplicationDeclaredRoles.class, webApplicationRoles);

        try {
            EnvironmentContext.setCurrent(env);
            ServletContainerRequest request = new ServletContainerRequest(req);
            ContainerResponse response = new ContainerResponse(new ServletContainerResponseWriter(res));
            requestHandler.handleRequest(request, response);
        } catch (IOException ioe) {
            // Met problem with Acrobat Reader HTTP client when use EverRest for WebDav.
            // Client close connection before all data transferred and it cause error on server side.
            if (ioe.getClass().getName().equals("org.apache.catalina.connector.ClientAbortException")) {
                if (log.isDebugEnabled()) {
                    log.debug(ioe.getMessage(), ioe);
                }
            } else {
                throw ioe;
            }
        } catch (UnhandledException e) {
            throw new ServletException(e);
        } finally {
            EnvironmentContext.setCurrent(null);
            RequestLifeCycle.end();
        }
    }
}
