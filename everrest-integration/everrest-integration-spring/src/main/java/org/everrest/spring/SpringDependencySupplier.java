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

import org.everrest.core.BaseDependencySupplier;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * Implementation of DependencySupplier that obtain dependencies from Spring IoC
 * container.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: SpringDependencySupplier.java 88 2010-11-11 11:22:12Z andrew00x
 *          $
 */
public final class SpringDependencySupplier extends BaseDependencySupplier implements BeanFactoryAware {
    private BeanFactory beanFactory;

    @Override
    public Object getComponent(Class<?> type) {
        try {
            return beanFactory.getBean(type);
        } catch (NoSuchBeanDefinitionException be) {
            return null;
        }
    }

    @Override
    public Object getComponentByName(String name) {
        try {
            return beanFactory.getBean(name);
        } catch (NoSuchBeanDefinitionException be) {
            return null;
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
