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
package org.everrest.core.impl.provider;

import org.everrest.core.ExtHttpHeaders;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Transforms {@link java.lang.Exception} to JAX-RS response.
 */
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        String message = exception.getMessage();
        if (message == null) {
            message = exception.getClass().getName();
        }
        return Response
                .status(500)
                .entity(message)
                .type(MediaType.TEXT_PLAIN)
                .header(ExtHttpHeaders.JAXRS_BODY_PROVIDED, "Error-Message")
                .build();
    }
}
