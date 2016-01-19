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
package org.everrest.core;


import java.io.IOException;

/**
 * Contract of this component is process all requests, initialization and
 * control main components of JAX-RS implementation.
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
    void handleRequest(GenericContainerRequest request, GenericContainerResponse response) throws UnhandledException, IOException;
}
