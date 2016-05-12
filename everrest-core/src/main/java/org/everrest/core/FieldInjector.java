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
package org.everrest.core;

/**
 * Object field. Useful for initialization object field if type is used in* per-request mode.
 *
 * @author andrew00x
 */
public interface FieldInjector extends Parameter {

    /** @return field name */
    String getName();

    /**
     * Set Object {@link java.lang.reflect.Field} using ApplicationContext for resolve actual field value.
     *
     * @param resource
     *         root resource or provider
     * @param context
     *         ApplicationContext
     */
    void inject(Object resource, ApplicationContext context);

}
