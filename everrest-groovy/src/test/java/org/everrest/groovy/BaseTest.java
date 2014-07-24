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

import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.impl.RequestHandlerImpl;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.tools.DependencySupplierImpl;
import org.everrest.core.tools.ResourceLauncher;

import java.lang.reflect.Constructor;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public abstract class BaseTest extends TestCase {
    protected ProviderBinder providers;

    protected ResourceBinderImpl resources;

    protected RequestHandlerImpl requestHandler;

    protected ResourceLauncher launcher;

    protected GroovyResourcePublisher groovyPublisher;

    protected DependencySupplierImpl dependencies;

    protected void setUp() throws Exception {
        this.resources = new ResourceBinderImpl();
        this.dependencies = new DependencySupplierImpl();
        Constructor<ProviderBinder> c = ProviderBinder.class.getDeclaredConstructor();
        c.setAccessible(true);
        ProviderBinder.setInstance(c.newInstance());
        this.providers = ProviderBinder.getInstance();
        this.requestHandler = new RequestHandlerImpl(new RequestDispatcher(resources), dependencies, null);
        this.launcher = new ResourceLauncher(requestHandler);
        this.groovyPublisher = new GroovyResourcePublisher(resources, dependencies);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
