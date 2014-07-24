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
package org.everrest.core.tools;

import org.everrest.core.ContainerResponseWriter;
import org.everrest.core.GenericContainerResponse;

import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;

/**
 * Mock object than can be used for any test when we don't care about response
 * entity at all.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: DummyContainerResponseWriter.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
 */
public class DummyContainerResponseWriter implements ContainerResponseWriter {
    /** {@inheritDoc} */
    @SuppressWarnings({"rawtypes"})
    public void writeBody(GenericContainerResponse response, MessageBodyWriter entityWriter) throws IOException {
    }

    /** {@inheritDoc} */
    public void writeHeaders(GenericContainerResponse response) throws IOException {
    }
}
