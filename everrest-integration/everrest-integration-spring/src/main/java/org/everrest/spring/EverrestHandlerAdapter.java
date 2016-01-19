/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.spring;

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.servlet.ServletContainerRequest;
import org.everrest.core.servlet.ServletContainerResponseWriter;
import org.everrest.core.tools.WebApplicationDeclaredRoles;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handler adapter for EverrestProcessor. Implements ServletContextAware,
 * ServletConfigAware since we need ServletContext and ServletConfig to be able
 * inject it via &#64;Context annotation to resources or providers.
 *
 * @author andrew00x
 */
public class EverrestHandlerAdapter implements HandlerAdapter, ServletContextAware, ServletConfigAware {
    private ServletContext              servletContext;
    private ServletConfig               servletConfig;
    private WebApplicationDeclaredRoles webApplicationRoles;


    @Override
    public long getLastModified(HttpServletRequest request, Object handler) {
        return -1;
    }


    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        EnvironmentContext env = new EnvironmentContext();
        env.put(HttpServletRequest.class, request);
        env.put(HttpServletResponse.class, request);
        env.put(ServletConfig.class, servletConfig);
        env.put(ServletContext.class, servletContext);
        env.put(WebApplicationDeclaredRoles.class, webApplicationRoles);
        //System.out.println("\n\n" + webApplicationRoles.getDeclaredRoles() + "\n");
        ((EverrestProcessor)handler).process(ServletContainerRequest.create(request), new ContainerResponse(
                new ServletContainerResponseWriter(response)), env);
        // return null since request handled directly.
        return null;
    }


    @Override
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }


    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        webApplicationRoles = new WebApplicationDeclaredRoles(servletContext);
    }


    @Override
    public boolean supports(Object handler) {
        return handler instanceof EverrestProcessor;
    }
}
