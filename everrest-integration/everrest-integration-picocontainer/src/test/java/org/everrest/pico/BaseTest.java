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
package org.everrest.pico;

import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.tools.ResourceLauncher;
import org.everrest.pico.servlet.EverrestPicoFilter;
import org.junit.After;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.Caching;

import javax.servlet.ServletContext;

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
    protected abstract class Composer extends EverrestComposer {
        protected void doComposeApplication(MutablePicoContainer container, ServletContext servletContext) {
        }

        protected void doComposeRequest(MutablePicoContainer container) {
        }

        protected void doComposeSession(MutablePicoContainer container) {
        }
    }

    protected EverrestProcessor    processor;
    protected ResourceLauncher     launcher;
    private   DefaultPicoContainer appContainer;

    @Before
    public void setUp() throws Exception {
        appContainer = new DefaultPicoContainer(new Caching());
        appContainer.start();
        DefaultPicoContainer sesContainer = new DefaultPicoContainer();
        DefaultPicoContainer reqContainer = new DefaultPicoContainer();
        Composer composer = getComposer();

        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getInitParameterNames()).thenReturn(emptyEnumeration());

        composer.composeApplication(appContainer, servletContext);
        composer.composeSession(sesContainer);
        composer.composeRequest(reqContainer);

        // NOTE Injection for constructors will not work properly. Just set up scoped containers here.
        EverrestPicoFilter picoFilter = new EverrestPicoFilter();
        picoFilter.setAppContainer(appContainer);
        picoFilter.setSessionContainer(sesContainer);
        picoFilter.setRequestContainer(reqContainer);
        picoFilter.initAdditionalScopedComponents(sesContainer, reqContainer);

        processor = retrieveComponentFromServletContext(EverrestProcessor.class, servletContext);
        launcher = new ResourceLauncher(processor);
    }

    private <T> T retrieveComponentFromServletContext(Class<T> componentType, ServletContext servletContext) {
        ArgumentCaptor<T> argumentCaptor = ArgumentCaptor.forClass(componentType);
        verify(servletContext, atLeastOnce()).setAttribute(eq(componentType.getName()), argumentCaptor.capture());
        return argumentCaptor.getValue();
    }


    @After
    public void tearDown() throws Exception {
        appContainer.stop();
        appContainer.dispose();
    }

    protected abstract Composer getComposer();
}
