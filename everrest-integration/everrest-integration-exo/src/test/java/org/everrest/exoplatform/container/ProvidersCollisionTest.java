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

import org.everrest.exoplatform.StandaloneBaseTest;

import javax.ws.rs.Consumes;
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

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class ProvidersCollisionTest extends StandaloneBaseTest {
    private RestfulContainer restfulContainer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        restfulContainer = new RestfulContainer(container);
    }

    /** @see org.everrest.exoplatform.BaseTest#tearDown() */
    @Override
    protected void tearDown() throws Exception {
        restfulContainer.stop();
        super.tearDown();
    }

    public void testCollisionMultipleImpl1() throws Exception {
        // Component A implements all JAX-RS extension interfaces:
        // javax.ws.rs.ext.MessageBodyReader, javax.ws.rs.ext.MessageBodyWriter,
        // javax.ws.rs.ext.ContextResolver, javax.ws.rs.ext.ExceptionMapper.
        // And it has not any annotation for restriction media types such as:
        // javax.ws.rs.Consumes and javax.ws.rs.Produces so any other implementation of
        // JAX-RS extension interfaces should not be allowed to add in container.
        restfulContainer.registerComponentImplementation(A.class);
        try {
            restfulContainer.registerComponentImplementation(B.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
        try {
            restfulContainer.registerComponentImplementation(C.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
        try {
            restfulContainer.registerComponentImplementation(D.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
        try {
            restfulContainer.registerComponentImplementation(E.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
        try {
            restfulContainer.registerComponentImplementation(F.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
        try {
            restfulContainer.registerComponentImplementation(G.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
    }

    public void testCollisionMultipleImpl2() throws Exception {
        // Component D implements JAX-RS extension interface javax.ws.rs.ext.ExceptionMapper.
        // Components E, F, G must be allowed to add in container but A, B, C should not be
        // because they implements javax.ws.rs.ext.ExceptionMapper for the same exception
        // type (java.lang.RuntimeException).
        restfulContainer.registerComponentImplementation("D", D.class);
        // D2 must be registered because handle sub-class of RuntimeException.
        restfulContainer.registerComponentImplementation("D2", D2.class);
        restfulContainer.unregisterComponent("D2");
        restfulContainer.registerComponentImplementation("E", E.class);
        restfulContainer.unregisterComponent("E");
        restfulContainer.registerComponentImplementation("F", F.class);
        restfulContainer.unregisterComponent("F");
        restfulContainer.registerComponentImplementation("G", G.class);
        restfulContainer.unregisterComponent("G");
        try {
            restfulContainer.registerComponentImplementation("A", A.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
        try {
            restfulContainer.registerComponentImplementation("B", B.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
        try {
            restfulContainer.registerComponentImplementation("C", C.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
    }

    public void testCollisionMultipleImpl3() throws Exception {
        // Component J implements JAX-RS extension interface javax.ws.rs.ext.ContextResolver.
        // Components F, G and D must be allowed to add in container but A, B, E, C should not be
        // because they implements javax.ws.rs.ext.ContextResolver for the same type.
        restfulContainer.registerComponentImplementation("J", J.class);
        // J_TEXT must be registered because to restricted media type: @Produces("text/plain").
        // It does not make conflict with J because it has not any media type restriction.
        restfulContainer.registerComponentImplementation("J_TEXT", J_TEXT.class);
        restfulContainer.unregisterComponent("J_TEXT");
        restfulContainer.registerComponentImplementation("F", F.class);
        restfulContainer.unregisterComponent("F");
        restfulContainer.registerComponentImplementation("G", G.class);
        restfulContainer.unregisterComponent("G");
        try {
            restfulContainer.registerComponentImplementation("A", A.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
        try {
            restfulContainer.registerComponentImplementation("B", B.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
        try {
            restfulContainer.registerComponentImplementation("C", C.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
        try {
            restfulContainer.registerComponentImplementation("E", E.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
    }

    public void testCollisionMultipleImpl4() throws Exception {
        // Interface H implements JAX-RS extension interface javax.ws.rs.ext.MessageBodyWriter.
        // Components C, D must be allowed to add in container but A, B, E, F should not be
        // because they implements javax.ws.rs.ext.MessageBodyWriter for the same type.
        restfulContainer.registerComponentImplementation("H", H.class);
        // H_TEXT must be registered because to restricted media type: @Produces("text/plain").
        // It does not make conflict with H because it has not any media type restriction.
        restfulContainer.registerComponentImplementation("H_TEXT", H_TEXT.class);
        restfulContainer.unregisterComponent("H_TEXT");
        restfulContainer.registerComponentImplementation("C", C.class);
        restfulContainer.unregisterComponent("C");
        restfulContainer.registerComponentImplementation("D", D.class);
        restfulContainer.unregisterComponent("D");
        try {
            restfulContainer.registerComponentImplementation("F", F.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
        try {
            restfulContainer.registerComponentImplementation("E", E.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
        try {
            restfulContainer.registerComponentImplementation("B", B.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
        try {
            restfulContainer.registerComponentImplementation("A", A.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
    }

    public void testCollisionMultipleImpl5() throws Exception {
        // Interface G implements JAX-RS extension interface javax.ws.rs.ext.MessageBodyReader.
        // Components B, C, D must be allowed to add in container but A, E, F should not be
        // because they implements javax.ws.rs.ext.MessageBodyReader for the same type.
        restfulContainer.registerComponentImplementation("G", G.class);
        // G_TEXT must be registered because to restricted media type: @Consumes("text/plain").
        // It does not make conflict with G because it has not any media type restriction.
        restfulContainer.registerComponentImplementation("G_TEXT", G_TEXT.class);
        restfulContainer.unregisterComponent("G_TEXT");
        restfulContainer.registerComponentImplementation("B", B.class);
        restfulContainer.unregisterComponent("B");
        restfulContainer.registerComponentImplementation("C", C.class);
        restfulContainer.unregisterComponent("C");
        restfulContainer.registerComponentImplementation("D", D.class);
        restfulContainer.unregisterComponent("D");
        try {
            restfulContainer.registerComponentImplementation("F", F.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
        try {
            restfulContainer.registerComponentImplementation("E", E.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
        try {
            restfulContainer.registerComponentImplementation("A", A.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
    }

    public void testRegisterComponentTwice() throws Exception {
        restfulContainer.registerComponentImplementation("H", H.class);
        try {
            restfulContainer.registerComponentImplementation("H", H.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
    }

    public void testRegisterExtended() throws Exception {
        restfulContainer.registerComponentImplementation(H.class);
        try {
            restfulContainer.registerComponentImplementation(ExtH.class);
            fail("PicoRegistrationException must be thrown. ");
        } catch (org.picocontainer.PicoRegistrationException e) {
        }
    }

    @Provider
    public static class A implements MessageBodyReader<Object>, MessageBodyWriter<Object>, ContextResolver<Object>,
                                     ExceptionMapper<RuntimeException> {
        @Override
        public Object getContext(Class<?> type) {
            return null;
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return 0;
        }

        @Override
        public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
                                                                                                          WebApplicationException {
        }

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
                                                                                                            WebApplicationException {
            return null;
        }

        @Override
        public Response toResponse(RuntimeException exception) {
            return null;
        }
    }

    @Provider
    public static class B implements MessageBodyWriter<Object>, ContextResolver<Object>,
                                     ExceptionMapper<RuntimeException> {
        @Override
        public Object getContext(Class<?> type) {
            return null;
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return 0;
        }

        @Override
        public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
                                                                                                          WebApplicationException {
        }

        @Override
        public Response toResponse(RuntimeException exception) {
            return null;
        }
    }

    @Provider
    public static class C implements ContextResolver<Object>, ExceptionMapper<RuntimeException> {
        @Override
        public Object getContext(Class<?> type) {
            return null;
        }

        @Override
        public Response toResponse(RuntimeException exception) {
            return null;
        }
    }

    @Provider
    public static class D implements ExceptionMapper<RuntimeException> {
        @Override
        public Response toResponse(RuntimeException exception) {
            return null;
        }
    }

    @Provider
    public static class D2 implements ExceptionMapper<IllegalStateException> {
        @Override
        public Response toResponse(IllegalStateException exception) {
            return null;
        }
    }

    @Provider
    public static class E implements MessageBodyReader<Object>, MessageBodyWriter<Object>, ContextResolver<Object> {
        @Override
        public Object getContext(Class<?> type) {
            return null;
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return 0;
        }

        @Override
        public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
                                                                                                          WebApplicationException {
        }

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
                                                                                                            WebApplicationException {
            return null;
        }
    }

    @Provider
    public static class F implements MessageBodyReader<Object>, MessageBodyWriter<Object> {
        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return 0;
        }

        @Override
        public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
                                                                                                          WebApplicationException {
        }

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
                                                                                                            WebApplicationException {
            return null;
        }
    }

    @Provider
    public static class G implements MessageBodyReader<Object> {
        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
                                                                                                            WebApplicationException {
            return null;
        }
    }

    @Consumes(MediaType.TEXT_PLAIN)
    @Provider
    public static class G_TEXT implements MessageBodyReader<Object> {
        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
                                                                                                            WebApplicationException {
            return null;
        }
    }

    @Provider
    public static class H implements MessageBodyWriter<Object> {
        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return 0;
        }

        @Override
        public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
                                                                                                          WebApplicationException {
        }
    }

    @Provider
    public static class ExtH extends H {
    }

    @Provider
    @Produces(MediaType.TEXT_PLAIN)
    public static class H_TEXT implements MessageBodyWriter<Object> {
        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return 0;
        }

        @Override
        public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
                                                                                                          WebApplicationException {
        }
    }

    @Provider
    public static class J implements ContextResolver<Object> {
        @Override
        public Object getContext(Class<?> type) {
            return null;
        }
    }

    @Provider
    @Produces(MediaType.TEXT_PLAIN)
    public static class J_TEXT implements ContextResolver<Object> {
        @Override
        public Object getContext(Class<?> type) {
            return null;
        }
    }
}
