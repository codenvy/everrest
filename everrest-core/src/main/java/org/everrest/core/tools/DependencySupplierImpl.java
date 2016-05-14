/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.tools;

import org.everrest.core.BaseDependencySupplier;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple dependency resolver.
 *
 * @author andrew00x
 */
public class DependencySupplierImpl extends BaseDependencySupplier {
    private final Map<Class<?>, Object> dependencies = new HashMap<Class<?>, Object>();

    public DependencySupplierImpl() {
    }

    public DependencySupplierImpl(Class<? extends Annotation> injectAnnotation) {
        super(injectAnnotation);
    }

    public void addInstance(Class<?> key, Object instance) {
        dependencies.put(key, instance);
    }

    @Override
    public Object getInstance(Class<?> type) {
        return dependencies.get(type);
    }
}
