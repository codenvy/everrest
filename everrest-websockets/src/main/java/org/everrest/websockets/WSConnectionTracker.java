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
package org.everrest.websockets;

import org.apache.catalina.websocket.Constants;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Close web socket connections when HTTP session to which these connections associated is going to be invalidated.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public final class WSConnectionTracker implements HttpSessionListener {
    @Override
    public void sessionCreated(HttpSessionEvent se) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        WSConnectionContext.closeAll(se.getSession().getId(), Constants.STATUS_CLOSE_NORMAL, "Http session destroyed");
    }
}
