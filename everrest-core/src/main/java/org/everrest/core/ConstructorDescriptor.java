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
package org.everrest.core;

import org.everrest.core.resource.ResourceDescriptor;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Abstraction of constructor descriptor. Used for create object instance when
 * type is used in per-request lifecycle.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public interface ConstructorDescriptor extends ResourceDescriptor {

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
    List<ConstructorParameter> getParameters();

}
