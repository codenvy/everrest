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
package org.everrest.websockets.message;

/**
 * Base class for input and output messages.
 *
 * @author andrew00x
 */
public abstract class Message {
    private String uuid;
    private String body;

    /**
     * Get message UUID. If specified for input message then output message gets the same UUID.
     *
     * @return message unique identifier.
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Set message UUID. If specified fro input message then output message gets the same UUID.
     *
     * @param uuid
     *         message unique identifier.
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Get message body.
     *
     * @return message body
     */
    public String getBody() {
        return body;
    }

    /**
     * Set message body.
     *
     * @param body
     *         message body
     */
    public void setBody(String body) {
        this.body = body;
    }
}
