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
package org.everrest.core.impl;

import org.everrest.core.impl.method.filter.SecurityConstraint;

/**
 * Helper class provide method to reset state of protected fields.
 */
public class ApplicationProviderBinderHelper {
    public static void resetApplicationProviderBinder(ApplicationProviderBinder binder){

        binder.writeProviders.clear();
        binder.readProviders.clear();
        binder.exceptionMappers.clear();
        binder.contextResolvers.clear();
        binder.readProviders.clear();
        binder.responseFilters.clear();
        binder.invokerFilters.clear();
        binder.addMethodInvokerFilter(new SecurityConstraint());
    }
}
