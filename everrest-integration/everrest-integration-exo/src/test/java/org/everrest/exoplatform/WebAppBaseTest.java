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
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.impl.RequestHandlerImpl;
import org.everrest.core.tools.ResourceLauncher;
import org.everrest.exoplatform.servlet.EverrestExoContextListener;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.StandaloneContainer;
import org.junit.After;
import org.junit.Before;
import org.mockito.ArgumentCaptor;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import static java.util.Collections.emptyEnumeration;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private   ServletContext             servletContext;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        servletContext = mock(ServletContext.class);
        when(servletContext.getInitParameterNames()).thenReturn(emptyEnumeration());
        listener = new EverrestExoContextListener() {
            @Override
            protected ExoContainer getContainer(ServletContext servletContext) {
                return container;
            }
        };

        listener.contextInitialized(new ServletContextEvent(servletContext));

        dependencySupplier = retrieveComponentFromServletContext(DependencySupplier.class);
        resources = retrieveComponentFromServletContext(ResourceBinder.class);
        providers = retrieveComponentFromServletContext(ApplicationProviderBinder.class);

        RequestDispatcher requestDispatcher = new RequestDispatcher(resources);
        RequestHandlerImpl requestHandler = new RequestHandlerImpl(requestDispatcher, providers);
        processor = new EverrestProcessor(new EverrestConfiguration(), dependencySupplier, requestHandler, null);
        launcher = new ResourceLauncher(processor);
    }

    private <T> T retrieveComponentFromServletContext(Class<T> componentType) {
        ArgumentCaptor<T> argumentCaptor = ArgumentCaptor.forClass(componentType);
        verify(servletContext, atLeastOnce()).setAttribute(eq(componentType.getName()), argumentCaptor.capture());
        return argumentCaptor.getValue();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        listener.contextDestroyed(new ServletContextEvent(servletContext));
        super.tearDown();
    }

    protected StandaloneContainer getContainer() throws Exception {
        String conf = getClass().getResource("/conf/test-configuration-web.xml").toString();
        StandaloneContainer.setConfigurationURL(conf);
        return StandaloneContainer.getInstance();
    }
}
