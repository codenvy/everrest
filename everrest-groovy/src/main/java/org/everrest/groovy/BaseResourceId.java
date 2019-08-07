/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
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

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass() == obj.getClass() && id.equals(((BaseResourceId)obj).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String toString() {
        return getClass().getSimpleName() + '(' + id + ')';
    }
}
