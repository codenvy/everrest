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
package org.everrest.groovy.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.ws.rs.core.Application;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.servlet.EverrestServletContextInitializer;

/** @author andrew00x */
public class GroovyEverrestInitializedListener implements ServletContextListener {

  @Override
  public void contextDestroyed(ServletContextEvent event) {}

  @Override
  public void contextInitialized(ServletContextEvent event) {
    ServletContext servletContext = event.getServletContext();
    EverrestProcessor processor =
        (EverrestProcessor) servletContext.getAttribute(EverrestProcessor.class.getName());
    if (processor == null) {
      throw new RuntimeException("EverrestProcessor not found. ");
    }
    EverrestServletContextInitializer initializer =
        new GroovyEverrestServletContextInitializer(servletContext);
    Application application = initializer.getApplication();
    if (application != null) {
      processor.addApplication(application);
    }
  }
}
