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
package org.everrest.core.impl;

import org.junit.Assert;
import org.junit.Test;

import javax.annotation.PreDestroy;
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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author andrew00x
 */
public class SingletonComponentLifecycleTest extends BaseTest {
    @Path("a")
    public static class Resource1 {
        AtomicInteger destroyVisit = new AtomicInteger();

        @SuppressWarnings("unused")
        @PreDestroy
        private void _destroy() // @PreDestroy must be processed even for private methods.
        {
            destroyVisit.incrementAndGet();
        }

        @GET
        public void m() {
        }
    }

    // Do nothing useful. Just for life cycle test.
    @Provider
    public static class Provider1 implements MessageBodyReader<Object>, MessageBodyWriter<Object> {
        AtomicInteger destroyVisit = new AtomicInteger();

        @SuppressWarnings("unused")
        @PreDestroy
        private void _destroy() // @PreDestroy must be processed even for private methods.
        {
            destroyVisit.incrementAndGet();
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
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
                throws IOException, WebApplicationException {
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

    public static class Application1 extends Application {
        static Set<Object> singletons = new LinkedHashSet<Object>(2);

        @Override
        public Set<Class<?>> getClasses() {
            return null;
        }

        @Override
        public Set<Object> getSingletons() {
            singletons.add(new Resource1());
            singletons.add(new Provider1());
            return singletons;
        }
    }

    @Test
    public void testLifeCycle() throws Exception {
        processor.addApplication(new Application1());
        processor.stop();
        Iterator<Object> iterator = Application1.singletons.iterator();
        Assert.assertEquals(1, ((Resource1)iterator.next()).destroyVisit.intValue());
        Assert.assertEquals(1, ((Provider1)iterator.next()).destroyVisit.intValue());
    }
}
