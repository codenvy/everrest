/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.servlet;

import org.everrest.core.UnhandledException;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.tools.ErrorPages;
import org.everrest.core.tools.WebApplicationDeclaredRoles;
import org.everrest.core.util.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author andrew00x
 */
@SuppressWarnings("serial")
public class EverrestServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(EverrestServlet.class.getName());
    private WebApplicationDeclaredRoles webApplicationRoles;
    private ErrorPages                  errorPages;

    protected EverrestProcessor processor;

    @Override
    public void init() throws ServletException {
        processor = (EverrestProcessor)getServletConfig().getServletContext().getAttribute(EverrestProcessor.class.getName());
        webApplicationRoles = new WebApplicationDeclaredRoles(getServletContext());
        errorPages = new ErrorPages(getServletContext());
    }

    @Override
    public void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException,
                                                                                                 ServletException {
        EnvironmentContext env = new EnvironmentContext();
        env.put(HttpServletRequest.class, httpRequest);
        env.put(HttpServletResponse.class, httpResponse);
        env.put(ServletConfig.class, getServletConfig());
        final ServletContext servletContext = getServletContext();
        env.put(ServletContext.class, servletContext);
        env.put(WebApplicationDeclaredRoles.class, webApplicationRoles);
        env.put(ErrorPages.class, errorPages);
        try {
            ServletContainerRequest request = ServletContainerRequest.create(httpRequest);
            ContainerResponse response = new ContainerResponse(new ServletContainerResponseWriter(httpResponse));
            processor.process(request, response, env);
        } catch (IOException ioe) {
            // Met problem with Acrobat Reader HTTP client when use EverRest for WebDav.
            // Client close connection before all data transferred and it cause error on server side.
            if (ioe.getClass().getName().equals("org.apache.catalina.connector.ClientAbortException")) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(ioe.getMessage(), ioe);
                }
            } else {
                throw ioe;
            }
        } catch (UnhandledException e) {
            LOG.error(e.getMessage(), e);
            throw new ServletException(e.getCause());
        }
    }
}
