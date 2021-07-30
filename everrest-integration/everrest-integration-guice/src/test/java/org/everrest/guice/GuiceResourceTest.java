/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.guice;

import com.google.common.io.CharStreams;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;

import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.junit.Assert.assertEquals;

/**
 * @author andrew00x
 */
public class GuiceResourceTest extends BaseTest {
    public static class Message {
        private String message;

        public Message(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    @Provider
    public static class MessageProvider implements MessageBodyReader<Message>, MessageBodyWriter<Message> {
        public long getSize(Message message, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return Message.class == type;
        }

        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return Message.class.isAssignableFrom(type);
        }

        public Message readFrom(Class<Message> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                                MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
            return new Message(CharStreams.toString(new InputStreamReader(entityStream, getCharsetOrUtf8(mediaType))));
        }

        public void writeTo(Message message, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
            Writer writer = new OutputStreamWriter(entityStream, getCharsetOrUtf8(mediaType));
            writer.write(message.getMessage());
            writer.flush();
        }

        private String getCharsetOrUtf8(MediaType mediaType) {
            String charset = mediaType == null ? null : mediaType.getParameters().get("charset");
            if (isNullOrEmpty(charset)) {
                charset = "UTF-8";
            }
            return charset;
        }
    }

    @Path("a")
    public static class Resource {
        @GET
        public void m(Message m) {
            assertEquals(messageBody, m.getMessage());
        }
    }

    private static final String messageBody = "GUICE RESOURCE TEST";

    @Test
    public void testResource() throws Exception {
        assertEquals(204, launcher.service("GET", "/a", "", null, messageBody.getBytes(), null).getStatus());
    }

    @Test
    public void testRemapResource() throws Exception {
        assertEquals(204, launcher.service("GET", "/a/b/c", "", null, messageBody.getBytes(), null).getStatus());
    }

    @Override
    protected List<Module> getModules() {
        Module module = new Module() {
            public void configure(Binder binder) {
                binder.bind(Resource.class);
                binder.bind(ServiceBindingHelper.bindingKey(Resource.class, "/a/b/c")).to((Resource.class));
                binder.bind(MessageProvider.class).in(Singleton.class);
            }
        };
        return Collections.singletonList(module);
    }
}
