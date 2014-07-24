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
package org.everrest.core.impl;

import org.everrest.core.LifecycleMethodStrategy;

/**
 * Life cycle wrapper for JAX-RS component (resource or provider).
 *
 * @see LifecycleMethodStrategy
 */
public final class LifecycleComponent {
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
}