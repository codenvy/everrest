/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;

import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.tools.ResourceLauncher;
import org.everrest.guice.servlet.EverrestGuiceContextListener;
import org.junit.After;
import org.junit.Before;
import org.mockito.ArgumentCaptor;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.util.List;

import static java.util.Collections.emptyEnumeration;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author andrew00x
 */
public abstract class BaseTest {
    private class Listener extends EverrestGuiceContextListener {
        protected ServletModule getServletModule() {
            return new ServletModule();
        }

        protected List<Module> getModules() {
            return BaseTest.this.getModules();
        }
    }

    protected EverrestProcessor processor;
    protected ResourceLauncher  launcher;
    private   ServletContext    servletContext;
    private   Listener          listener;

    @Before
    public void setUp() throws Exception {
        mockServletContext();

        listener = new Listener();
        listener.contextInitialized(new ServletContextEvent(servletContext));

        processor = (EverrestProcessor)servletContext.getAttribute(EverrestProcessor.class.getName());
        launcher = new ResourceLauncher(processor);
    }

    private void mockServletContext() {
        servletContext = mock(ServletContext.class);
        when(servletContext.getInitParameterNames()).thenReturn(emptyEnumeration());
        when(servletContext.getAttribute(Injector.class.getName())).thenAnswer(
                invocation -> retrieveComponentFromServletContext(Injector.class));
        when(servletContext.getAttribute(EverrestProcessor.class.getName())).thenAnswer(
                invocation -> retrieveComponentFromServletContext(EverrestProcessor.class));
    }

    private <T> T retrieveComponentFromServletContext(Class<T> componentType) {
        ArgumentCaptor<T> argumentCaptor = ArgumentCaptor.forClass(componentType);
        verify(servletContext, atLeastOnce()).setAttribute(eq(componentType.getName()), argumentCaptor.capture());
        return argumentCaptor.getValue();
    }

    @After
    public void tearDown() throws Exception {
        listener.contextDestroyed(new ServletContextEvent(servletContext));
    }

    protected abstract List<Module> getModules();
}
