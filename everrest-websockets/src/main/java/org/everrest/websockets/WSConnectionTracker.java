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

import static javax.websocket.CloseReason.CloseCodes.NORMAL_CLOSURE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.IOException;

/**
 * Close web socket connections when HTTP session to which these connections associated is going to be invalidated.
 *
 * @author andrew00x
 */
public final class WSConnectionTracker implements HttpSessionListener {
    private static final Logger LOG = LoggerFactory.getLogger(WSConnectionTracker.class);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        final String destroyedSessionId = se.getSession().getId();
        for (WSConnectionImpl wsConnection : WSConnectionContext.connections.values()) {
            final HttpSession httpSession = wsConnection.getHttpSession();
            if (httpSession != null && destroyedSessionId.equals(httpSession.getId())) {
                try {
                    wsConnection.close(NORMAL_CLOSURE.getCode(), "Http session destroyed");
                } catch (IOException e) {
                    LOG.warn(String.format("Error occurs while try to close web-socket connection %s. %s", wsConnection, e.getMessage()),
                             e);
                }
            }
        }
    }
}
