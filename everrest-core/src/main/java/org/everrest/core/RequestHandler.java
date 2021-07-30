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
package org.everrest.core;


import org.everrest.core.impl.ProviderBinder;

import java.io.IOException;

/**
 * Contract of this component is process all requests, initialization and control main components of JAX-RS implementation.
 *
 * @author andrew00x
 */
public interface RequestHandler {
    /**
     * Handle the HTTP request by dispatching request to appropriate resource. If
     * no one appropriate resource found then error response will be produced.
     *
     * @param request
     *         HTTP request
     * @param response
     *         HTTP response
     * @throws java.io.IOException
     *         if any i/o exceptions occurs
     * @throws UnhandledException
     *         if any other errors occurs
     */
    void handleRequest(GenericContainerRequest request, GenericContainerResponse response) throws IOException;

    ResourceBinder getResources();

    ProviderBinder getProviders();
}
