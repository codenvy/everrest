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

/** Call "initialize" and "destroy" methods of object. */
public interface LifecycleMethodStrategy {
    /**
     * Call "initialize" method on the specified object. It is up to the implementation how to find "initialize"
     * method. It is possible to have more than one initialize method but any particular order of methods invocation
     * is not guaranteed.
     *
     * @param o
     *         the object
     * @throws org.everrest.core.impl.InternalException
     *         if initialize method throws any exception
     */
    void invokeInitializeMethods(Object o);

    /**
     * Call "destroy" method on the specified object. It is up to the implementation how to find "destroy" method. It
     * is possible to have more than one destroy method but any particular order of methods invocation is not
     * guaranteed.
     *
     * @param o
     *         the object
     * @throws org.everrest.core.impl.InternalException
     *         if destroy method throws any exception
     */
    void invokeDestroyMethods(Object o);
}
