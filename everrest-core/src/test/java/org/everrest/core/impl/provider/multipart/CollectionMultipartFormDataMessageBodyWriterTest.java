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
package org.everrest.core.impl.provider.multipart;

import com.google.common.collect.ImmutableMap;

import org.everrest.core.util.ParameterizedTypeImpl;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.everrest.core.impl.provider.multipart.OutputItem.anOutputItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CollectionMultipartFormDataMessageBodyWriterTest {
    private CollectionMultipartFormDataMessageBodyWriter collectionMultipartFormDataMessageBodyWriter;
    private MultipartFormDataWriter                      multipartFormDataWriter;

    @Before
    public void setUp() throws Exception {
        multipartFormDataWriter = mock(MultipartFormDataWriter.class);
        collectionMultipartFormDataMessageBodyWriter = new CollectionMultipartFormDataMessageBodyWriter(multipartFormDataWriter);
    }

    @Test
    public void isWriteableForListOfOutputItems() throws Exception {
        Class<List> type = List.class;
        ParameterizedType genericType = ParameterizedTypeImpl.newParameterizedType(List.class, OutputItem.class);

        assertTrue(collectionMultipartFormDataMessageBodyWriter.isWriteable(type, genericType, new Annotation[0], null));
    }

    @Test
    public void isWriteableForSetOfOutputItems() throws Exception {
        Class<Set> type = Set.class;
        ParameterizedType genericType = ParameterizedTypeImpl.newParameterizedType(Set.class, OutputItem.class);

        assertTrue(collectionMultipartFormDataMessageBodyWriter.isWriteable(type, genericType, new Annotation[0], null));
    }

    @Test
    public void isWriteableForAnyCollectionOfOutputItems() throws Exception {
        Class<Collection> type = Collection.class;
        ParameterizedType genericType = ParameterizedTypeImpl.newParameterizedType(Collection.class, OutputItem.class);

        assertTrue(collectionMultipartFormDataMessageBodyWriter.isWriteable(type, genericType, new Annotation[0], null));
    }

    @Test
    public void isNotWriteableForAnyCollectionOfOtherTypeThanOutputItems() throws Exception {
        Class<Collection> type = Collection.class;
        ParameterizedType genericType = ParameterizedTypeImpl.newParameterizedType(Collection.class, String.class);

        assertFalse(collectionMultipartFormDataMessageBodyWriter.isWriteable(type, genericType, new Annotation[0], null));
    }

    @Test
    public void isNotWriteableWhenGenericTypeIsNotAvailable() throws Exception {
        Class<Collection> type = Collection.class;

        assertFalse(collectionMultipartFormDataMessageBodyWriter.isWriteable(type, null, new Annotation[0], null));
    }

    @Test
    public void sizeOfContentCannotBeDetermined() throws Exception {
        Class<Collection> type = Collection.class;
        ParameterizedType genericType = ParameterizedTypeImpl.newParameterizedType(Collection.class, OutputItem.class);

        assertEquals(-1, collectionMultipartFormDataMessageBodyWriter.getSize(newArrayList(), type, genericType, new Annotation[0], TEXT_PLAIN_TYPE));
    }

    @Test
    public void writesCollectionOfOutputItems() throws Exception {
        OutputItem item1 = anOutputItem().withName("item1").withEntity("item1 entity").withMediaType(TEXT_PLAIN_TYPE).withFilename("item1.txt").build();
        OutputItem item2 = anOutputItem().withName("item2").withEntity("{\"item2\":\"entity\"}").withMediaType(APPLICATION_JSON_TYPE).withFilename("item2.json").build();
        Collection<OutputItem> items = newArrayList(item1, item2);
        Class<Collection> type = Collection.class;
        ParameterizedType genericType = ParameterizedTypeImpl.newParameterizedType(Collection.class, OutputItem.class);
        Annotation[] annotations = new Annotation[0];
        MediaType mediaType = new MediaType("multipart", "form-data", ImmutableMap.of("boundary", "1234567"));
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        OutputStream out = new ByteArrayOutputStream();

        collectionMultipartFormDataMessageBodyWriter.writeTo(items, type, genericType, annotations, mediaType, headers, out);

        verify(multipartFormDataWriter).writeItems(same(items), same(out), aryEq("1234567".getBytes()));

        MediaType mediaTypeWithBoundary = (MediaType)headers.getFirst("Content-type");
        assertNotNull(mediaTypeWithBoundary);
        String boundary = mediaTypeWithBoundary.getParameters().get("boundary");
        assertEquals("1234567", boundary);
    }

    @Test
    public void writesCollectionOfOutputItemsAndGenerateBoundary() throws Exception {
        OutputItem item1 = anOutputItem().withName("item1").withEntity("item1 entity").withMediaType(TEXT_PLAIN_TYPE).withFilename("item1.txt").build();
        OutputItem item2 = anOutputItem().withName("item2").withEntity("{\"item2\":\"entity\"}").withMediaType(APPLICATION_JSON_TYPE).withFilename("item2.json").build();
        Collection<OutputItem> items = newArrayList(item1, item2);
        Class<Collection> type = Collection.class;
        ParameterizedType genericType = ParameterizedTypeImpl.newParameterizedType(Collection.class, OutputItem.class);
        Annotation[] annotations = new Annotation[0];
        MediaType mediaType = MediaType.MULTIPART_FORM_DATA_TYPE;
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        OutputStream out = new ByteArrayOutputStream();

        collectionMultipartFormDataMessageBodyWriter.writeTo(items, type, genericType, annotations, mediaType, headers, out);

        MediaType mediaTypeWithBoundary = (MediaType)headers.getFirst("Content-type");
        assertNotNull(mediaTypeWithBoundary);
        String boundary = mediaTypeWithBoundary.getParameters().get("boundary");
        assertNotNull(boundary);

        verify(multipartFormDataWriter).writeItems(same(items), same(out), aryEq(boundary.getBytes()));
    }
}