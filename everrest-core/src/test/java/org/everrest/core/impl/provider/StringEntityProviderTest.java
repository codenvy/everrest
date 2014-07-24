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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class StringEntityProviderTest extends BaseTest {
    private static final String TEST_CYR = "\u041f\u0440\u0438\u0432\u0456\u0442";

    private MediaType mediaType;

    public void setUp() throws Exception {
        super.setUp();
        Map<String, String> p = new HashMap<String, String>(1);
        p.put("charset", "windows-1251");
        mediaType = new MediaType("text", "plain", p);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testRead() throws IOException {
        MessageBodyReader reader = providers.getMessageBodyReader(String.class, null, null, mediaType);
        byte[] data = TEST_CYR.getBytes("windows-1251");
        InputStream in = new ByteArrayInputStream(data);
        String res = (String)reader.readFrom(String.class, String.class, null, mediaType, null, in);
        assertTrue(TEST_CYR.equals(res));

        // not set character set then UTF-8 should be used
        mediaType = new MediaType("text", "plain");
        in = new ByteArrayInputStream(data);
        res = (String)reader.readFrom(String.class, null, null, mediaType, null, in);
        System.out.println(getClass().getName() + " : " + res);
        // string is wrong encoded
        assertFalse(TEST_CYR.equals(res));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testWrite() throws IOException {
        MessageBodyWriter writer = providers.getMessageBodyWriter(String.class, null, null, mediaType);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeTo(TEST_CYR, String.class, String.class, null, mediaType, null, out);
        String res = out.toString("windows-1251");
        System.out.println(getClass().getName() + " : " + res);
        assertTrue(TEST_CYR.equals(res));

        out.reset();

        // not set character set then UTF-8 should be used
        mediaType = new MediaType("text", "plain");
        writer.writeTo(TEST_CYR, String.class, String.class, null, mediaType, null, out);
        res = out.toString("windows-1251");
        System.out.println(res);
        // string is wrong encoded
        assertFalse(TEST_CYR.equals(res));
    }
}
