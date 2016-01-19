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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
 * @author andrew00x
 */
public class StringEntityProviderTest {
    private String    testString;
    private MediaType mediaType;

    @Before
    public void setUp() throws Exception {
        testString = "\u041f\u0440\u0438\u0432\u0456\u0442";
        Map<String, String> p = new HashMap<>(1);
        p.put("charset", "windows-1251");
        mediaType = new MediaType("text", "plain", p);
    }

    @Test
    @SuppressWarnings({"unchecked"})
    public void testRead() throws IOException {
        MessageBodyReader reader = new StringEntityProvider();
        byte[] data = testString.getBytes("windows-1251");
        InputStream in = new ByteArrayInputStream(data);
        String res = (String)reader.readFrom(String.class, String.class, null, mediaType, null, in);
        Assert.assertTrue(testString.equals(res));

        // not set character set then UTF-8 should be used
        mediaType = new MediaType("text", "plain");
        in = new ByteArrayInputStream(data);
        res = (String)reader.readFrom(String.class, null, null, mediaType, null, in);
        System.out.println(getClass().getName() + " : " + res);
        // string is wrong encoded
        Assert.assertFalse(testString.equals(res));
    }

    @Test
    @SuppressWarnings({"unchecked"})
    public void testWrite() throws IOException {
        MessageBodyWriter writer = new StringEntityProvider();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeTo(testString, String.class, String.class, null, mediaType, null, out);
        String res = out.toString("windows-1251");
        System.out.println(getClass().getName() + " : " + res);
        Assert.assertTrue(testString.equals(res));

        out.reset();

        // not set character set then UTF-8 should be used
        mediaType = new MediaType("text", "plain");
        writer.writeTo(testString, String.class, String.class, null, mediaType, null, out);
        res = out.toString("windows-1251");
        System.out.println(res);
        // string is wrong encoded
        Assert.assertFalse(testString.equals(res));
    }
}
