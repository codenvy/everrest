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
package org.everrest.core.impl.provider.json;

/**
 * Should be thrown if any error occurs during JSON <-> Java Object transformation.
 *
 * @author andrew00x
 */
@SuppressWarnings("serial")
public class JsonException extends Exception {
    /**
     * @param message
     *         the message.
     * @param cause
     *         the cause.
     */
    public JsonException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     *         the cause.
     */
    public JsonException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     *         the message.
     */
    public JsonException(String message) {
        super(message);
    }
}
