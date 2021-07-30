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
package org.everrest.websockets.message;

import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

/**
 * @author andrew00x
 */
public abstract class BaseTextDecoder<T> implements Decoder.Text<T> {
    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }
}
