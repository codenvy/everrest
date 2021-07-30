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
package org.everrest.guice;

import com.google.inject.Provider;

import org.everrest.core.ApplicationContext;
import org.everrest.core.FieldInjector;
import org.everrest.core.ObjectFactory;
import org.everrest.core.ObjectModel;

import java.util.List;

/**
 * @author andrew00x
 */
public class GuiceObjectFactory<T extends ObjectModel> implements ObjectFactory<T> {
    protected final T model;

    protected final Provider<?> provider;

    public GuiceObjectFactory(T model, Provider<?> provider) {
        this.model = model;
        this.provider = provider;
    }

    @Override
    public Object getInstance(ApplicationContext context) {
        Object object = provider.get();
        List<FieldInjector> fieldInjectors = model.getFieldInjectors();
        if (fieldInjectors != null && fieldInjectors.size() > 0) {
            for (FieldInjector injector : fieldInjectors) {
                if (injector.getAnnotation() != null) {
                    injector.inject(object, context);
                }
            }
        }
        return object;
    }

    @Override
    public T getObjectModel() {
        return model;
    }
}
