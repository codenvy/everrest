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
package org.everrest.websockets.client;

/**
 * Implementation of this interface passed to WSClient. After open connection to remote server listener notified about
 * incoming messages from server.
 *
 * @author andrew00x
 */
public interface ClientMessageListener {
    /**
     * Notify listener about test message.
     *
     * @param data
     *         text message
     */
    void onMessage(String data);

    /**
     * Notify listener about binary message.
     *
     * @param data
     *         binary message
     */
    void onMessage(byte[] data);

    /**
     * Notify about pong message.
     *
     * @param data
     *         pong message data
     */
    void onPong(byte[] data);

    /**
     * Notify listener about connection open and WSClient is ready to use.
     *
     * @param client
     *         websocket client
     */
    void onOpen(WSClient client);

    /**
     * Notify listener that connection closed.
     *
     * @param status
     *         connection closed status or <code>0</code> if unknown
     * @param message
     *         optional message MAY be passed if connection closed abnormally.This is never message which sent by server when
     *         it closed connection.  This message generated on client side when some error is occurred and client decide to
     *         close connection
     */
    void onClose(int status, String message);
}
