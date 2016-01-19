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
package org.everrest.guice;

import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;

import org.everrest.core.DependencySupplier;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.tools.ResourceLauncher;
import org.everrest.guice.servlet.EverrestGuiceContextListener;
import org.everrest.test.mock.MockServletContext;
import org.junit.After;
import org.junit.Before;

import javax.servlet.ServletContextEvent;
import java.util.List;

/**
 * @author andrew00x
 */
public abstract class BaseTest {
    private class Listener extends EverrestGuiceContextListener {
        protected ServletModule getServletModule() {
            // Do not need servlet in test.
            return new ServletModule();
        }

        protected List<Module> getModules() {
            return BaseTest.this.getModules();
        }
    }

    protected EverrestProcessor  processor;
    protected ResourceLauncher   launcher;
    private   MockServletContext sctx;
    private   Listener           listener;

    @Before
    public void setUp() throws Exception {
        sctx = new MockServletContext();
        listener = new Listener();
        listener.contextInitialized(new ServletContextEvent(sctx));

        DependencySupplier dependencies = (DependencySupplier)sctx.getAttribute(DependencySupplier.class.getName());
        ResourceBinder resources = (ResourceBinder)sctx.getAttribute(ResourceBinder.class.getName());
        ApplicationProviderBinder providers = (ApplicationProviderBinder)sctx.getAttribute(ApplicationProviderBinder.class.getName());
        processor = new EverrestProcessor(resources, providers, dependencies, new EverrestConfiguration(), null);
        launcher = new ResourceLauncher(processor);
    }

    @After
    public void tearDown() throws Exception {
        listener.contextDestroyed(new ServletContextEvent(sctx));
    }

    protected abstract List<Module> getModules();
}
