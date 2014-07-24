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
package org.everrest.exoplatform;

import org.everrest.core.impl.provider.IOHelper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class StandaloneExoResourcesTest extends StandaloneBaseTest {
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
        public long getSize(Message t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return Message.class == type;
        }

        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return Message.class.isAssignableFrom(type);
        }

        public Message readFrom(Class<Message> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                                MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
                                                                                                             WebApplicationException {
            return new Message(IOHelper.readString(entityStream,
                                                   mediaType != null ? mediaType.getParameters().get("charset") : null));
        }

        public void writeTo(Message t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
                                                                                                          WebApplicationException {
            IOHelper.writeString(t.getMessage(), entityStream, mediaType.getParameters().get("charset"));
        }
    }

    @Path("StandaloneExoResourcesTest.Resource1")
    public static class Resource1 {
        @GET
        public void m(Message m) {
            assertEquals(mesageBody, m.getMessage());
        }
    }

    private static final String mesageBody = "EXO RESOURCE TEST";

    @Path("StandaloneExoResourcesTest.Resource2")
    public static class Resource2 {
        @GET
        public void m(Message m) {
            assertEquals(mesageBody, m.getMessage());
        }
    }

    public static class Application0 extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            return Collections.<Class<?>>singleton(Resource2.class);
        }

        @Override
        public Set<Object> getSingletons() {
            return Collections.<Object>singleton(new MessageProvider());
        }
    }

    public void testResource() throws Exception {
        assertEquals(204,
                     launcher.service("GET", "/StandaloneExoResourcesTest.Resource1", "", null, mesageBody.getBytes(), null)
                             .getStatus());
    }

    public void testApplicationResource() throws Exception {
        assertEquals(204,
                     launcher.service("GET", "/StandaloneExoResourcesTest.Resource2", "", null, mesageBody.getBytes(), null)
                             .getStatus());
    }
}
