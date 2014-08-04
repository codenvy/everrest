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
import org.everrest.core.impl.header.MediaTypeHelper;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * @author andrew00x
 */
public class ByteEntityProviderTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testRead() throws Exception {
        MessageBodyReader reader =  new ByteEntityProvider();
        Assert.assertTrue(reader.isReadable(byte[].class, null, null, null));
        byte[] data = new byte[16];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)i;
        }
        Assert.assertTrue(reader.isReadable(data.getClass(), null, null, null));
        byte[] result =
                (byte[])reader.readFrom(byte[].class, null, null, MediaTypeHelper.DEFAULT_TYPE, new MultivaluedMapImpl(),
                                        new ByteArrayInputStream(data));
        Assert.assertTrue(Arrays.equals(data, result));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testWrite() throws Exception {
        MessageBodyWriter writer = new ByteEntityProvider();
        Assert.assertTrue(writer.isWriteable(byte[].class, null, null, null));
        byte[] data = new byte[16];
        for (int i = data.length - 1; i >= 0; i--) {
            data[i] = (byte)i;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeTo(data, byte[].class, null, null, MediaTypeHelper.DEFAULT_TYPE, new MultivaluedMapImpl(), out);
        Assert.assertTrue(Arrays.equals(data, out.toByteArray()));
    }
}
