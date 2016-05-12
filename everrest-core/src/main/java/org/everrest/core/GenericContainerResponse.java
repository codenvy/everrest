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
package org.everrest.core;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Type;

public interface GenericContainerResponse {

    /**
     * Set response. New response can override old one.
     *
     * @param response
     *         See {@link Response}
     */
    void setResponse(Response response);

    /**
     * Get preset {@link Response}. This method can be useful for modification
     * {@link GenericContainerResponse}. See
     * {@link ResponseFilter#doFilter(GenericContainerResponse)}.
     *
     * @return preset {@link Response} or null if it was not initialized yet.
     */
    Response getResponse();

    /**
     * Write response to output stream.
     *
     * @throws IOException
     *         if any i/o errors occurs
     */
    void writeResponse() throws IOException;

    /** @return HTTP status */
    int getStatus();

    /** @return HTTP headers */
    MultivaluedMap<String, Object> getHttpHeaders();

    /** @return entity body */
    Object getEntity();

    /** @return entity type */
    Type getEntityType();

    /** @return body content type */
    MediaType getContentType();

}
