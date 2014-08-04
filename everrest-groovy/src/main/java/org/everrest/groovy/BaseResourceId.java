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
package org.everrest.groovy;

/**
 * Base implementation of ResourceId.
 *
 * @author andrew00x
 */
public class BaseResourceId implements ResourceId {
    private final String id;

    public BaseResourceId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Id may not be null. ");
        }
        this.id = id;
    }

    /** {@inheritDoc} */
    public String getId() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass() == obj.getClass() && id.equals(((BaseResourceId)obj).id);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /** {@inheritDoc} */
    public String toString() {
        return getClass().getSimpleName() + '(' + id + ')';
    }
}
