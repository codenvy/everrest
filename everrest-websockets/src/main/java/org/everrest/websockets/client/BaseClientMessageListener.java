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
package org.everrest.websockets.client;

/**
 * Base implementation of ClientMessageListener. Implemented methods do nothing. Extend this class instead implement
 * ClientMessageListener if need just few of methods declared in ClientMessageListener.
 *
 * @author andrew00x
 */
public class BaseClientMessageListener implements ClientMessageListener {
    @Override
    public void onMessage(String data) {
    }

    @Override
    public void onMessage(byte[] data) {
    }

    @Override
    public void onPong(byte[] data) {
    }

    @Override
    public void onOpen(WSClient client) {
    }

    @Override
    public void onClose(int status, String message) {
    }
}
