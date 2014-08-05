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

import org.everrest.core.ContainerResponseWriter;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.ContainerRequest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.InputHeadersMap;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.tools.DummyContainerResponseWriter;
import org.everrest.core.tools.SimpleSecurityContext;
import org.everrest.exoplatform.StandaloneBaseTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Map;

/**
 * @author andrew00x
 */
public class RegisterComponentTest extends StandaloneBaseTest {
    private RestfulContainer restfulContainer;
    private Field            restToComponentAdaptersField;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        restfulContainer = new RestfulContainer(container);
        ContainerResponseWriter writer = new DummyContainerResponseWriter();
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("TEST", "TO BE OR NOT TO BE");
        ContainerRequest request = new ContainerRequest("GET", new URI("/a"), new URI(""), new ByteArrayInputStream(new byte[0]),
                                                        new InputHeadersMap(h), new SimpleSecurityContext(false));
        ContainerResponse response = new ContainerResponse(writer);
        ApplicationContextImpl.setCurrent(new ApplicationContextImpl(request, response, null));
        // Add dependencies to different containers to be sure both are resolvable.
        container.registerComponentInstance(new ConstructorDependency());
        restfulContainer.registerComponentInstance(new InjectDependency());

        restToComponentAdaptersField = restfulContainer.getClass().getDeclaredField("restToComponentAdapters");
        restToComponentAdaptersField.setAccessible(true);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        restfulContainer.stop();
        ApplicationContextImpl.setCurrent(null);
        super.tearDown();
    }

    @Test
    public void testRegisterProvider() throws Exception {
        restfulContainer.registerComponentImplementation("A", A.class);
        Assert.assertEquals(1, ((Map)restToComponentAdaptersField.get(restfulContainer)).size());
        restfulContainer.unregisterComponent("A");
        Assert.assertEquals(0, ((Map)restToComponentAdaptersField.get(restfulContainer)).size());
    }

    @Test
    public void testRegisterResource() throws Exception {
        restfulContainer.registerComponentImplementation("X", X.class);
        Assert.assertEquals(1, ((Map)restToComponentAdaptersField.get(restfulContainer)).size());
        restfulContainer.unregisterComponent("X");
        Assert.assertEquals(0, ((Map)restToComponentAdaptersField.get(restfulContainer)).size());
    }

    @Test
    public void testCreateProvider() throws Exception {
        restfulContainer.registerComponentImplementation("A", A.class);
        A a = (A)restfulContainer.getComponentInstance("A");
        Assert.assertNotNull(a);
        Assert.assertNotNull(a.h);
        Assert.assertNotNull(a.u);
        Assert.assertEquals("/a", a.u.getRequestUri().getPath());
        Assert.assertEquals("TO BE OR NOT TO BE", a.h.getRequestHeaders().getFirst("test"));
        Assert.assertNotNull(a.inj);
        Assert.assertNotNull(a.c);
    }

    @Test
    public void testCreateResource() throws Exception {
        restfulContainer.registerComponentImplementation("X", X.class);
        X x = (X)restfulContainer.getComponentInstance("X");
        Assert.assertNotNull(x);
        Assert.assertNotNull(x.h);
        Assert.assertNotNull(x.u);
        Assert.assertEquals("/a", x.u.getRequestUri().getPath());
        Assert.assertEquals("TO BE OR NOT TO BE", x.h.getRequestHeaders().getFirst("test"));
        Assert.assertNotNull(x.inj);
        Assert.assertNotNull(x.c);
    }

    public static class ConstructorDependency {
    }

    public static class InjectDependency {
    }

    @Path("X")
    public static class X {
        @Context
        HttpHeaders      h;
        @Context
        UriInfo          u;
        @Inject
        InjectDependency inj;
        final ConstructorDependency c;

        public X(ConstructorDependency c) {
            this.c = c;
        }

        @GET
        public void a() {
        }
    }

    @Provider
    public static class A implements MessageBodyReader<Object> {
        @Context
        HttpHeaders      h;
        @Context
        UriInfo          u;
        @Inject
        InjectDependency inj;
        final ConstructorDependency c;

        public A(ConstructorDependency c) {
            this.c = c;
        }

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
                throws IOException, WebApplicationException {
            return null;
        }
    }
}
