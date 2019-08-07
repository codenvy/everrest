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
package org.everrest.core.impl;

import org.everrest.core.LifecycleMethodStrategy;

/**
 * Life cycle wrapper for JAX-RS component (resource or provider).
 *
 * @see LifecycleMethodStrategy
 */
public class LifecycleComponent {
    private static final LifecycleMethodStrategy defaultStrategy = new AnnotatedLifecycleMethodStrategy();

    private final Object                  component;
    private final LifecycleMethodStrategy lifecycleStrategy;

    public LifecycleComponent(Object component) {
        this(component, defaultStrategy);
    }

    public LifecycleComponent(Object component, LifecycleMethodStrategy lifecycleStrategy) {
        this.component = component;
        this.lifecycleStrategy = lifecycleStrategy;
    }

    public void initialize() {
        lifecycleStrategy.invokeInitializeMethods(component);
    }

    public void destroy() {
        lifecycleStrategy.invokeDestroyMethods(component);
    }

    public Object getComponent() {
        return component;
    }
}