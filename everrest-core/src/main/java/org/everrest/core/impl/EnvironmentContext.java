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

import java.util.HashMap;

/**
 * Keeps objects from environment (e. g. servlet container) which can be passed
 * in resource. Parameter must be annotated by {@link javax.ws.rs.core.Context}.
 */
public class EnvironmentContext extends HashMap<Class<?>, Object> {
    private static final long serialVersionUID = 5409617947238152318L;

    /** {@link ThreadLocal} EnvironmentContext. */
    private static ThreadLocal<EnvironmentContext> current = new ThreadLocal<EnvironmentContext>();

    /**
     * @return preset {@link ThreadLocal} EnvironmentContext
     * @see ThreadLocal
     */
    public static EnvironmentContext getCurrent() {
        return current.get();
    }

    /**
     * @param env
     *         set {@link ThreadLocal} EnvironmentContext
     * @see ThreadLocal
     */
    public static void setCurrent(EnvironmentContext env) {
        current.set(env);
    }
}
