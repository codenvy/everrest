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
package org.everrest.core.impl;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Should not be used by custom services. They have to use
 * {@link javax.ws.rs.WebApplicationException} instead. This Exception is used
 * as wrapper for exception that may occur during request processing.
 *
 * @author andrew00x
 */
@SuppressWarnings("serial")
public final class InternalException extends RuntimeException {
    /**
     * @param message
     *         message
     * @param cause
     *         cause
     */
    public InternalException(String message, Throwable cause) {
        super(message, checkNotNull(cause));
    }

    /**
     * @param cause
     *         cause
     */
    public InternalException(Throwable cause) {
        super(checkNotNull(cause));
    }
}
