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
package org.everrest.pico;

import org.everrest.core.ApplicationContext;
import org.everrest.core.FieldInjector;
import org.everrest.core.ObjectFactory;
import org.everrest.core.ObjectModel;
import org.everrest.pico.servlet.EverrestPicoFilter;

import java.util.List;

/**
 * @author andrew00x
 */
public class PicoObjectFactory<T extends ObjectModel> implements ObjectFactory<T> {

    protected final T model;

    public PicoObjectFactory(T model) {
        this.model = model;
    }

    @Override
    public Object getInstance(ApplicationContext context) {
        Class<?> clazz = model.getObjectClass();
        Object component = EverrestPicoFilter.getComponent(clazz);
        if (component != null) {
            List<FieldInjector> fieldInjectors = model.getFieldInjectors();
            if (fieldInjectors != null && fieldInjectors.size() > 0) {
                for (FieldInjector injector : fieldInjectors) {
                    if (injector.getAnnotation() != null) {
                        injector.inject(component, context);
                    }
                }
            }
        }
        return component;
    }

    @Override
    public T getObjectModel() {
        return model;
    }

}
