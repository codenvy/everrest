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

import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;

/**
 * All implementation of this interface should be able to write data in container response, e. g. servlet response.
 *
 * @author andrew00x
 */
public interface ContainerResponseWriter {

    /**
     * Write HTTP status and headers in HTTP response.
     *
     * @param response
     *         container response
     * @throws IOException
     *         if any i/o error occurs
     */
    void writeHeaders(GenericContainerResponse response) throws IOException;

    /**
     * Write entity body in output stream.
     *
     * @param response
     *         container response
     * @param entityWriter
     *         See {@link MessageBodyWriter}
     * @throws IOException
     *         if any i/o error occurs
     */
    void writeBody(GenericContainerResponse response, MessageBodyWriter entityWriter) throws IOException;
}
