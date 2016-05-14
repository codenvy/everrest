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
package org.everrest.core.impl.provider.multipart;

import org.apache.commons.fileupload.FileItem;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;
import static org.everrest.core.util.ParameterizedTypeImpl.newParameterizedType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ListMultipartFormDataMessageBodyReaderTest {
    private Providers                              providers;
    private ListMultipartFormDataMessageBodyReader listMultipartFormDataMessageBodyReader;

    @Before
    public void setUp() throws Exception {
        providers = mock(Providers.class);
        listMultipartFormDataMessageBodyReader = new ListMultipartFormDataMessageBodyReader(providers);
    }

    @Test
    public void isReadableForListOfInputItems() throws Exception {
        Class<List> type = List.class;
        ParameterizedType genericType = newParameterizedType(List.class, InputItem.class);

        assertTrue(listMultipartFormDataMessageBodyReader.isReadable(type, genericType, new Annotation[0], null));
    }

    @Test
    public void isNotReadableForListOfOtherTypeThanInputItem() throws Exception {
        Class<List> type = List.class;
        ParameterizedType genericType = newParameterizedType(List.class, String.class);

        assertFalse(listMultipartFormDataMessageBodyReader.isReadable(type, genericType, new Annotation[0], null));
    }

    @Test
    public void isNotReadableForOtherCollectionOfInputItems() throws Exception {
        Class<Collection> type = Collection.class;
        ParameterizedType genericType = newParameterizedType(Collection.class, InputItem.class);

        assertFalse(listMultipartFormDataMessageBodyReader.isReadable(type, genericType, new Annotation[0], null));
    }

    @Test
    public void isNotReadableWhenGenericTypeIsNotAvailable() throws Exception {
        Class<List> type = List.class;

        assertFalse(listMultipartFormDataMessageBodyReader.isReadable(type, null, new Annotation[0], null));
    }

    @Test
    public void readsListOfInputItems() throws Exception {
        Class type = List.class;
        ParameterizedType genericType = newParameterizedType(List.class, InputItem.class);
        Annotation[] annotations = new Annotation[0];
        MediaType mediaType = MULTIPART_FORM_DATA_TYPE;
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        InputStream in = mock(InputStream.class);

        FileItem fileItem = createFileItem("fileItem1");
        MessageBodyReader fileItemReader = createFileItemMessageBodyReader(annotations, mediaType, headers, in, fileItem);
        when(providers.getMessageBodyReader(eq(Iterator.class), eq(newParameterizedType(Iterator.class, FileItem.class)), aryEq(annotations), eq(mediaType)))
                .thenReturn(fileItemReader);

        List<InputItem> inputItems = listMultipartFormDataMessageBodyReader.readFrom(type, genericType, annotations, mediaType, headers, in);
        assertEquals(1, inputItems.size());
        assertEquals(fileItem.getFieldName(), inputItems.get(0).getName());
    }

    private FileItem createFileItem(String name) {
        FileItem fileItem = mock(FileItem.class);
        when(fileItem.getFieldName()).thenReturn(name);
        return fileItem;
    }

    @SuppressWarnings("unchecked")
    private MessageBodyReader createFileItemMessageBodyReader(Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> headers, InputStream in,
                                                              FileItem... fileItems) throws Exception {
        MessageBodyReader fileItemReader = mock(MessageBodyReader.class);
        when(fileItemReader.readFrom(eq(Iterator.class), eq(newParameterizedType(Iterator.class, FileItem.class)), aryEq(annotations), eq(mediaType), eq(headers), eq(in)))
                .thenReturn(newArrayList(fileItems).iterator());
        return fileItemReader;
    }
}