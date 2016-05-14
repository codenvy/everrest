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
package org.everrest.core.impl.provider;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringEntityProviderTest {
    private static final String TEST_CONTENT = "\u041f\u0440\u0438\u0432\u0456\u0442";

    private StringEntityProvider stringEntityProvider;

    @Before
    public void setUp() throws Exception {
        stringEntityProvider = new StringEntityProvider();
    }

    @Test
    public void isReadableForString() throws Exception {
        assertTrue(stringEntityProvider.isReadable(String.class, null, null, null));
    }

    @Test
    public void isNotReadableForTypeOtherThanString() throws Exception {
        assertFalse(stringEntityProvider.isReadable(Object.class, null, null, null));
    }

    @Test
    public void isWritableForString() throws Exception {
        assertTrue(stringEntityProvider.isWriteable(String.class, null, null, null));
    }

    @Test
    public void isNotWritableForTypeOtherThanString() throws Exception {
        assertFalse(stringEntityProvider.isWriteable(Object.class, null, null, null));
    }

    @Test
    public void readsContentOfEntityStreamAsString() throws Exception {
        MediaType mediaType = new MediaType("text", "plain", ImmutableMap.of("charset", "windows-1251"));
        byte[] data = TEST_CONTENT.getBytes("windows-1251");
        String result = stringEntityProvider.readFrom(String.class, String.class, null, mediaType, null, new ByteArrayInputStream(data));

        assertEquals(TEST_CONTENT, result);
    }

    @Test
    public void readsContentOfEntityStreamAsUtf8String() throws Exception {
        MediaType mediaType = new MediaType("text", "plain");
        byte[] data = TEST_CONTENT.getBytes("UTF-8");
        String result = stringEntityProvider.readFrom(String.class, String.class, null, mediaType, null, new ByteArrayInputStream(data));

        assertEquals(TEST_CONTENT, result);
    }

    @Test
    public void writesStringToOutputStream() throws Exception {
        MediaType mediaType = new MediaType("text", "plain", ImmutableMap.of("charset", "windows-1251"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        stringEntityProvider.writeTo(TEST_CONTENT, String.class, null, null, mediaType, new MultivaluedHashMap<>(), out);

        assertArrayEquals(TEST_CONTENT.getBytes("windows-1251"), out.toByteArray());
    }

    @Test
    public void writesUtf8StringToOutputStream() throws Exception {
        MediaType mediaType = new MediaType("text", "plain");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        stringEntityProvider.writeTo(TEST_CONTENT, String.class, null, null, mediaType, new MultivaluedHashMap<>(), out);

        assertArrayEquals(TEST_CONTENT.getBytes("UTF-8"), out.toByteArray());
    }
}
