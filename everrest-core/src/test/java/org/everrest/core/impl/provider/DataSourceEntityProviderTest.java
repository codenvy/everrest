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

import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.io.ByteStreams;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.ws.rs.core.MultivaluedHashMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.everrest.core.ApplicationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DataSourceEntityProviderTest {
  private byte[] testContent = "to be or not to be".getBytes();

  private ApplicationContext context;
  private DataSourceEntityProvider dataSourceEntityProvider;

  @Before
  public void setUp() throws Exception {
    context = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
    when(context.getEverrestConfiguration().getMaxBufferSize()).thenReturn(128);
    ApplicationContext.setCurrent(context);
    dataSourceEntityProvider = new DataSourceEntityProvider();
  }

  @After
  public void tearDown() throws Exception {
    ApplicationContext.setCurrent(null);
  }

  @Test
  public void isReadableForDataSource() throws Exception {
    assertTrue(dataSourceEntityProvider.isReadable(DataSource.class, null, null, null));
  }

  @Test
  public void isNotReadableForTypeOtherThanDataSource() throws Exception {
    assertFalse(dataSourceEntityProvider.isReadable(String.class, null, null, null));
  }

  @Test
  public void isWritableForDataSource() throws Exception {
    assertTrue(dataSourceEntityProvider.isWriteable(DataSource.class, null, null, null));
  }

  @Test
  public void isNotWritableForTypeOtherThanDataSource() throws Exception {
    assertFalse(dataSourceEntityProvider.isWriteable(String.class, null, null, null));
  }

  @Test
  public void readsContentOfEntityStreamAsDataSource() throws Exception {
    DataSource result =
        dataSourceEntityProvider.readFrom(
            DataSource.class,
            null,
            null,
            TEXT_PLAIN_TYPE,
            null,
            new ByteArrayInputStream(testContent));

    assertTrue(result instanceof ByteArrayDataSource);
    byte[] bytes = ByteStreams.toByteArray(result.getInputStream());
    assertArrayEquals(testContent, bytes);
    assertEquals(TEXT_PLAIN, result.getContentType());
  }

  @Test
  public void buffersContentOfEntityStreamToFileAndReadsAsDataSource() throws Exception {
    when(context.getEverrestConfiguration().getMaxBufferSize()).thenReturn(8);

    DataSource result =
        dataSourceEntityProvider.readFrom(
            DataSource.class,
            null,
            null,
            TEXT_PLAIN_TYPE,
            null,
            new ByteArrayInputStream(testContent));
    assertTrue(result instanceof FileDataSource);

    byte[] bytes = ByteStreams.toByteArray(result.getInputStream());
    assertArrayEquals(testContent, bytes);
    assertEquals(TEXT_PLAIN, result.getContentType());
  }

  @Test
  public void writesFileToOutputStream() throws Exception {
    ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(testContent, TEXT_PLAIN);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();

    dataSourceEntityProvider.writeTo(
        byteArrayDataSource, DataSource.class, null, null, null, headers, out);

    assertArrayEquals(testContent, out.toByteArray());
    assertEquals(TEXT_PLAIN, headers.getFirst(CONTENT_TYPE));
  }
}
