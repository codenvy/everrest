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
        ctx.setAttribute(DependencySupplier.class.getName(), dependencySupplier);
        ctx.setAttribute(ResourceBinder.class.getName(), resources);
        ctx.setAttribute(ApplicationProviderBinder.class.getName(), providers);
        ctx.setAttribute(EverrestProcessor.class.getName(), processor);
    }
}
