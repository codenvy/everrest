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

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Abstraction of constructor descriptor. Used for create object instance when type is used in per-request lifecycle.
 *
 * @author andrew00x
 */
public interface ConstructorDescriptor {
    /**
     * @param context
     *         ApplicationContext
     * @return newly created instance of the constructor's
     * @see ApplicationContext
     */
    Object createInstance(ApplicationContext context);

    /**
     * Get source constructor.
     *
     * @return constructor
     * @see Constructor
     */
    Constructor<?> getConstructor();

    /** @return constructor's parameters */
    List<Parameter> getParameters();
}
