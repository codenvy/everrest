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
package org.everrest.core;

/**
 * Provide object instance of components that support singleton lifecycle.
 *
 * @param <T>
 * @author andrew00x
 */
public class SingletonObjectFactory<T extends ObjectModel> implements ObjectFactory<T> {
    /** @see ObjectModel */
    protected final T model;

    /** Component instance. */
    protected final Object object;

    /**
     * @param model
     *         ObjectMode
     * @param object
     *         component instance
     */
    public SingletonObjectFactory(T model, Object object) {
        this.model = model;
        this.object = object;
    }

    @Override
    public Object getInstance(ApplicationContext context) {
        return getInstance();
    }

    public Object getInstance() {
        return object;
    }

    @Override
    public T getObjectModel() {
        return model;
    }
}
