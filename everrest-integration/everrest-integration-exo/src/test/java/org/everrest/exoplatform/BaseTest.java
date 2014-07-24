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

import junit.framework.TestCase;

import org.everrest.core.impl.ProviderBinder;
import org.exoplatform.container.StandaloneContainer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * @author andrew00x
 */
public abstract class BaseTest extends TestCase {
    protected StandaloneContainer container;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // reset set of providers for each test
        Constructor<ProviderBinder> c = ProviderBinder.class.getDeclaredConstructor();
        c.setAccessible(true);
        ProviderBinder.setInstance(c.newInstance());

        String conf = getClass().getResource("/conf/test-configuration.xml").toString();
        StandaloneContainer.setConfigurationURL(conf);
        container = StandaloneContainer.getInstance();
    }

    @Override
    protected void tearDown() throws Exception {
        container.stop();
        Field containerField = StandaloneContainer.class.getDeclaredField("container");
        containerField.setAccessible(true);
        containerField.set(null, null);
        container = null;
        super.tearDown();
    }
}
