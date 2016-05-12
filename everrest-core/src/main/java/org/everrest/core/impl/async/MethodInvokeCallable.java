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
package org.everrest.core.impl.async;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

class MethodInvokeCallable implements Callable<Object> {
    private final Object   resource;
    private final Method   method;
    private final Object[] params;

    MethodInvokeCallable(Object resource, Method method, Object[] params) {
        this.resource = resource;
        this.method = method;
        this.params = params;
    }

    @Override
    public Object call() throws Exception {
        return method.invoke(resource, params);
    }
}
