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
package org.everrest.exoplatform;

import org.everrest.core.DependencySupplier;
import org.everrest.core.RequestHandler;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.tools.ResourceLauncher;

/**
 * Initialize EverRest framework by ExoContainer.
 * EverRest itself and JAX-RS application are configured as ExoContainer components.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public abstract class StandaloneBaseTest extends BaseTest {
    protected ResourceLauncher   launcher;
    protected ResourceBinder     resources;
    protected DependencySupplier dependencies;
    protected ProvidersRegistry  providersRegistry;
    protected RequestHandler     requestHandler;
    protected RequestDispatcher  requestDispatcher;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dependencies = (DependencySupplier)container.getComponentInstanceOfType(DependencySupplier.class);
        resources = (ResourceBinder)container.getComponentInstanceOfType(ResourceBinder.class);
        providersRegistry = (ProvidersRegistry)container.getComponentInstanceOfType(ProvidersRegistry.class);
        requestHandler = (RequestHandler)container.getComponentInstanceOfType(RequestHandler.class);
        launcher = new ResourceLauncher(requestHandler);
        requestDispatcher = (RequestDispatcher)container.getComponentInstanceOfType(RequestDispatcher.class);
    }
}
