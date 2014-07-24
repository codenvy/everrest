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

import org.everrest.core.impl.BaseTest;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class InputStreamEntityProviderTest extends BaseTest {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setContext();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testRead() throws Exception {
        MessageBodyReader reader = providers.getMessageBodyReader(InputStream.class, null, null, null);
        assertNotNull(reader);
        assertTrue(reader.isReadable(InputStream.class, null, null, null));
        byte[] data = new byte[16];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte)i;
        InputStream in = new ByteArrayInputStream(data);
        InputStream result = (InputStream)reader.readFrom(InputStream.class, null, null, null, null, in);
        byte[] data2 = new byte[16];
        result.read(data2);
        assertTrue(Arrays.equals(data, data2));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testWrite() throws Exception {
        MessageBodyWriter writer = providers.getMessageBodyWriter(InputStream.class, null, null, null);
        assertNotNull(writer);
        assertTrue(writer.isWriteable(InputStream.class, null, null, null));
        byte[] data = new byte[16];
        for (int i = data.length - 1; i >= 0; i--)
            data[i] = (byte)i;
        InputStream source = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeTo(source, InputStream.class, null, null, null, null, out);
        assertTrue(Arrays.equals(data, out.toByteArray()));
    }

}
