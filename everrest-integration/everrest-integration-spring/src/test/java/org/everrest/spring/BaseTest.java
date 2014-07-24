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
package org.everrest.spring;

import org.everrest.core.DependencySupplier;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.EverrestConfiguration;
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
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
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
    protected RequestHandlerImpl              requestHandler;
    protected ResourceLauncher                launcher;
    @Autowired
    protected ConfigurableListableBeanFactory factory;

    @Before
    public void start() throws Exception {
        requestHandler =
                new RequestHandlerImpl(new RequestDispatcher(resources), providers, dependencies, new EverrestConfiguration());
        launcher = new ResourceLauncher(requestHandler);
    }

    @After
    public void stop() {
        //factory.destroyScopedBean("org.everrest.lifecycle.SpringEverrestProcessorDestroyer");
    }
}
