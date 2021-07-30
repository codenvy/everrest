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