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
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.OutputHeadersMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class JsonEntityProviderTest extends BaseTest {

    private static final String DATA = "{\"name\":\"andrew\",\"password\":\"hello\"}";

    private MediaType mediaType;

    public void setUp() throws Exception {
        super.setUp();
        mediaType = new MediaType("application", "json");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testRead() throws Exception {
        MessageBodyReader reader = providers.getMessageBodyReader(Bean.class, null, null, mediaType);
        assertNotNull(reader);
        assertTrue(reader.isReadable(Bean.class, Bean.class, null, mediaType));
        byte[] data = DATA.getBytes("UTF-8");
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle(HttpHeaders.CONTENT_LENGTH, "" + data.length);
        Bean bean = (Bean)reader.readFrom(Bean.class, Bean.class, null, mediaType, h, new ByteArrayInputStream(data));
        assertEquals("andrew", bean.getName());
        assertEquals("hello", bean.getPassword());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testWrite() throws Exception {
        MessageBodyWriter writer = providers.getMessageBodyWriter(Bean.class, null, null, mediaType);
        assertNotNull(writer);
        assertTrue(writer.isWriteable(Bean.class, Bean.class, null, mediaType));
        Bean bean = new Bean();
        bean.setName("andrew");
        bean.setPassword("test");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writer.writeTo(bean, Bean.class, Bean.class, null, mediaType, new OutputHeadersMap(), outputStream);
        System.out.println(new String(outputStream.toByteArray()));
    }

    //

    public static class Bean {
        private String name;

        private String password;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String toString() {
            return "name=" + name + "; password=" + password;
        }
    }

}
