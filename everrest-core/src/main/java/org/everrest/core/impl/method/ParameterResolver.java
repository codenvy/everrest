/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors:
 * Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.method;

import org.everrest.core.ApplicationContext;

import java.lang.annotation.Annotation;

/**
 * Create object that might be injected in JAX-RS component.
 * @param <T>
 *         on of JAX-RS annotation that used for method, constructor parameters or fields
 * @author andrew00x
 */
public interface ParameterResolver<T extends Annotation> {
    /**
     * Creates object which will be passed in resource method or locator.
     *
     * @param parameter
     *         See {@link org.everrest.core.Parameter}
     * @param context
     *         See {@link ApplicationContext}
     * @return newly created instance of class {@link org.everrest.core.Parameter#getParameterClass()}
     * @throws Exception
     *         if any errors occurs
     */
    Object resolve(org.everrest.core.Parameter parameter, ApplicationContext context) throws Exception;
}
