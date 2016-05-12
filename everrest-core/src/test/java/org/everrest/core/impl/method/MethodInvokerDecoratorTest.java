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
package org.everrest.core.impl.method;

import org.everrest.core.ApplicationContext;
import org.everrest.core.method.MethodInvoker;
import org.everrest.core.resource.GenericResourceMethod;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MethodInvokerDecoratorTest {
    private MethodInvokerDecorator decorator;
    private MethodInvoker          methodInvoker;

    @Before
    public void setUp() throws Exception {
        methodInvoker = mock(MethodInvoker.class);
        decorator = new MethodInvokerDecorator(methodInvoker);
    }

    @Test
    public void delegatesCallToMethodInvoker() throws Exception {
        Object object = new Object();
        GenericResourceMethod method = mock(GenericResourceMethod.class);
        ApplicationContext applicationContext = mock(ApplicationContext.class);

        decorator.invokeMethod(object, method, applicationContext);

        verify(methodInvoker).invokeMethod(object, method, applicationContext);
    }
}