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
