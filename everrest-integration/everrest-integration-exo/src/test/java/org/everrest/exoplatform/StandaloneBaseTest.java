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
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.tools.ResourceLauncher;
import org.exoplatform.container.StandaloneContainer;
import org.junit.Before;

/**
 * Initialize EverRest framework by ExoContainer. EverRest itself and JAX-RS application are configured as ExoContainer components.
 *
 * @author andrew00x
 */
public abstract class StandaloneBaseTest extends BaseTest {
    protected ResourceLauncher          launcher;
    protected ResourceBinder            resources;
    protected DependencySupplier        dependencySupplier;
    protected ApplicationProviderBinder providers;
    protected EverrestProcessor         processor;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        dependencySupplier = (DependencySupplier)container.getComponentInstanceOfType(DependencySupplier.class);
        resources = (ResourceBinder)container.getComponentInstanceOfType(ResourceBinder.class);
        providers = (ApplicationProviderBinder)container.getComponentInstanceOfType(ApplicationProviderBinder.class);
        processor = (EverrestProcessor)container.getComponentInstanceOfType(EverrestProcessor.class);
        launcher = new ResourceLauncher(processor);
    }

    protected StandaloneContainer getContainer() throws Exception {
        String conf = getClass().getResource("/conf/test-configuration-standalone.xml").toString();
        StandaloneContainer.setConfigurationURL(conf);
        return StandaloneContainer.getInstance();
    }
}
