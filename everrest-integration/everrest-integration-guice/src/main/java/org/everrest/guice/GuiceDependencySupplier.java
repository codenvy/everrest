/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.guice;

import com.google.inject.Injector;

import org.everrest.core.BaseDependencySupplier;

/**
 * @author andrew00x
 */
public class GuiceDependencySupplier extends BaseDependencySupplier {
    private final Injector injector;

    public GuiceDependencySupplier(Injector injector) {
        this.injector = injector;
    }

    @Override
    public Object getInstance(Class<?> type) {
        return injector.getInstance(type);
    }
}
