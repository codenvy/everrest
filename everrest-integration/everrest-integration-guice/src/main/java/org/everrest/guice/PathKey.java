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
package org.everrest.guice;

import com.google.inject.Key;

/**
 * Guice key that allows remap URI template of service.
 *
 * @author andrew00x
 */
public final class PathKey<T> extends Key<T> {
    private final String   path;
    private final Class<T> clazz;

    public PathKey(Class<T> clazz, String path) {
        this.clazz = clazz;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public Class<T> getClazz() {
        return clazz;
    }
}
