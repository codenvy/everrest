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

import org.everrest.core.DependencySupplier;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.FileCollectorDestroyer;
import org.everrest.core.impl.ResourceBinderImpl;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.core.Application;

/**
 * Initialize required components of JAX-RS framework and deploy single JAX-RS application.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class EverrestInitializedListener implements ServletContextListener {
    /** {@inheritDoc} */
    public void contextDestroyed(ServletContextEvent sce) {
        makeFileCollectorDestroyer().stopFileCollector();
        ServletContext ctx = sce.getServletContext();
        EverrestProcessor processor = (EverrestProcessor)ctx.getAttribute(EverrestProcessor.class.getName());
        if (processor != null) {
            processor.stop();
        }
    }

    protected FileCollectorDestroyer makeFileCollectorDestroyer() {
        return new FileCollectorDestroyer();
    }

    /** {@inheritDoc} */
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        DependencySupplier dependencySupplier = (DependencySupplier)ctx.getAttribute(DependencySupplier.class.getName());
        if (dependencySupplier == null) {
            dependencySupplier = new ServletContextDependencySupplier(ctx);
        }
        ResourceBinder resources = new ResourceBinderImpl();
        ApplicationProviderBinder providers = new ApplicationProviderBinder();
        EverrestServletContextInitializer initializer = new EverrestServletContextInitializer(ctx);
        EverrestConfiguration config = initializer.getConfiguration();
        Application application = initializer.getApplication();
        EverrestApplication everrest = new EverrestApplication(config);
        everrest.addApplication(application);
        EverrestProcessor processor = new EverrestProcessor(resources, providers, dependencySupplier, config, everrest);
        processor.start();

        ctx.setAttribute(EverrestConfiguration.class.getName(), config);
        ctx.setAttribute(Application.class.getName(), everrest);
        ctx.setAttribute(DependencySupplier.class.getName(), dependencySupplier);
        ctx.setAttribute(ResourceBinder.class.getName(), resources);
        ctx.setAttribute(ApplicationProviderBinder.class.getName(), providers);
        ctx.setAttribute(EverrestProcessor.class.getName(), processor);
    }
}
