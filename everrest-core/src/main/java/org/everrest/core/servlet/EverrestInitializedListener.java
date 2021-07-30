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

import org.everrest.core.DependencySupplier;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.FileCollectorDestroyer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.core.Application;

/**
 * Initialize required components of JAX-RS framework and deploy single JAX-RS application.
 *
 * @author andrew00x
 */
public class EverrestInitializedListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        makeFileCollectorDestroyer().stopFileCollector();
        ServletContext servletContext = sce.getServletContext();
        EverrestProcessor processor = (EverrestProcessor)servletContext.getAttribute(EverrestProcessor.class.getName());
        if (processor != null) {
            processor.stop();
        }
        servletContext.removeAttribute(EverrestProcessor.class.getName());
        servletContext.removeAttribute(EverrestConfiguration.class.getName());
        servletContext.removeAttribute(Application.class.getName());
        servletContext.removeAttribute(DependencySupplier.class.getName());
        servletContext.removeAttribute(ResourceBinder.class.getName());
        servletContext.removeAttribute(ApplicationProviderBinder.class.getName());
    }

    protected FileCollectorDestroyer makeFileCollectorDestroyer() {
        return new FileCollectorDestroyer();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        EverrestServletContextInitializer initializer = new EverrestServletContextInitializer(servletContext);
        initializeEverrestComponents(initializer, servletContext);
    }

    void initializeEverrestComponents(EverrestServletContextInitializer initializer, ServletContext servletContext) {
        EverrestProcessor processor = initializer.createEverrestProcessor();
        processor.start();

        servletContext.setAttribute(EverrestProcessor.class.getName(), processor);
        servletContext.setAttribute(EverrestConfiguration.class.getName(), initializer.createConfiguration());
        servletContext.setAttribute(Application.class.getName(), processor.getApplication());
        servletContext.setAttribute(DependencySupplier.class.getName(), processor.getDependencySupplier());
        servletContext.setAttribute(ResourceBinder.class.getName(), processor.getResources());
        servletContext.setAttribute(ApplicationProviderBinder.class.getName(), processor.getProviders());
    }
}
