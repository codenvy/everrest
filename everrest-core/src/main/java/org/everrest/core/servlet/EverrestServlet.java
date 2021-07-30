/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.servlet;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.everrest.core.UnhandledException;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.tools.ErrorPages;
import org.everrest.core.tools.WebApplicationDeclaredRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author andrew00x */
@SuppressWarnings("serial")
public class EverrestServlet extends HttpServlet {
  private static final Logger LOG = LoggerFactory.getLogger(EverrestServlet.class);

  private WebApplicationDeclaredRoles webApplicationRoles;
  private ErrorPages errorPages;

  protected EverrestProcessor processor;

  @Override
  public void init() throws ServletException {
    processor =
        (EverrestProcessor) getServletContext().getAttribute(EverrestProcessor.class.getName());
    webApplicationRoles = new WebApplicationDeclaredRoles(getServletContext());
    errorPages = new ErrorPages(getServletContext());
  }

  @Override
  public void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
      throws IOException, ServletException {
    EnvironmentContext env = new EnvironmentContext();
    env.put(HttpServletRequest.class, httpRequest);
    env.put(HttpServletResponse.class, httpResponse);
    env.put(ServletConfig.class, getServletConfig());
    env.put(ServletContext.class, getServletContext());
    env.put(WebApplicationDeclaredRoles.class, webApplicationRoles);
    env.put(ErrorPages.class, errorPages);
    try {
      ServletContainerRequest request = ServletContainerRequest.create(httpRequest);
      ContainerResponse response =
          new ContainerResponse(new ServletContainerResponseWriter(httpResponse));
      processor.process(request, response, env);
    } catch (IOException ioe) {
      // Met problem with Acrobat Reader HTTP client when use EverRest for WebDav.
      // Client close connection before all data transferred and it cause error on server side.
      if (ioe.getClass().getName().equals("org.apache.catalina.connector.ClientAbortException")) {
        LOG.debug(ioe.getMessage(), ioe);
      } else {
        throw ioe;
      }
    } catch (UnhandledException e) {
      LOG.error(e.getMessage(), e);
      if (e.getResponseStatus() != 0) {
        httpResponse.sendError(e.getResponseStatus());
      } else {
        throw new ServletException(e.getCause());
      }
    } catch (Throwable e) {
      LOG.debug(e.getLocalizedMessage(), e);
      throw e;
    }
  }
}
