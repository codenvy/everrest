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
package org.everrest.exoplatform.container;

import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.ContainerRequest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.tools.DummyContainerResponseWriter;
import org.everrest.core.tools.EmptyInputStream;
import org.everrest.core.tools.SimpleSecurityContext;
import org.everrest.exoplatform.StandaloneBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.ComponentAdapter;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.everrest.core.ApplicationContext.anApplicationContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author andrew00x
 */
public class LookupComponentsTest extends StandaloneBaseTest {
    private RestfulContainer restfulContainer;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        restfulContainer = new RestfulContainer(container);
        ApplicationContext applicationContext = anApplicationContext()
                .withProviders((ProviderBinder)container.getComponentInstanceOfType(ProviderBinder.class))
                .withRequest(new ContainerRequest("XXX", URI.create(""), URI.create(""), new EmptyInputStream(), new MultivaluedMapImpl(), new SimpleSecurityContext(false)))
                .withResponse(new ContainerResponse(new DummyContainerResponseWriter()))
                .build();
        ApplicationContext.setCurrent(applicationContext);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        restfulContainer.stop();
        ApplicationContext.setCurrent(null);
        super.tearDown();
    }

    @Test
    public void testLookupResource() throws Exception {
        restfulContainer.registerComponentImplementation("X", X.class);
        List<String> params = new ArrayList<>();
        ComponentAdapter xAdapter = restfulContainer.getMatchedResource("/x/message", params);
        Object x = xAdapter.getComponentInstance(restfulContainer);
        assertTrue(x instanceof X);
        assertEquals(1, params.size());
        assertEquals("/message", params.get(0));
    }

    @Test
    public void testLookupResource2() throws Exception {
        restfulContainer.registerComponentImplementation("X", X.class);
        restfulContainer.registerComponentImplementation("Y", Y.class);
        List<String> params = new ArrayList<>();
        // Two resource matched to request path but Y must be selected
        // since it has more characters in @Path template.
        ComponentAdapter yAdapter = restfulContainer.getMatchedResource("/x/message", params);
        Object y = yAdapter.getComponentInstance(restfulContainer);
        assertTrue(y instanceof Y);
        assertEquals(2, params.size());
        assertEquals("message", params.get(0));
        assertEquals(null, params.get(1));
    }

    @Test
    public void testLookupWriter() throws Exception {
        restfulContainer.registerComponentImplementation("W", W.class);
        MessageBodyWriter<ToWrite> writer =
                restfulContainer.getMessageBodyWriter(ToWrite.class, null, null, MediaType.TEXT_PLAIN_TYPE);
        assertTrue(writer instanceof W);
    }

    @Test
    public void testLookupWriter2() throws Exception {
        restfulContainer.registerComponentImplementation("W", W.class);
        restfulContainer.registerComponentImplementation("W_TEXT", W_TEXT.class);
        MessageBodyWriter<ToWrite> writer =
                restfulContainer.getMessageBodyWriter(ToWrite.class, null, null, MediaType.TEXT_PLAIN_TYPE);
        // Writer W_TEXT must be selected since it has Produces annotation with media
        // types for which it may be used.
        assertTrue(writer instanceof W_TEXT);
    }

    @Test
    public void testLookupWriter3() throws Exception {
        restfulContainer.registerComponentImplementation("W", W.class);
        restfulContainer.registerComponentImplementation("W_TEXT", W_TEXT.class);
        MessageBodyWriter<ToWrite> writer =
                restfulContainer.getMessageBodyWriter(ToWrite.class, null, null, MediaType.APPLICATION_JSON_TYPE);
        // Writer W must be selected since it is able to process any media types.
        assertTrue(writer instanceof W);
    }

    @Test
    public void testLookupReader() throws Exception {
        restfulContainer.registerComponentImplementation("R", R.class);
        MessageBodyReader<ToRead> reader =
                restfulContainer.getMessageBodyReader(ToRead.class, null, null, MediaType.TEXT_PLAIN_TYPE);
        assertTrue(reader instanceof R);
    }

    @Test
    public void testLookupReader2() throws Exception {
        restfulContainer.registerComponentImplementation("R", R.class);
        restfulContainer.registerComponentImplementation("R_TEXT", R_TEXT.class);
        MessageBodyReader<ToRead> reader =
                restfulContainer.getMessageBodyReader(ToRead.class, null, null, MediaType.TEXT_PLAIN_TYPE);
        // Reader R_TEXT must be selected since it has Consumes annotation with media
        // types for which it may be used.
        assertTrue(reader instanceof R_TEXT);
    }

    @Test
    public void testLookupReader3() throws Exception {
        restfulContainer.registerComponentImplementation("R", R.class);
        restfulContainer.registerComponentImplementation("R_TEXT", R_TEXT.class);
        MessageBodyReader<ToRead> reader =
                restfulContainer.getMessageBodyReader(ToRead.class, null, null, MediaType.APPLICATION_JSON_TYPE);
        // Reader R must be selected since it is able to process any media types.
        assertTrue(reader instanceof R);
    }

    @Test
    public void testLookupExceptionMapper() throws Exception {
        restfulContainer.registerComponentImplementation("E1", E1.class);
        restfulContainer.registerComponentImplementation("E2", E2.class);
        ExceptionMapper<Exception> mapper = restfulContainer.getExceptionMapper(Exception.class);
        assertTrue(mapper instanceof E1);
    }

    @Test
    public void testLookupExceptionMapper2() throws Exception {
        restfulContainer.registerComponentImplementation("E1", E1.class);
        restfulContainer.registerComponentImplementation("E2", E2.class);
        ExceptionMapper<MyException> mapper = restfulContainer.getExceptionMapper(MyException.class);
        assertTrue(mapper instanceof E2);
    }

    @Test
    public void testLookupContextResolver() throws Exception {
        restfulContainer.registerComponentImplementation("C", C.class);
        ContextResolver<ToResolve> resolver =
                restfulContainer.getContextResolver(ToResolve.class, MediaType.APPLICATION_XML_TYPE);
        assertTrue(resolver instanceof C);
    }

    @Test
    public void testLookupContextResolver2() throws Exception {
        restfulContainer.registerComponentImplementation("C", C.class);
        restfulContainer.registerComponentImplementation("C_XML", C_XML.class);
        ContextResolver<ToResolve> resolver =
                restfulContainer.getContextResolver(ToResolve.class, MediaType.APPLICATION_XML_TYPE);
        // C_XML must be selected since it has @Produces annotation and media type
        // in that annotation is matched to "application/xml".
        assertTrue(resolver instanceof C_XML);
    }

    @Test
    public void testLookupContextResolver3() throws Exception {
        restfulContainer.registerComponentImplementation("C", C.class);
        restfulContainer.registerComponentImplementation("C_XML", C_XML.class);
        ContextResolver<ToResolve> resolver =
                restfulContainer.getContextResolver(ToResolve.class, MediaType.APPLICATION_JSON_TYPE);
        // C must be selected since it supports any media types.
        assertTrue(resolver instanceof C);
    }

    @Path("x")
    public static class X {
        @GET
        @Path("{value}")
        public void a() {
        }
    }

    @Path("x/{value}")
    public static class Y {
        @GET
        public void a() {
        }
    }

    public static class ToWrite {
    }

    @Provider
    public static class W implements MessageBodyWriter<ToWrite> {
        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == ToWrite.class;
        }

        @Override
        public long getSize(ToWrite t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return 0;
        }

        @Override
        public void writeTo(ToWrite t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
                                                                                                          WebApplicationException {
        }
    }

    @Provider
    @Produces("text/*")
    public static class W_TEXT implements MessageBodyWriter<ToWrite> {
        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == ToWrite.class;
        }

        @Override
        public long getSize(ToWrite t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return 0;
        }

        @Override
        public void writeTo(ToWrite t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
                                                                                                          WebApplicationException {
        }
    }

    public static class ToRead {
    }

    @Provider
    public static class R implements MessageBodyReader<ToRead> {
        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == ToRead.class;
        }

        @Override
        public ToRead readFrom(Class<ToRead> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
                                                                                                            WebApplicationException {
            return null;
        }
    }

    @Provider
    @Consumes("text/*")
    public static class R_TEXT implements MessageBodyReader<ToRead> {
        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == ToRead.class;
        }

        @Override
        public ToRead readFrom(Class<ToRead> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
                                                                                                            WebApplicationException {
            return null;
        }
    }

    @Provider
    public static class E1 implements ExceptionMapper<Exception> {
        @Override
        public Response toResponse(Exception exception) {
            return null;
        }
    }

    @SuppressWarnings("serial")
    public static class MyException extends Exception {
    }

    @Provider
    public static class E2 implements ExceptionMapper<MyException> {
        @Override
        public Response toResponse(MyException exception) {
            return null;
        }
    }

    public static class ToResolve {
    }

    @Provider
    public static class C implements ContextResolver<ToResolve> {
        @Override
        public ToResolve getContext(Class<?> type) {
            return null;
        }
    }

    @Provider
    @Produces("application/xml")
    public static class C_XML implements ContextResolver<ToResolve> {
        @Override
        public ToResolve getContext(Class<?> type) {
            return null;
        }
    }
}
