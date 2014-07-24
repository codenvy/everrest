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
import org.everrest.core.impl.header.MediaTypeHelper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class ReaderEntityProviderTest extends BaseTest {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setContext();
    }

    private static final String TEST_CYR = "\u041f\u0440\u0438\u0432\u0456\u0442";

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testRead() throws Exception {
        MessageBodyReader reader = providers.getMessageBodyReader(Reader.class, null, null, MediaTypeHelper.DEFAULT_TYPE);
        assertNotNull(reader);
        assertNotNull(providers.getMessageBodyReader(Reader.class, null, null, null));
        assertTrue(reader.isReadable(Reader.class, null, null, null));
        byte[] data = TEST_CYR.getBytes("windows-1251");
        InputStream in = new ByteArrayInputStream(data);
        Map<String, String> p = new HashMap<String, String>(1);
        p.put("charset", "windows-1251");
        MediaType mediaType = new MediaType("text", "plain", p);
        Reader result = (Reader)reader.readFrom(Reader.class, null, null, mediaType, null, in);
        char[] c = new char[1024];
        int b = result.read(c);
        String resstr = new String(c, 0, b);
        System.out.println(getClass().getName() + " : " + resstr);
        assertEquals(TEST_CYR, resstr);
        // Provoke encoding error, doesn't set encoding in media type
        mediaType = new MediaType("text", "plain");
        in = new ByteArrayInputStream(data);
        result = (Reader)reader.readFrom(Reader.class, null, null, mediaType, null, in);
        c = new char[1024];
        b = result.read(c);
        resstr = new String(c, 0, b);
        System.out.println(getClass().getName() + " : " + resstr);
        assertFalse(TEST_CYR.equals(resstr));
    }

    //   Test fails on systems which use not UTF-8 encoding.
    //
    //   @SuppressWarnings("unchecked")
    //   public void testWrite() throws Exception
    //   {
    //      MessageBodyWriter writer = providers.getMessageBodyWriter(Reader.class, null, null, MediaTypeHelper.DEFAULT_TYPE);
    //      assertNotNull(writer);
    //      assertNotNull(providers.getMessageBodyWriter(Reader.class, null, null, null));
    //      assertTrue(writer.isWriteable(Reader.class, null, null, null));
    //      Reader source =
    //         new InputStreamReader(new ByteArrayInputStream(TEST_CYR.getBytes("windows-1251")), "windows-1251");
    //      ByteArrayOutputStream out = new ByteArrayOutputStream();
    //      writer.writeTo(source, source.getClass(), null, null, null, null, out);
    //      int b = out.size();
    //      String resstr = new String(out.toByteArray(), 0, b, "UTF-8");
    //      System.out.println(getClass().getName() + " : " + resstr);
    //      assertEquals(TEST_CYR, resstr);
    //   }

}
