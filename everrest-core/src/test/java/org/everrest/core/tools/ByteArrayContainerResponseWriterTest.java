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
package org.everrest.core.tools;

import org.everrest.core.GenericContainerResponse;
import org.everrest.core.impl.provider.StringEntityProvider;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ByteArrayContainerResponseWriterTest {

    private ByteArrayContainerResponseWriter responseWriter;
    private GenericContainerResponse         containerResponse;

    @Before
    public void setUp() throws Exception {
        responseWriter = new ByteArrayContainerResponseWriter();
        containerResponse = mock(GenericContainerResponse.class);
    }

    @Test
    public void writesHeadersOfResponse() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle("content-type", "text/plain");
        when(containerResponse.getHttpHeaders()).thenReturn(headers);

        responseWriter.writeHeaders(containerResponse);

        assertEquals(headers, responseWriter.getHeaders());
    }

    @Test
    public void writesBodyOfResponse() throws Exception {
        String entity = "to be or not to be";
        MessageBodyWriter entityWriter = new StringEntityProvider();
        when(containerResponse.getEntity()).thenReturn(entity);

        responseWriter.writeBody(containerResponse, entityWriter);

        assertArrayEquals(entity.getBytes(), responseWriter.getBody());
    }
}