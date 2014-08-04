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
package org.everrest.spring;

import org.everrest.core.ApplicationContext;
import org.everrest.core.FieldInjector;
import org.everrest.core.ObjectFactory;
import org.everrest.core.ObjectModel;
import org.springframework.beans.factory.BeanFactory;

import java.util.List;

/**
 * Spring container object factory obtains instance of bean form Spring container by <code>name</code>.
 *
 * @author andrew00x
 */
public class SpringObjectFactory<T extends ObjectModel> implements ObjectFactory<T> {
    protected final BeanFactory beanFactory;

    protected final String name;

    protected final T model;

    public SpringObjectFactory(T model, String name, BeanFactory beanFactory) {
        this.model = model;
        this.name = name;
        this.beanFactory = beanFactory;
    }

    /** {@inheritDoc} */
    public Object getInstance(ApplicationContext context) {
        Object bean = beanFactory.getBean(name);
        List<FieldInjector> fieldInjectors = model.getFieldInjectors();
        if (fieldInjectors != null && fieldInjectors.size() > 0) {
            for (FieldInjector injector : fieldInjectors) {
                if (injector.getAnnotation() != null) {
                    injector.inject(bean, context);
                }
            }
        }
        return bean;
    }

    /** {@inheritDoc} */
    public T getObjectModel() {
        return model;
    }

}
