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
package org.everrest.spring;

import org.everrest.core.DependencySupplier;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.impl.RequestHandlerImpl;
import org.everrest.core.tools.ResourceLauncher;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author andrew00x
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/spring-component-test.xml"})
public abstract class BaseTest {
    @Autowired
    protected ProviderBinder                  providers;
    @Autowired
    protected ResourceBinder                  resources;
    @Autowired
    protected DependencySupplier              dependencies;
    @Autowired
    protected ConfigurableListableBeanFactory factory;

    protected EverrestProcessor processor;
    protected ResourceLauncher  launcher;

    @Before
    public void start() throws Exception {
        RequestDispatcher requestDispatcher = new RequestDispatcher(resources);
        RequestHandlerImpl requestHandler = new RequestHandlerImpl(requestDispatcher, providers);
        processor = new EverrestProcessor(new EverrestConfiguration(), dependencies, requestHandler, null);
        launcher = new ResourceLauncher(processor);
    }

    @After
    public void stop() {
        //factory.destroyScopedBean("org.everrest.lifecycle.SpringEverrestProcessorDestroyer");
    }
}
