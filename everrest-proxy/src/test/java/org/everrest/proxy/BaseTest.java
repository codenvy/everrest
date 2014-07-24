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
package org.everrest.proxy;

import junit.framework.TestCase;

import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.impl.RequestHandlerImpl;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.tools.DependencySupplierImpl;
import org.everrest.core.tools.ResourceLauncher;

public abstract class BaseTest extends TestCase {

    protected ProviderBinder providers;

    protected ResourceBinderImpl resources;

    protected RequestHandlerImpl requestHandler;

    protected ResourceLauncher launcher;

    public void setUp() throws Exception {
        this.resources = new ResourceBinderImpl();
        this.requestHandler =
                new RequestHandlerImpl(new RequestDispatcher(resources), new DependencySupplierImpl(),
                                       new EverrestConfiguration());
        this.providers = ProviderBinder.getInstance();
        this.launcher = new ResourceLauncher(requestHandler);
    }

}
