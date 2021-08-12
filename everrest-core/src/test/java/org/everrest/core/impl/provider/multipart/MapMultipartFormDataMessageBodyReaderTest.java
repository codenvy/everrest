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
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;
import static org.everrest.core.util.ParameterizedTypeImpl.newParameterizedType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Providers;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.fileupload.FileItem;
import org.junit.Before;
import org.junit.Test;

public class MapMultipartFormDataMessageBodyReaderTest {
  private Providers providers;
  private MapMultipartFormDataMessageBodyReader mapMultipartFormDataMessageBodyReader;

  @Before
  public void setUp() throws Exception {
    providers = mock(Providers.class);
    mapMultipartFormDataMessageBodyReader = new MapMultipartFormDataMessageBodyReader(providers);
  }

  @Test
  public void isReadableForMapStringToInputItems() throws Exception {
    Class<Map> type = Map.class;
    ParameterizedType genericType = newParameterizedType(Map.class, String.class, InputItem.class);

    assertTrue(
        mapMultipartFormDataMessageBodyReader.isReadable(
            type, genericType, new Annotation[0], null));
  }

  @Test
  public void isNotReadableForMapStringToOtherTypeThanInputItems() throws Exception {
    Class<Map> type = Map.class;
    ParameterizedType genericType = newParameterizedType(Map.class, String.class, String.class);

    assertFalse(
        mapMultipartFormDataMessageBodyReader.isReadable(
            type, genericType, new Annotation[0], null));
  }

  @Test
  public void isNotReadableForMapOtherTypeThanStringToInputItems() throws Exception {
    Class<Map> type = Map.class;
    ParameterizedType genericType = newParameterizedType(Map.class, Object.class, InputItem.class);

    assertFalse(
        mapMultipartFormDataMessageBodyReader.isReadable(
            type, genericType, new Annotation[0], null));
  }

  @Test
  public void isNotReadableWhenGenericTypeIsNotAvailable() throws Exception {
    Class<Map> type = Map.class;

    assertFalse(
        mapMultipartFormDataMessageBodyReader.isReadable(type, null, new Annotation[0], null));
  }

  @Test
  public void readsMapOfInputItems() throws Exception {
    Class type = Map.class;
    ParameterizedType genericType = newParameterizedType(Map.class, String.class, InputItem.class);
    Annotation[] annotations = new Annotation[0];
    MediaType mediaType = MULTIPART_FORM_DATA_TYPE;
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    InputStream in = mock(InputStream.class);

    FileItem fileItem = createFileItem("fileItem1");
    MessageBodyReader fileItemReader =
        createFileItemMessageBodyReader(annotations, mediaType, headers, in, fileItem);
    when(providers.getMessageBodyReader(
            eq(Iterator.class),
            eq(newParameterizedType(Iterator.class, FileItem.class)),
            aryEq(annotations),
            eq(mediaType)))
        .thenReturn(fileItemReader);

    Map<String, InputItem> inputItems =
        mapMultipartFormDataMessageBodyReader.readFrom(
            type, genericType, annotations, mediaType, headers, in);
    assertEquals(1, inputItems.size());
    assertEquals(fileItem.getFieldName(), inputItems.get(fileItem.getFieldName()).getName());
  }

  private FileItem createFileItem(String name) {
    FileItem fileItem = mock(FileItem.class);
    when(fileItem.getFieldName()).thenReturn(name);
    return fileItem;
  }

  private MessageBodyReader createFileItemMessageBodyReader(
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, String> headers,
      InputStream in,
      FileItem... fileItems)
      throws Exception {
    MessageBodyReader fileItemReader = mock(MessageBodyReader.class);
    when(fileItemReader.readFrom(
            eq(Iterator.class),
            eq(newParameterizedType(Iterator.class, FileItem.class)),
            aryEq(annotations),
            eq(mediaType),
            eq(headers),
            eq(in)))
        .thenReturn(newArrayList(fileItems).iterator());
    return fileItemReader;
  }
}
