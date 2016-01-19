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
package org.everrest.exoplatform;

import org.everrest.core.DependencySupplier;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.tools.ResourceLauncher;
import org.everrest.exoplatform.servlet.EverrestExoContextListener;
import org.everrest.test.mock.MockServletContext;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.StandaloneContainer;
import org.junit.After;
import org.junit.Before;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * Emulate start of JAXR-RS application via {@link EverrestExoContextListener}. EverRest itself is not configured as ExoContainer
 * components. Container used for delivering JAXR-RS components only.
 *
 * @author andrew00x
 */
public abstract class WebAppBaseTest extends BaseTest {
    protected ResourceLauncher           launcher;
    protected ResourceBinder             resources;
    protected DependencySupplier         dependencySupplier;
    protected ApplicationProviderBinder  providers;
    protected EverrestProcessor          processor;
    private   EverrestExoContextListener listener;
    private   MockServletContext         sctx;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        sctx = new MockServletContext();
        listener = new EverrestExoContextListener() {
            @Override
            protected ExoContainer getContainer(ServletContext servletContext) {
                return container;
            }
        };

        listener.contextInitialized(new ServletContextEvent(sctx));

        dependencySupplier = (DependencySupplier)sctx.getAttribute(DependencySupplier.class.getName());
        resources = (ResourceBinder)sctx.getAttribute(ResourceBinder.class.getName());
        providers = (ApplicationProviderBinder)sctx.getAttribute(ApplicationProviderBinder.class.getName());

        processor = new EverrestProcessor(resources, providers, dependencySupplier, new EverrestConfiguration(), null);
        launcher = new ResourceLauncher(processor);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        listener.contextDestroyed(new ServletContextEvent(sctx));
        super.tearDown();
    }

    protected StandaloneContainer getContainer() throws Exception {
        String conf = getClass().getResource("/conf/test-configuration-web.xml").toString();
        StandaloneContainer.setConfigurationURL(conf);
        return StandaloneContainer.getInstance();
    }
}
