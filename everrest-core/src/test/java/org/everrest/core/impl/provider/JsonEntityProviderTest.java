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
import org.everrest.core.impl.OutputHeadersMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author andrew00x
 */
public class JsonEntityProviderTest {

    private String    data;
    private MediaType mediaType;

    @Before
    public void setUp() throws Exception {
        data = "{\"name\":\"andrew\",\"password\":\"hello\"}";
        mediaType = new MediaType("application", "json");
    }

    @Test
    @SuppressWarnings({"unchecked"})
    public void testRead() throws Exception {
        MessageBodyReader reader = new JsonEntityProvider();
        Assert.assertTrue(reader.isReadable(Bean.class, Bean.class, null, mediaType));
        byte[] data = this.data.getBytes("UTF-8");
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle(HttpHeaders.CONTENT_LENGTH, Integer.toString(data.length));
        Bean bean = (Bean)reader.readFrom(Bean.class, Bean.class, null, mediaType, h, new ByteArrayInputStream(data));
        Assert.assertEquals("andrew", bean.getName());
        Assert.assertEquals("hello", bean.getPassword());
    }

    @Test
    @SuppressWarnings({"unchecked"})
    public void testWrite() throws Exception {
        MessageBodyWriter writer = new JsonEntityProvider();
        Assert.assertTrue(writer.isWriteable(Bean.class, Bean.class, null, mediaType));
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
