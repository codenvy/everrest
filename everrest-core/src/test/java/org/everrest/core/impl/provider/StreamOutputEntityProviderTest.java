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
package org.everrest.core.impl.provider;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.StreamingOutput;
import org.junit.Before;
import org.junit.Test;

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

    doAnswer(
            invocation -> {
              out.write(streamingOutputContent);
              return null;
            })
        .when(streamingOutput)
        .write(out);

    streamOutputEntityProvider.writeTo(
        streamingOutput, String.class, null, null, null, new MultivaluedHashMap<>(), out);

    assertArrayEquals(streamingOutputContent, out.toByteArray());
  }
}
