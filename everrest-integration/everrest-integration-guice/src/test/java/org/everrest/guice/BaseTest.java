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
package org.everrest.guice;

import junit.framework.TestCase;

import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;

import org.everrest.core.DependencySupplier;
import org.everrest.core.RequestHandler;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.impl.RequestHandlerImpl;
import org.everrest.core.tools.ResourceLauncher;
import org.everrest.guice.servlet.EverrestGuiceContextListener;
import org.everrest.test.mock.MockServletContext;

import javax.servlet.ServletContextEvent;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public abstract class BaseTest extends TestCase {
    private class Listener extends EverrestGuiceContextListener {
        protected ServletModule getServletModule() {
            // Do not need servlet in test.
            return new ServletModule();
        }

        protected List<Module> getModules() {
            return BaseTest.this.getModules();
        }
    }

    protected ResourceLauncher   launcher;
    private   MockServletContext sctx;
    private   Listener           listener;

    public void setUp() throws Exception {
        sctx = new MockServletContext();
        listener = new Listener();
        listener.contextInitialized(new ServletContextEvent(sctx));

        DependencySupplier dependencies = (DependencySupplier)sctx.getAttribute(DependencySupplier.class.getName());
        ResourceBinder resources = (ResourceBinder)sctx.getAttribute(ResourceBinder.class.getName());
        ApplicationProviderBinder providers =
                (ApplicationProviderBinder)sctx.getAttribute(ApplicationProviderBinder.class.getName());
        RequestHandler requestHandler =
                new RequestHandlerImpl(new RequestDispatcher(resources), providers, dependencies, new EverrestConfiguration());
        launcher = new ResourceLauncher(requestHandler);
    }

    @Override
    protected void tearDown() throws Exception {
        listener.contextDestroyed(new ServletContextEvent(sctx));
        super.tearDown();
    }

    protected abstract List<Module> getModules();
}
