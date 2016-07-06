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
