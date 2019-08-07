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
package org.everrest.core;

/**
 * Throws if root resource can't be published, e.g. resource can't be registered
 * because to conflict of root path
 *
 * @author andrew00x
 */
@SuppressWarnings("serial")
public class ResourcePublicationException extends RuntimeException {
    public ResourcePublicationException(String message) {
        super(message);
    }

    public ResourcePublicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
