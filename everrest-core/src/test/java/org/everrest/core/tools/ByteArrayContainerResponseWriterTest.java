/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
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