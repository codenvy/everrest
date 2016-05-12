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

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class StreamOutputEntityProviderTest {
    private StreamOutputEntityProvider streamOutputEntityProvider;

    @Before
    public void setUp() throws Exception {
        streamOutputEntityProvider = new StreamOutputEntityProvider();
    }

    @Test
    public void isNotReadableForStreamingOutput() throws Exception {
        assertFalse(streamOutputEntityProvider.isReadable(StreamingOutput.class, null, null, null));
    }

    @Test
    public void isNotReadableForTypeOtherThanStreamingOutput() throws Exception {
        assertFalse(streamOutputEntityProvider.isReadable(StreamingOutput.class, null, null, null));
    }

    @Test
    public void isWritableForStreamingOutput() throws Exception {
        assertTrue(streamOutputEntityProvider.isWriteable(StreamingOutput.class, null, null, null));
    }

    @Test
    public void isNotWritableForTypeOtherThanStreamingOutput() throws Exception {
        assertFalse(streamOutputEntityProvider.isWriteable(Object.class, null, null, null));
    }

    @Test
    public void writesStreamingOutputToOutputStream() throws Exception {
        StreamingOutput streamingOutput = mock(StreamingOutput.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] streamingOutputContent = "streaming output".getBytes();

        doAnswer(invocation -> {
            out.write(streamingOutputContent);
            return null;
        }).when(streamingOutput).write(out);

        streamOutputEntityProvider.writeTo(streamingOutput, String.class, null, null, null, new MultivaluedHashMap<>(), out);

        assertArrayEquals(streamingOutputContent, out.toByteArray());
    }
}