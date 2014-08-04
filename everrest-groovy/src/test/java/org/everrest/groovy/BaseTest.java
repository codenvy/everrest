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
package org.everrest.groovy;

import junit.framework.TestCase;

import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.core.impl.async.AsynchronousProcessListWriter;
import org.everrest.core.tools.DependencySupplierImpl;
import org.everrest.core.tools.ResourceLauncher;

import java.lang.reflect.Constructor;

/**
 * @author andrew00x
 */
public abstract class BaseTest extends TestCase {
    protected ProviderBinder          providers;
    protected ResourceBinderImpl      resources;
    protected DependencySupplierImpl  dependencySupplier;
    protected EverrestProcessor       processor;
    protected ResourceLauncher        launcher;
    protected GroovyResourcePublisher groovyPublisher;

    protected void setUp() throws Exception {
        resources = new ResourceBinderImpl();
        // reset embedded providers to be sure it is clean
        Constructor<ProviderBinder> c = ProviderBinder.class.getDeclaredConstructor();
        c.setAccessible(true);
        ProviderBinder.setInstance(c.newInstance());
        providers = new ApplicationProviderBinder();
        providers.addMessageBodyWriter(new AsynchronousProcessListWriter());
        resources.addResource("/async", AsynchronousJobService.class, null);
        dependencySupplier = new DependencySupplierImpl();
        processor = new EverrestProcessor(resources, providers, dependencySupplier, new EverrestConfiguration(), null);
        launcher = new ResourceLauncher(processor);
        groovyPublisher = new GroovyResourcePublisher(resources, dependencySupplier);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
