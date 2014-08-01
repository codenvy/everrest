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
package org.everrest.core.impl.resource;

import javax.ws.rs.Path;

/**
 * Describe the Path annotation, see {@link javax.ws.rs.Path}.
 *
 * @author andrew00x
 */
public class PathValue {
    public static String getPath(Path annotation) {
        return annotation == null ? null : annotation.value();
    }

    /** URI template, see {@link javax.ws.rs.Path#value()} . */
    private final String path;

    /**
     * @param path
     *         URI template
     */
    public PathValue(String path) {
        this.path = path;
    }

    /** @return URI template string */
    public String getPath() {
        return path;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "( " + path + " )";
    }
}
