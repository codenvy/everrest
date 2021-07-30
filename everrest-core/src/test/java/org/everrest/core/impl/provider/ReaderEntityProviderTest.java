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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.StringReader;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import org.everrest.core.ApplicationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ReaderEntityProviderTest {
  private static final String TEST_CONTENT = "\u041f\u0440\u0438\u0432\u0456\u0442";

  private ReaderEntityProvider readerEntityProvider;

  @Before
  public void setUp() throws Exception {
    readerEntityProvider = new ReaderEntityProvider();
    ApplicationContext context = mock(ApplicationContext.class);
    when(context.isAsynchronous()).thenReturn(false);
    ApplicationContext.setCurrent(context);
  }

  @After
  public void tearDown() throws Exception {
    ApplicationContext.setCurrent(null);
  }

  @Test
  public void isReadableForReader() throws Exception {
    assertTrue(readerEntityProvider.isReadable(Reader.class, null, null, null));
  }

  @Test
  public void isNotReadableForTypeOtherThanReader() throws Exception {
    assertFalse(readerEntityProvider.isReadable(Object.class, null, null, null));
  }

  @Test
  public void isWritableForReader() throws Exception {
    assertTrue(readerEntityProvider.isWriteable(Reader.class, null, null, null));
  }

  @Test
  public void isNotWritableForTypeOtherThanReader() throws Exception {
    assertFalse(readerEntityProvider.isWriteable(Object.class, null, null, null));
  }

  @Test
  public void readsContentOfEntityStreamAsReader() throws Exception {
    MediaType mediaType =
        new MediaType("text", "plain", ImmutableMap.of("charset", "windows-1251"));
    byte[] data = TEST_CONTENT.getBytes("windows-1251");
    Reader result =
        readerEntityProvider.readFrom(
            Reader.class, String.class, null, mediaType, null, new ByteArrayInputStream(data));

    assertEquals(TEST_CONTENT, CharStreams.toString(result));
  }

  @Test
  public void readsContentOfEntityStreamAsUtf8Reader() throws Exception {
    MediaType mediaType = new MediaType("text", "plain");
    byte[] data = TEST_CONTENT.getBytes("UTF-8");
    Reader result =
        readerEntityProvider.readFrom(
            Reader.class, String.class, null, mediaType, null, new ByteArrayInputStream(data));

    assertEquals(TEST_CONTENT, CharStreams.toString(result));
  }

  @Test
  public void writesReaderToOutputStream() throws Exception {
    MediaType mediaType =
        new MediaType("text", "plain", ImmutableMap.of("charset", "windows-1251"));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    readerEntityProvider.writeTo(
        new StringReader(TEST_CONTENT),
        String.class,
        null,
        null,
        mediaType,
        new MultivaluedHashMap<>(),
        out);

    assertArrayEquals(TEST_CONTENT.getBytes("windows-1251"), out.toByteArray());
  }

  @Test
  public void writesUtf8ReaderToOutputStream() throws Exception {
    MediaType mediaType = new MediaType("text", "plain");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    readerEntityProvider.writeTo(
        new StringReader(TEST_CONTENT),
        String.class,
        null,
        null,
        mediaType,
        new MultivaluedHashMap<>(),
        out);

    assertArrayEquals(TEST_CONTENT.getBytes("UTF-8"), out.toByteArray());
  }
}
