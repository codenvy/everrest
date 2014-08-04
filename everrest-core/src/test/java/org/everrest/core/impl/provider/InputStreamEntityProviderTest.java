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

import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.ContainerRequest;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.tools.EmptyInputStream;
import org.everrest.core.tools.SimpleSecurityContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;

/**
 * @author andrew00x
 */
public class InputStreamEntityProviderTest {
    @Before
    public void setUp() throws Exception {
        ApplicationContextImpl.setCurrent(new ApplicationContextImpl(
                new ContainerRequest("", URI.create(""), URI.create(""), new EmptyInputStream(), new MultivaluedMapImpl(),
                                     new SimpleSecurityContext(false)), null, ProviderBinder.getInstance()));
    }

    @After
    public void tearDown() throws Exception {
        ApplicationContextImpl.setCurrent(null);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testRead() throws Exception {
        MessageBodyReader reader = new InputStreamEntityProvider();
        Assert.assertTrue(reader.isReadable(InputStream.class, null, null, null));
        byte[] data = new byte[16];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)i;
        }
        InputStream in = new ByteArrayInputStream(data);
        InputStream result = (InputStream)reader.readFrom(InputStream.class, null, null, null, null, in);
        byte[] data2 = new byte[16];
        result.read(data2);
        Assert.assertTrue(Arrays.equals(data, data2));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testWrite() throws Exception {
        MessageBodyWriter writer = new InputStreamEntityProvider();
        Assert.assertTrue(writer.isWriteable(InputStream.class, null, null, null));
        byte[] data = new byte[16];
        for (int i = data.length - 1; i >= 0; i--) {
            data[i] = (byte)i;
        }
        InputStream source = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeTo(source, InputStream.class, null, null, null, null, out);
        Assert.assertTrue(Arrays.equals(data, out.toByteArray()));
    }
}
