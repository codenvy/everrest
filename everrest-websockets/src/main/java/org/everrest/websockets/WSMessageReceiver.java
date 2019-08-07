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
package org.everrest.websockets;

import org.everrest.websockets.message.InputMessage;

/**
 * Receives incoming messages. Implementation of this interface should be added to WSConnection.
 *
 * @see WSConnection#registerMessageReceiver(WSMessageReceiver)
 * @see WSConnection#removeMessageReceiver(WSMessageReceiver)
 */
public interface WSMessageReceiver {
    /**
     * Called when new message received.
     *
     * @param input
     *         input message
     */
    void onMessage(InputMessage input);

    /**
     * Called when error occurs when process incoming message so method {@link #onMessage(org.everrest.websockets.message.InputMessage)}
     * cannot be called.
     *
     * @param error
     *         error
     */
    void onError(Exception error);
}
