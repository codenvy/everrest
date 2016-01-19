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
package org.everrest.core.impl.provider;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.everrest.test.mock.MockHttpServletRequest;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author andrew00x
 */
public class FormEntityTest extends BaseTest {

    @Path("/")
    public static class Resource1 {
        @POST
        @Path("a")
        @Consumes("application/x-www-form-urlencoded")
        public void m1(@FormParam("foo") String foo, @FormParam("bar") String bar, MultivaluedMap<String, String> form) {
            Assert.assertEquals(foo, form.getFirst("foo"));
            Assert.assertEquals(bar, form.getFirst("bar"));
        }

        @POST
        @Path("b")
        @Consumes("application/x-www-form-urlencoded")
        public void m2(MultivaluedMap<String, String> form) {
            Assert.assertEquals("to be or not to be", form.getFirst("foo"));
            Assert.assertEquals("hello world", form.getFirst("bar"));
        }
    }

    @Path("/")
    public static class Resource2 {
        @GET
        @Path("a")
        @Produces("application/x-www-form-urlencoded")
        public MultivaluedMap<String, String> m1() {
            MultivaluedMap<String, String> m = new MultivaluedMapImpl();
            m.putSingle("foo", "bar");
            return m;
        }
    }

    @Test
    public void testFormEntityRead() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource1());
            }
        });
        byte[] data = "foo=to%20be%20or%20not%20to%20be&bar=hello%20world".getBytes("UTF-8");
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("content-type", "application/x-www-form-urlencoded");
        h.putSingle("content-length", "" + data.length);
        Assert.assertEquals(204, launcher.service("POST", "/a", "", h, data, null).getStatus());
        Assert.assertEquals(204, launcher.service("POST", "/b", "", h, data, null).getStatus());
    }

    @Test
    public void getsFormEntityFromServletRequest() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource1());
            }
        });
        EnvironmentContext env = new EnvironmentContext();
        env.put(HttpServletRequest.class,
                new MockHttpServletRequest("/dummy?foo=to%20be%20or%20not%20to%20be&bar=hello%20world",
                                           new ByteArrayInputStream(new byte[0]), 0, "GET", new HashMap<String, List<String>>()));
        EnvironmentContext.setCurrent(env);
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("content-type", "application/x-www-form-urlencoded");
        Assert.assertEquals(204, launcher.service("POST", "/a", "", h, null, env).getStatus());
        Assert.assertEquals(204, launcher.service("POST", "/b", "", h, null, env).getStatus());
    }


    @Test
    public void getParameterMap() throws Exception {
        //given
        MockHttpServletRequest req = new MockHttpServletRequest("/dummy?foo=to%20be%20or%20not%20to%20be&bar=hello%20world",
                                                                new ByteArrayInputStream(new byte[0]), 0, "GET",
                                                                new HashMap<>());
        //when
        Map parameters = req.getParameterMap();
        //then
        Assert.assertEquals(parameters.size(), 2);
        Assert.assertEquals("to be or not to be", ((String[])parameters.get("foo"))[0]);
        Assert.assertEquals("hello world",((String[]) parameters.get("bar"))[0]);
    };



    @Test
    public void testFormEntityWrite() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource2());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("accept", "application/x-www-form-urlencoded");
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service("GET", "/a", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        //System.out.println(new String(writer.getBody()));
        Assert.assertEquals("foo=bar", new String(writer.getBody()));
    }
}
