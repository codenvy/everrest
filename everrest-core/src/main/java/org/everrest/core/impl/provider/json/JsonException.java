/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
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
