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

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.junit.Assert.assertEquals;

public class DefaultExceptionMapperTest {

    private DefaultExceptionMapper exceptionMapper;

    @Before
    public void setUp() throws Exception {
        exceptionMapper = new DefaultExceptionMapper();
    }

    @Test
    public void createsResponseWithStatus500AndUsesExceptionMessageAsEntity() {
        Exception exception = new Exception("<error message>");

        Response response = exceptionMapper.toResponse(exception);

        assertEquals(500, response.getStatus());
        assertEquals(TEXT_PLAIN_TYPE, response.getMediaType());
        assertEquals(exception.getMessage(), response.getEntity());
    }

    @Test
    public void createsResponseWithStatus500AndUsesFullClassNameOfExceptionAsEntity() {
        Exception exception = new Exception();

        Response response = exceptionMapper.toResponse(exception);

        assertEquals(500, response.getStatus());
        assertEquals(TEXT_PLAIN_TYPE, response.getMediaType());
        assertEquals(exception.getClass().getName(), response.getEntity());
    }
}