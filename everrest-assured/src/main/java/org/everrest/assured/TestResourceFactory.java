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
package org.everrest.assured;

import org.everrest.core.ApplicationContext;
import org.everrest.core.FieldInjector;
import org.everrest.core.ObjectFactory;
import org.everrest.core.ObjectModel;
import org.everrest.core.PerRequestObjectFactory;
import org.everrest.core.SingletonObjectFactory;
import org.everrest.core.impl.provider.ProviderDescriptorImpl;
import org.everrest.core.provider.ProviderDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;

/** Get instance of the REST resource from test class in request time. */
public class TestResourceFactory<T extends ObjectModel> implements ObjectFactory {

    private static final Logger LOG = LoggerFactory.getLogger(TestResourceFactory.class);
    private final Object testParent;
    private final Field  resourceField;
    private final T      model;

    public TestResourceFactory(T model, Object testParent, Field resourceField) {
        this.model = model;
        this.testParent = testParent;
        this.resourceField = resourceField;
        this.resourceField.setAccessible(true);
    }

    /** @see org.everrest.core.ObjectFactory#getInstance(org.everrest.core.ApplicationContext) */
    @Override
    public Object getInstance(ApplicationContext context) {
        try {

            Object object = resourceField.get(testParent);
            if (object != null) {
                ProviderDescriptor descriptor = new ProviderDescriptorImpl(object);
                List<FieldInjector> fieldInjectors = model.getFieldInjectors();
                if (fieldInjectors != null && fieldInjectors.size() > 0) {
                    fieldInjectors.stream()
                                  .filter(injector -> injector.getAnnotation() != null)
                                  .forEach(injector -> injector.inject(object, context));
                }
                return new SingletonObjectFactory<>(descriptor, object).getInstance(context);
            } else {
                ProviderDescriptor descriptor = new ProviderDescriptorImpl(resourceField.getType());
                return new PerRequestObjectFactory<>(descriptor).getInstance(context);
            }

        } catch (IllegalArgumentException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e.getLocalizedMessage(), e);
        } catch (IllegalAccessException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    /** @see org.everrest.core.ObjectFactory#getObjectModel() */
    @Override
    public T getObjectModel() {
        return model;
    }

}
