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
package org.everrest.groovy;

import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.impl.RequestHandlerImpl;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.core.impl.async.AsynchronousProcessListWriter;
import org.everrest.core.tools.DependencySupplierImpl;
import org.everrest.core.tools.ResourceLauncher;
import org.junit.After;
import org.junit.Before;

/**
 * @author andrew00x
 */
public abstract class BaseTest {
    protected ProviderBinder          providers;
    protected ResourceBinderImpl      resources;
    protected DependencySupplierImpl  dependencySupplier;
    protected EverrestProcessor       processor;
    protected ResourceLauncher        launcher;
    protected GroovyResourcePublisher groovyPublisher;

    @Before
    public void setUp() throws Exception {
        resources = new ResourceBinderImpl();
        ProviderBinder.setInstance(null);
        providers = new ApplicationProviderBinder();
        providers.addMessageBodyWriter(new AsynchronousProcessListWriter());
        resources.addResource("/async", AsynchronousJobService.class, null);
        dependencySupplier = new DependencySupplierImpl();
        RequestDispatcher dispatcher = new RequestDispatcher(resources);
        RequestHandlerImpl requestHandler = new RequestHandlerImpl(dispatcher, providers);
        processor = new EverrestProcessor(new EverrestConfiguration(), dependencySupplier, requestHandler, null);
        launcher = new ResourceLauncher(processor);
        groovyPublisher = new GroovyResourcePublisher(resources, dependencySupplier);
    }

    @After
    public void tearDown() throws Exception {
    }
}
