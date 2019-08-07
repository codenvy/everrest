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
package org.everrest.core.impl.provider;

import com.google.common.io.ByteStreams;

import org.everrest.core.ApplicationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InputStreamEntityProviderTest {
    private byte[] testContent;
    private InputStreamEntityProvider inputStreamEntityProvider = new InputStreamEntityProvider();

    @Before
    public void setUp() throws Exception {
        testContent = new byte[16];
        new Random().nextBytes(testContent);
        ApplicationContext context = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
        when(context.getEverrestConfiguration().getMaxBufferSize()).thenReturn(100);
        ApplicationContext.setCurrent(context);
    }

    @After
    public void tearDown() throws Exception {
        ApplicationContext.setCurrent(null);
    }

    @Test
    public void isReadableForInputStream() throws Exception {
        assertTrue(inputStreamEntityProvider.isReadable(InputStream.class, null, null, null));
    }

    @Test
    public void isNotReadableForTypeOtherThanInputStream() throws Exception {
        assertFalse(inputStreamEntityProvider.isReadable(String.class, null, null, null));
    }

    @Test
    public void isWritableForInputStream() throws Exception {
        assertTrue(inputStreamEntityProvider.isWriteable(InputStream.class, null, null, null));
    }

    @Test
    public void isNotWritableForTypeOtherThanInputStream() throws Exception {
        assertFalse(inputStreamEntityProvider.isWriteable(String.class, null, null, null));
    }

    @Test
    public void readsContentOfEntityStreamAsInputStream() throws Exception {
        InputStream in = new ByteArrayInputStream(testContent);
        InputStream result = inputStreamEntityProvider.readFrom(InputStream.class, null, null, null, null, in);
        assertArrayEquals(testContent, ByteStreams.toByteArray(result));
    }

    @Test
    public void writesInputStreamToOutputStream() throws Exception {
        InputStream source = new ByteArrayInputStream(testContent);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        inputStreamEntityProvider.writeTo(source, InputStream.class, null, null, null, null, out);
        assertArrayEquals(testContent, out.toByteArray());
    }
}
