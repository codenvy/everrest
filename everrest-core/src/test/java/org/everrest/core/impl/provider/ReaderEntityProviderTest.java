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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author andrew00x
 */
public class ReaderEntityProviderTest {
    private MessageBodyReader reader;
    private String            testString;

    @Before
    public void setUp() throws Exception {
        reader = new ReaderEntityProvider();
        testString = "\u041f\u0440\u0438\u0432\u0456\u0442";
        ApplicationContextImpl.setCurrent(new ApplicationContextImpl(
                new ContainerRequest("", URI.create(""), URI.create(""), new EmptyInputStream(), new MultivaluedMapImpl(),
                                     new SimpleSecurityContext(false)), null, ProviderBinder.getInstance()));
    }

    @After
    public void tearDown() throws Exception {
        ApplicationContextImpl.setCurrent(null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testIsReadable() throws Exception {
        Assert.assertTrue(reader.isReadable(Reader.class, null, null, null));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testReadWithCharset() throws Exception {
        InputStream in = new ByteArrayInputStream(testString.getBytes("windows-1251"));
        Map<String, String> p = new HashMap<>(1);
        p.put("charset", "windows-1251");
        MediaType mediaType = new MediaType("text", "plain", p);
        Reader result = (Reader)reader.readFrom(Reader.class, null, null, mediaType, null, in);
        char[] c = new char[1024];
        int b = result.read(c);
        String s = new String(c, 0, b);
        Assert.assertEquals(testString, s);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testReadWithoutCharset() throws Exception {
        // Provoke encoding error, doesn't set encoding in media type
        MediaType mediaType = new MediaType("text", "plain");
        InputStream in = new ByteArrayInputStream(testString.getBytes("windows-1251"));
        Reader result = (Reader)reader.readFrom(Reader.class, null, null, mediaType, null, in);
        char[] c = new char[1024];
        int b = result.read(c);
        String s = new String(c, 0, b);
        Assert.assertNotEquals(testString, s);
    }
}
