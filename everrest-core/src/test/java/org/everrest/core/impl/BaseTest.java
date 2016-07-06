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
package org.everrest.core.impl;

import org.everrest.core.impl.async.AsynchronousJobPool;
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
    protected DependencySupplierImpl dependencySupplier;
    protected EverrestProcessor      processor;
    protected ResourceLauncher       launcher;
    protected AsynchronousJobPool    asynchronousPool;

    @Before
    public void setUp() throws Exception {
        ResourceBinderImpl resources = new ResourceBinderImpl();
        resetProviderBinder();
        ProviderBinder providers = new ApplicationProviderBinder();
        asynchronousPool = new AsynchronousJobPool(new EverrestConfiguration());
        providers.addContextResolver(asynchronousPool);
        providers.addMessageBodyWriter(new AsynchronousProcessListWriter());
        resources.addResource("/async", AsynchronousJobService.class, null);
        dependencySupplier = new DependencySupplierImpl();
        processor = new EverrestProcessor(new EverrestConfiguration(), dependencySupplier, new RequestHandlerImpl(new RequestDispatcher(resources), providers), null);
        launcher = new ResourceLauncher(processor);
    }

    private void resetProviderBinder() {
        ProviderBinder providerBinder = new ProviderBinder();
        providerBinder.init();
        ProviderBinder.setInstance(providerBinder);
    }

    @After
    public void tearDown() throws Exception {
        asynchronousPool.stop();
        processor.stop();
    }
}
