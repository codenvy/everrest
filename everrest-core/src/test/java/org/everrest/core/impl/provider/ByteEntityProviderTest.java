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

import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ByteEntityProviderTest {

    private byte[] testContent;
    private ByteEntityProvider byteEntityProvider;

    @Before
    public void setUp() throws Exception {
        testContent = new byte[16];
        new Random().nextBytes(testContent);

        byteEntityProvider = new ByteEntityProvider();
    }

    @Test
    public void isReadableForByteArray() throws Exception {
        assertTrue(byteEntityProvider.isReadable(byte[].class, null, null, null));
    }

    @Test
    public void isNotReadableForTypeOtherThanByteArray() throws Exception {
        assertFalse(byteEntityProvider.isReadable(String.class, null, null, null));
    }

    @Test
    public void isWritableForByteArray() throws Exception {
        assertTrue(byteEntityProvider.isWriteable(byte[].class, null, null, null));
    }

    @Test
    public void isNotWritableForTypeOtherThanByteArray() throws Exception {
        assertFalse(byteEntityProvider.isWriteable(String.class, null, null, null));
    }

    @Test
    public void readsContentOfEntityStreamAsByteArray() throws Exception {
        byte[] result = byteEntityProvider.readFrom(byte[].class, null, null, MediaType.WILDCARD_TYPE, new MultivaluedMapImpl(),
                                                    new ByteArrayInputStream(testContent));
        assertArrayEquals(testContent, result);
    }

    @Test
    public void writesByteArrayToOutputStream() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byteEntityProvider.writeTo(testContent, byte[].class, null, null, MediaType.WILDCARD_TYPE, new MultivaluedHashMap<>(), out);

        assertArrayEquals(testContent, out.toByteArray());
    }
}
