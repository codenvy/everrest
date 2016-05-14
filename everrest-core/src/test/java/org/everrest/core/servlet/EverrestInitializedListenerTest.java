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
package org.everrest.core.servlet;

import org.everrest.core.DependencySupplier;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestApplication;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.ProviderBinder;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.ws.rs.core.Application;

import static java.util.Collections.emptyEnumeration;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EverrestInitializedListenerTest {
    private ServletContext servletContext;

    @Before
    public void setUp() throws Exception {
        servletContext = mock(ServletContext.class);
        when(servletContext.getInitParameterNames()).thenReturn(emptyEnumeration());
    }

    @Test
    public void setsEverrestComponentsAsServletContextAttributeWhenServletContextInitialized() {
        EverrestInitializedListener everrestInitializedListener = new EverrestInitializedListener();
        ServletContextEvent servletContextEvent = new ServletContextEvent(servletContext);
        everrestInitializedListener.contextInitialized(servletContextEvent);

        verify(servletContext).setAttribute(eq(EverrestProcessor.class.getName()), isA(EverrestProcessor.class));
        verify(servletContext).setAttribute(eq(EverrestConfiguration.class.getName()), isA(EverrestConfiguration.class));
        verify(servletContext).setAttribute(eq(Application.class.getName()), isA(EverrestApplication.class));
        verify(servletContext).setAttribute(eq(ApplicationProviderBinder.class.getName()), isA(ProviderBinder.class));
        verify(servletContext).setAttribute(eq(ResourceBinder.class.getName()), isA(ResourceBinder.class));
        verify(servletContext).setAttribute(eq(DependencySupplier.class.getName()), isA(DependencySupplier.class));
    }

    @Test
    public void removesEverrestComponentsFromServletContextAttributeWhenServletContextDestroyed() {
        EverrestInitializedListener everrestInitializedListener = new EverrestInitializedListener();
        ServletContextEvent servletContextEvent = new ServletContextEvent(servletContext);
        everrestInitializedListener.contextDestroyed(servletContextEvent);

        verify(servletContext).removeAttribute(eq(EverrestProcessor.class.getName()));
        verify(servletContext).removeAttribute(eq(EverrestConfiguration.class.getName()));
        verify(servletContext).removeAttribute(eq(Application.class.getName()));
        verify(servletContext).removeAttribute(eq(ApplicationProviderBinder.class.getName()));
        verify(servletContext).removeAttribute(eq(ResourceBinder.class.getName()));
        verify(servletContext).removeAttribute(eq(DependencySupplier.class.getName()));
    }

    @Test
    public void startsEverrestProcessorWhenServletContextInitialized() throws Exception {
        EverrestProcessor everrestProcessor = mock(EverrestProcessor.class);
        EverrestServletContextInitializer initializer = mock(EverrestServletContextInitializer.class);
        when(initializer.createEverrestProcessor()).thenReturn(everrestProcessor);

        EverrestInitializedListener everrestInitializedListener = new EverrestInitializedListener();
        everrestInitializedListener.initializeEverrestComponents(initializer, servletContext);

        verify(everrestProcessor).start();
    }

    @Test
    public void stopsEverrestProcessorWhenServletContextDestroyed() throws Exception {
        EverrestProcessor everrestProcessor = mock(EverrestProcessor.class);
        when(servletContext.getAttribute(EverrestProcessor.class.getName())).thenReturn(everrestProcessor);

        EverrestInitializedListener everrestInitializedListener = new EverrestInitializedListener();
        ServletContextEvent servletContextEvent = new ServletContextEvent(servletContext);
        everrestInitializedListener.contextDestroyed(servletContextEvent);

        verify(everrestProcessor).stop();
    }
}