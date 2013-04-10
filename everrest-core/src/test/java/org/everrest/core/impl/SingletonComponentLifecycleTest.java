/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.core.impl;

import org.everrest.core.servlet.EverrestInitializedListener;
import org.everrest.test.mock.MockServletContext;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
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
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
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

    public void testLifeCycle() throws Exception {
        MockServletContext servletContext = new MockServletContext();
        servletContext.setInitParameter("javax.ws.rs.Application", Application1.class.getName());
        Application1.singletons.clear();
        ServletContextListener listener = new EverrestInitializedListener();
        listener.contextInitialized(new ServletContextEvent(servletContext));
        listener.contextDestroyed(new ServletContextEvent(servletContext));
        Iterator<Object> iterator = Application1.singletons.iterator();
        assertEquals(1, ((Resource1)iterator.next()).destroyVisit.intValue());
        assertEquals(1, ((Provider1)iterator.next()).destroyVisit.intValue());
    }
}
