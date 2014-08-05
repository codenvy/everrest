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
 * Implementation of DependencySupplier should be able to provide objects that
 * required for constructors or fields of Resource or Provider.
 *
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id$
 */
public interface DependencySupplier {
    /**
     * Get object that is approach do description <code>parameter</code>.
     *
     * @param parameter
     *         required parameter description
     * @return object of required type or null if instance described by
     * <code>parameter</code> may not be produced
     * @throws RuntimeException
     *         if any error occurs while creating instance
     *         described by <code>parameter</code>
     * @see Parameter#getParameterClass()
     * @see Parameter#getGenericType()
     */
    Object getComponent(Parameter parameter);

    /**
     * Get instance of <code>type</code>.
     *
     * @param type
     *         required parameter class
     * @return object of required type or null if instance described by
     * <code>type</code> may not be produced
     * @throws RuntimeException
     *         if any error occurs while creating instance
     *         of <code>type</code>
     */
    Object getComponent(Class<?> type);
}
