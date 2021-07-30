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
package org.everrest.core.impl.provider.multipart;

import static com.google.common.collect.Lists.newArrayList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.everrest.core.impl.provider.multipart.OutputItem.anOutputItem;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MultipartFormDataWriterTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private MultipartFormDataWriter multipartFormDataWriter;
  private Providers providers;

  @Before
  public void setUp() throws Exception {
    providers = mock(Providers.class);
    multipartFormDataWriter = new MultipartFormDataWriter(providers);
  }

  @Test
  public void failsWhenMessageBodyWriterForItemEntityIsNotAvailable() throws Exception {
    ByteArrayOutputStream bOut = new ByteArrayOutputStream();
    OutputItem item1 =
        anOutputItem()
            .withName("item1")
            .withEntity("item1 entity")
            .withMediaType(TEXT_PLAIN_TYPE)
            .withFilename("item1.txt")
            .build();

    thrown.expect(RuntimeException.class);
    multipartFormDataWriter.writeItems(newArrayList(item1), bOut, "1234567".getBytes());
  }

  @Test
  public void writesItemsOfMultipartContentToOutputStream() throws Exception {
    ByteArrayOutputStream bOut = new ByteArrayOutputStream();
    MessageBodyWriter<String> messageBodyWriter = mockStringMessageBodyWriter(bOut);
    when(providers.getMessageBodyWriter(
            eq(String.class), any(), any(Annotation[].class), any(MediaType.class)))
        .thenReturn(messageBodyWriter);

    OutputItem item1 =
        anOutputItem()
            .withName("item1")
            .withEntity("item1 entity")
            .withMediaType(TEXT_PLAIN_TYPE)
            .withFilename("item1.txt")
            .build();
    OutputItem item2 =
        anOutputItem()
            .withName("item2")
            .withEntity("{\"item2\":\"entity\"}")
            .withMediaType(APPLICATION_JSON_TYPE)
            .withFilename("item2.json")
            .build();
    multipartFormDataWriter.writeItems(newArrayList(item1, item2), bOut, "1234567".getBytes());

    String expectedContent =
        "--1234567\r\n"
            + "Content-Disposition: form-data; name=\"item1\"; filename=\"item1.txt\"\r\n"
            + "Content-Type: text/plain\r\n"
            + "\r\n"
            + "item1 entity\r\n"
            + "--1234567\r\n"
            + "Content-Disposition: form-data; name=\"item2\"; filename=\"item2.json\"\r\n"
            + "Content-Type: application/json\r\n"
            + "\r\n"
            + "{\"item2\":\"entity\"}\r\n"
            + "--1234567--\r\n";
    assertEquals(expectedContent, bOut.toString());
  }

  private MessageBodyWriter<String> mockStringMessageBodyWriter(OutputStream out) throws Exception {
    @SuppressWarnings({"unchecked"})
    MessageBodyWriter<String> messageBodyWriter = mock(MessageBodyWriter.class);
    doAnswer(
            invocation -> {
              Object[] arguments = invocation.getArguments();
              out.write(((String) arguments[0]).getBytes());
              return null;
            })
        .when(messageBodyWriter)
        .writeTo(
            anyObject(),
            eq(String.class),
            any(),
            any(Annotation[].class),
            any(MediaType.class),
            any(),
            eq(out));
    return messageBodyWriter;
  }
}
