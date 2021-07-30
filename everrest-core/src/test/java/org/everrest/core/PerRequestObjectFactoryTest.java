/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core;

import org.everrest.core.impl.LifecycleComponent;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PerRequestObjectFactoryTest {

    private PerRequestObjectFactory perRequestObjectFactory;

    private ApplicationContext    applicationContext;
    private ConstructorDescriptor constructorDescriptor;
    private FieldInjector         fieldInjector;
    private List<LifecycleComponent> lifecycleComponents = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        lifecycleComponents.clear();
        applicationContext = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
        when(applicationContext.getAttributes().get("org.everrest.lifecycle.PerRequest")).thenReturn(lifecycleComponents);

        constructorDescriptor = mock(ConstructorDescriptor.class);

        fieldInjector = mock(FieldInjector.class);

        ObjectModel objectModel = mock(ObjectModel.class);
        when(objectModel.getConstructorDescriptors()).thenReturn(newArrayList(constructorDescriptor));
        when(objectModel.getFieldInjectors()).thenReturn(newArrayList(fieldInjector));

        perRequestObjectFactory = new PerRequestObjectFactory(objectModel);
    }

    @Test
    public void createsInstance() throws Exception {
        Object instance = new Object();
        when(constructorDescriptor.createInstance(applicationContext)).thenReturn(instance);
        Object result = perRequestObjectFactory.getInstance(applicationContext);
        assertSame(instance, result);
    }

    @Test
    public void injectsFields() throws Exception {
        Object instance = new Object();
        when(constructorDescriptor.createInstance(applicationContext)).thenReturn(instance);
        perRequestObjectFactory.getInstance(applicationContext);

        verify(fieldInjector).inject(instance, applicationContext);
    }

    @Test
    public void addsLifeCycleComponentInApplicationContext() throws Exception {
        Object instance = new Object();
        when(constructorDescriptor.createInstance(applicationContext)).thenReturn(instance);
        perRequestObjectFactory.getInstance(applicationContext);

        assertEquals(1, lifecycleComponents.size());
        assertSame(instance, lifecycleComponents.get(0).getComponent());
    }
}
