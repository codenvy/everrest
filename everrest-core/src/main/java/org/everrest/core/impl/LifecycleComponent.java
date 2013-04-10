/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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