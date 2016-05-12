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

import org.everrest.core.resource.ResourceMethodDescriptor;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

class AsynchronousFutureFactory {
    private static final AtomicLong sequence = new AtomicLong(1);

    private static Long nextId() {
        return sequence.getAndIncrement();
    }

    public AsynchronousFuture createAsynchronousFuture(Callable<Object> callable,
                                                       long expirationDate,
                                                       ResourceMethodDescriptor resourceMethod,
                                                       List<AsynchronousJobListener> jobListeners) {
        return new AsynchronousFuture(nextId(), callable, expirationDate, resourceMethod, jobListeners);
    }
}
