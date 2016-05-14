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
package org.everrest.core.impl.integration;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.async.AsynchronousProcess;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AsynchronousRequestTest extends BaseTest {
    @Path("a")
    public static class Resource1 {
        @GET
        public String m() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                return "stopped";
            }
            return "asynchronous response";
        }
    }

    @Path("b")
    public static class Resource2 {
        @GET
        @Path("sub")
        @Produces("application/json")
        public Msg m1() {
            return new Msg();
        }

        @GET
        @Path("sub")
        @Produces("text/plain")
        public String m2() {
            return "text";
        }
    }

    @Path("c")
    public static class Resource3 {
        @GET
        public void m() {
            throw new RuntimeException("test process exceptions in asynchronous mode");
        }
    }

    @Path("d")
    public static class Resource4 {
        @Path("sub")
        public SubResource m() {
            return new SubResource1();
        }
    }

    public interface SubResource {
        @GET
        @Produces("application/json")
        Msg m();
    }

    public static class SubResource1 implements SubResource {
        public Msg m() {
            return new Msg();
        }
    }

    public static class Msg {
        public String getMessage() {
            return "to be or not to be";
        }
    }

    @Test
    public void startsAsynchronousJob() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return newHashSet(Resource1.class);
            }
        });
        String jobUrl = startAsynchronousJobAndGetItsUrl("/a", "GET", null, null, null);

        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = getAsynchronousResponse(jobUrl, writer);
        assertEquals(200, response.getStatus());
        assertEquals("asynchronous response", new String(writer.getBody()));

        // Try one more time. Job must be removed from pool so expected result is 404.
        response = launcher.service("GET", jobUrl, "", null, null, null);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void providesListOfAsynchronousJobsAsJson() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return newHashSet(Resource1.class);
            }
        });
        startAsynchronousJobAndGetItsUrl("/a", "GET", null, null, null);

        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        headers.putSingle("accept", "application/json");
        ContainerResponse response = launcher.service("GET", "/async", "", headers, null, null, null);

        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType().toString());

        Collection<AsynchronousProcess> processes = (Collection<AsynchronousProcess>)response.getEntity();

        assertEquals(1, processes.size());
        AsynchronousProcess process = processes.iterator().next();
        assertNull(process.getOwner());
        assertEquals("running", process.getStatus());
        assertEquals("/a", process.getPath());
    }

    @Test
    public void providesListOfAsynchronousJobsAsPlainText() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return newHashSet(Resource1.class);
            }
        });
        startAsynchronousJobAndGetItsUrl("/a", "GET", null, null, null);

        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        headers.putSingle("accept", "text/plain");
        ContainerResponse response = launcher.service("GET", "/async", "", headers, null, null, null);

        assertEquals(200, response.getStatus());
        assertEquals("text/plain", response.getContentType().toString());

        Collection<AsynchronousProcess> processes = (Collection<AsynchronousProcess>)response.getEntity();

        assertEquals(1, processes.size());
        AsynchronousProcess process = processes.iterator().next();
        assertNull(process.getOwner());
        assertEquals("running", process.getStatus());
        assertEquals("/a", process.getPath());
    }

    @Test
    public void removesRunningAsynchronousJob() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return newHashSet(Resource1.class);
            }
        });
        String jobUrl = startAsynchronousJobAndGetItsUrl("/a", "GET", null, null, null);

        ContainerResponse response = launcher.service("DELETE", jobUrl, "", null, null, null);
        assertEquals(204, response.getStatus());

        response = launcher.service("GET", jobUrl, "", null, null, null);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void findsResourceThatProducesAcceptableMediaType() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return newHashSet(Resource2.class);
            }
        });
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        headers.putSingle("accept", "application/json");
        String jobUrl = startAsynchronousJobAndGetItsUrl("/b/sub", "GET", headers, null, null);

        ContainerResponse response = getAsynchronousResponse(jobUrl, null);

        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType().toString());

        assertEquals("to be or not to be", ((Msg)response.getEntity()).getMessage());
    }

    @Test
    public void processesExceptionThrownInResourceMethod() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return newHashSet(Resource3.class);
            }
        });
        String jobUrl = startAsynchronousJobAndGetItsUrl("/c", "GET", null, null, null);

        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = getAsynchronousResponse(jobUrl, writer);

        assertEquals(500, response.getStatus());
        assertEquals("text/plain", response.getContentType().toString());
        assertEquals("test process exceptions in asynchronous mode", new String(writer.getBody()));
    }

    @Test
    public void runsAsynchronouslySubResourceLocator() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return newHashSet(Resource4.class);
            }
        });
        String jobUrl = startAsynchronousJobAndGetItsUrl("/d/sub", "GET", null, null, null);

        ContainerResponse response = getAsynchronousResponse(jobUrl, null);

        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType().toString());

        assertEquals("to be or not to be", ((Msg)response.getEntity()).getMessage());
    }

    private String startAsynchronousJobAndGetItsUrl(String url, String method, Map<String, List<String>> headers,
                                                    byte[] data, EnvironmentContext env) throws Exception {
        ContainerResponse response = launcher.service(method,
                                                      UriBuilder.fromUri(url).queryParam("async", true).build().toString(),
                                                      "",
                                                      headers,
                                                      data,
                                                      env);
        assertEquals(202, response.getStatus());
        return (String)response.getEntity();
    }

    private ContainerResponse getAsynchronousResponse(String jobUrl, ByteArrayContainerResponseWriter writer) throws Exception {
        ContainerResponse response;
        final long endTime = System.currentTimeMillis() + 5000;
        synchronized (this) {
            while ((response = launcher.service("GET", jobUrl, "", null, null, writer, null)).getStatus() == 202
                   && System.currentTimeMillis() < endTime) {

                wait(300);
                if (writer != null) {
                    writer.reset();
                }
            }
        }
        return response;
    }
}
