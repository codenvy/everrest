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
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 * @see EverrestHandlerMapping
 */
public class EverrestHandlerAdapter implements HandlerAdapter, ServletContextAware, ServletConfigAware {
    private ServletContext              servletContext;
    private ServletConfig               servletConfig;
    private WebApplicationDeclaredRoles webApplicationRoles;

    /** {@inheritDoc} */
    public long getLastModified(HttpServletRequest request, Object handler) {
        return -1;
    }

    /** {@inheritDoc} */
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        EnvironmentContext env = new EnvironmentContext();
        env.put(HttpServletRequest.class, request);
        env.put(HttpServletResponse.class, request);
        env.put(ServletConfig.class, servletConfig);
        env.put(ServletContext.class, servletContext);
        env.put(WebApplicationDeclaredRoles.class, webApplicationRoles);
        //System.out.println("\n\n" + webApplicationRoles.getDeclaredRoles() + "\n");
        ((EverrestProcessor)handler).process(new ServletContainerRequest(request), new ContainerResponse(
                new ServletContainerResponseWriter(response)), env);
        // return null since request handled directly.
        return null;
    }

    /** {@inheritDoc} */
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }

    /** {@inheritDoc} */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        webApplicationRoles = new WebApplicationDeclaredRoles(servletContext);
    }

    /** {@inheritDoc} */
    public boolean supports(Object handler) {
        return handler instanceof EverrestProcessor;
    }
}
