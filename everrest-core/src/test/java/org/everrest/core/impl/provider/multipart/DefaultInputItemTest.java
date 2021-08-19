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
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.everrest.core.util.ParameterizedTypeImpl.newParameterizedType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Providers;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.fileupload.FileItem;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DefaultInputItemTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private FileItem fileItem;
  private Providers providers;
  private DefaultInputItem inputItem;

  @Before
  public void setUp() throws Exception {
    fileItem = mock(FileItem.class);
    providers = mock(Providers.class);
    inputItem = new DefaultInputItem(fileItem, providers);
  }

  @Test
  public void getsName() throws Exception {
    when(fileItem.getFieldName()).thenReturn("name1");

    assertEquals("name1", inputItem.getName());
  }

  @Test
  public void getsFilename() throws Exception {
    when(fileItem.getName()).thenReturn("name2");

    assertEquals("name2", inputItem.getFilename());
  }

  @Test
  public void getsMediaType() throws Exception {
    when(fileItem.getContentType()).thenReturn("text/plain");

    assertEquals(TEXT_PLAIN_TYPE, inputItem.getMediaType());
  }

  @Test
  public void getsNullMediaTypeIfContentTypeInFileItemIsNotAvailable() throws Exception {
    assertNull(inputItem.getMediaType());
  }

  @Test
  public void getsHeadersAsEmptyMap() throws Exception {
    assertTrue(inputItem.getHeaders().isEmpty());
  }

  @Test
  public void getsBodyAsStream() throws Exception {
    ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
    when(fileItem.getInputStream()).thenReturn(in);

    assertSame(in, inputItem.getBody());
  }

  @Test
  public void getsBodyAsString() throws Exception {
    when(fileItem.getString()).thenReturn("content");

    assertEquals("content", inputItem.getBodyAsString());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getsBodyAsCustomType() throws Exception {
    Class<List> type = List.class;
    ParameterizedType genericType = newParameterizedType(List.class, String.class);
    Annotation[] annotations = new Annotation[0];
    InputStream in = mock(InputStream.class);

    when(fileItem.getInputStream()).thenReturn(in);
    when(fileItem.getContentType()).thenReturn(TEXT_PLAIN_TYPE.toString());

    MessageBodyReader reader = mock(MessageBodyReader.class);
    ArrayList<String> bodyAsList = newArrayList("to", "be", "or", "not", "to", "be");
    when(reader.readFrom(
            eq(type), eq(genericType), aryEq(annotations), eq(TEXT_PLAIN_TYPE), any(), eq(in)))
        .thenReturn(bodyAsList);
    when(providers.getMessageBodyReader(
            eq(type), eq(genericType), aryEq(annotations), eq(TEXT_PLAIN_TYPE)))
        .thenReturn(reader);

    assertEquals(bodyAsList, inputItem.getBody(type, genericType));
  }

  @Test
  public void failsGetBodyAsCustomTypeWhenMessageBodyReaderForThisTypeIsNotAvailable()
      throws Exception {
    Class<List> type = List.class;
    ParameterizedType genericType = newParameterizedType(List.class, String.class);
    InputStream in = mock(InputStream.class);

    when(fileItem.getInputStream()).thenReturn(in);
    when(fileItem.getContentType()).thenReturn(TEXT_PLAIN_TYPE.toString());

    thrown.expect(RuntimeException.class);
    inputItem.getBody(type, genericType);
  }
}
