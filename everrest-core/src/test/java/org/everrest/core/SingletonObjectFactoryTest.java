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
package org.everrest.core;

import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class SingletonObjectFactoryTest {
    @Test
    public void returnsInstanceGivenInCreationOfObjectFactory() {
        Object instance = new Object();
        ObjectModel objectModel = mock(ObjectModel.class);
        SingletonObjectFactory singletonObjectFactory = new SingletonObjectFactory<>(objectModel, instance);

        Object result = singletonObjectFactory.getInstance(null);
        assertSame(instance, result);
    }
}