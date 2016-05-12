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

import com.google.common.collect.Lists;

import org.apache.commons.fileupload.FileItem;
import org.everrest.core.ApplicationContext;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Iterator;
import java.util.List;

import static org.everrest.core.util.ParameterizedTypeImpl.newParameterizedType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MultipartFormDataEntityProviderTest {

    private HttpServletRequest              httpServletRequest;
    private MultipartFormDataEntityProvider formDataEntityProvider;

    @Before
    public void setUp() throws Exception {
        httpServletRequest = mock(HttpServletRequest.class);

        ApplicationContext context = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
        when(context.getEverrestConfiguration().getMaxBufferSize()).thenReturn(100);
        ApplicationContext.setCurrent(context);

        formDataEntityProvider = new MultipartFormDataEntityProvider(httpServletRequest);
    }

    @Test
    public void isReadableForIteratorOfFileItems() throws Exception {
        Class<Iterator> type = Iterator.class;
        ParameterizedType genericType = newParameterizedType(Iterator.class, FileItem.class);

        assertTrue(formDataEntityProvider.isReadable(type, genericType, new Annotation[0], null));
    }

    @Test
    public void isNotReadableForIteratorOfOtherTypeThanFileItem() throws Exception {
        Class<Iterator> type = Iterator.class;
        ParameterizedType genericType = newParameterizedType(Iterator.class, String.class);

        assertFalse(formDataEntityProvider.isReadable(type, genericType, new Annotation[0], null));
    }

    @Test
    public void isNotReadableWhenGenericTypeIsNotAvailable() throws Exception {
        Class<Iterator> type = Iterator.class;

        assertFalse(formDataEntityProvider.isReadable(type, null, new Annotation[0], null));
    }

    @Test
    public void isNotWritableForIteratorOfFileItems() throws Exception {
        Class<Iterator> type = Iterator.class;
        ParameterizedType genericType = newParameterizedType(Iterator.class, String.class);

        assertFalse(formDataEntityProvider.isWriteable(type, genericType, new Annotation[0], null));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void writeToIsNotSupported() throws Exception {
        formDataEntityProvider.writeTo(null, null, null, null, null, null, null);
    }

    @Test
    public void readsEntityStreamAsIteratorOfFileItems() throws Exception {
        Class type = Iterator.class;
        ParameterizedType genericType = newParameterizedType(Iterator.class, FileItem.class);
        Annotation[] annotations = new Annotation[0];
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        InputStream in = mock(InputStream.class);

        mockHttpServletRequest();

        Iterator<FileItem> fileItems = formDataEntityProvider.readFrom(type, genericType, annotations, null, headers, in);
        List<FileItem> fileItemList = Lists.newArrayList(fileItems);
        assertEquals(1, fileItemList.size());
        assertEquals("text", fileItemList.get(0).getFieldName());
        assertEquals("test.txt", fileItemList.get(0).getName());
        assertEquals("text/plain", fileItemList.get(0).getContentType());
        assertEquals("hello", fileItemList.get(0).getString());
    }

    private HttpServletRequest mockHttpServletRequest() throws Exception {
        String formData =
                "--1234567\r\n"
                + "Content-Disposition: form-data; name=\"text\"; filename=\"test.txt\"\r\n"
                + "Content-Type: text/plain\r\n\r\n"
                + "hello"
                + "\r\n--1234567--\r\n";
        ByteArrayInputStream contentAsStream = new ByteArrayInputStream(formData.getBytes());
        ServletInputStream servletInputStream = createServletInputStream(contentAsStream);
        when(httpServletRequest.getInputStream()).thenReturn(servletInputStream);
        when(httpServletRequest.getContentType()).thenReturn("multipart/form-data;boundary=1234567");
        return httpServletRequest;
    }

    private ServletInputStream createServletInputStream(ByteArrayInputStream in) {
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() throws IOException {
                return in.read();
            }
        };
    }

}