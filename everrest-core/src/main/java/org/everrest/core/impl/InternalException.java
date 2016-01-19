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
package org.everrest.core.impl;

/**
 * Should not be used by custom services. They have to use
 * {@link javax.ws.rs.WebApplicationException} instead. This Exception is used
 * as wrapper for exception that may occur during request processing.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
@SuppressWarnings("serial")
public final class InternalException extends RuntimeException {
    /**
     * @param s
     *         message
     * @param throwable
     *         cause
     */
    public InternalException(String s, Throwable throwable) {
        super(s, throwable);
    }

    /**
     * @param throwable
     *         cause
     */
    public InternalException(Throwable throwable) {
        super(throwable);
    }
}
