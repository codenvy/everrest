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
 * Implementation of this interface should be able provide object instance
 * dependent of component lifecycle.
 *
 * @param <T>
 *         ObjectModel extensions
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 * @see ObjectModel
 */
public interface ObjectFactory<T extends ObjectModel> {

    /**
     * Create object instance. ApplicationContext can be used for getting
     * required parameters for object constructors or fields.
     *
     * @param context
     *         ApplicationContext
     * @return object instance
     */
    Object getInstance(ApplicationContext context);

    /**
     * @return any extension of {@link ObjectModel}. That must allows create
     *         object instance and initialize object's fields for per-request
     *         resources
     */
    T getObjectModel();

}
