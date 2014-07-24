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
package org.everrest.exoplatform;

import org.everrest.core.DependencySupplier;
import org.everrest.core.RequestHandler;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.impl.RequestHandlerImpl;
import org.everrest.core.tools.ResourceLauncher;
import org.everrest.exoplatform.servlet.EverrestExoContextListener;
import org.everrest.test.mock.MockServletContext;
import org.exoplatform.container.ExoContainer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * Emulate start of JAXR-RS application via {@link EverrestExoContextListener}.
 * EverRest itself is not configured as ExoContainer components. Container
 * used for delivering JAXR-RS components only.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public abstract class WebAppBaseTest extends BaseTest {
    protected ResourceLauncher           launcher;
    protected ResourceBinder             resources;
    protected DependencySupplier         dependencies;
    protected ApplicationProviderBinder  providers;
    private   EverrestExoContextListener listener;
    private   MockServletContext         sctx;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        sctx = new MockServletContext();
        listener = new EverrestExoContextListener() {
            @Override
            protected ExoContainer getContainer(ServletContext servletContext) {
                return container;
            }
        };

        listener.contextInitialized(new ServletContextEvent(sctx));

        dependencies = (DependencySupplier)sctx.getAttribute(DependencySupplier.class.getName());
        resources = (ResourceBinder)sctx.getAttribute(ResourceBinder.class.getName());
        providers = (ApplicationProviderBinder)sctx.getAttribute(ApplicationProviderBinder.class.getName());

        RequestHandler requestHandler =
                new RequestHandlerImpl(new RequestDispatcher(resources), providers, dependencies, new EverrestConfiguration());
        launcher = new ResourceLauncher(requestHandler);
    }

    @Override
    protected void tearDown() throws Exception {
        listener.contextDestroyed(new ServletContextEvent(sctx));
        super.tearDown();
    }

}
