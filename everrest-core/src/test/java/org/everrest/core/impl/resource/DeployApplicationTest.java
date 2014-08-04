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
package org.everrest.core.impl.resource;

import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResponseFilter;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.resource.GenericMethodResource;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashSet;
import java.util.Set;

/**
 * @author andrew00x
 */
public class DeployApplicationTest extends BaseTest {

    public static class Application1 extends javax.ws.rs.core.Application {
        private final Set<Class<?>> classes    = new HashSet<Class<?>>();
        private final Set<Object>   singletons = new HashSet<Object>();

        public Application1() {
            classes.add(Resource1.class);
            classes.add(Resource2.class);
            classes.add(ExceptionMapper1.class);
            classes.add(MethodInvokerFilter1.class);
            classes.add(RequestFilter1.class);

            singletons.add(new Resource3());
            singletons.add(new Resource4());
            singletons.add(new ExceptionMapper2());
            singletons.add(new ResponseFilter1());
        }

        @Override
        public Set<Class<?>> getClasses() {
            return classes;
        }

        public Set<Object> getSingletons() {
            return singletons;
        }
    }

    // will be per-request resource
    @Path("a")
    public static class Resource1 {
        @GET
        public String m0() {
            return null;
        }
    }

    // will be per-request resource
    @Path("b")
    public static class Resource2 {
        @GET
        public void m0() {
        }
    }

    // will be singleton resource
    @Path("c")
    public static class Resource3 {
        @GET
        public String m0() {
            return null;
        }
    }

    // will be per-request resource
    @Path("d")
    public static class Resource4 {
        @GET
        public void m0() {
        }
    }

    @Provider
    public static class ExceptionMapper1 implements ExceptionMapper<RuntimeException> {
        public Response toResponse(RuntimeException exception) {
            return Response.status(200).entity(exception.getMessage()).build();
        }
    }

    @Provider
    public static class ExceptionMapper2 implements ExceptionMapper<IllegalStateException> {
        public Response toResponse(IllegalStateException exception) {
            return Response.status(200).entity(exception.getMessage()).build();
        }
    }

    @Filter
    public static class MethodInvokerFilter1 implements MethodInvokerFilter {
        public void accept(GenericMethodResource genericMethodResource) {
        }
    }

    @Filter
    public static class RequestFilter1 implements RequestFilter {
        public void doFilter(GenericContainerRequest request) {
        }
    }

    @Filter
    public static class ResponseFilter1 implements ResponseFilter {
        public void doFilter(GenericContainerResponse response) {
        }
    }

    @Test
    public void testRegistry() {
        int resourcesSize = processor.getResources().getSize();
        int requestFiltersSize = processor.getProviders().getRequestFilters(null).size();
        int responseFilterSize = processor.getProviders().getResponseFilters(null).size();
        int methodFilterSize = processor.getProviders().getMethodInvokerFilters(null).size();
        processor.addApplication(new Application1());
        Assert.assertEquals(resourcesSize + 4, processor.getResources().getSize());
        Assert.assertEquals(requestFiltersSize + 1, processor.getProviders().getRequestFilters(null).size());
        Assert.assertEquals(responseFilterSize + 1, processor.getProviders().getResponseFilters(null).size());
        Assert.assertEquals(methodFilterSize + 1, processor.getProviders().getMethodInvokerFilters(null).size());
        Assert.assertNotNull(processor.getProviders().getExceptionMapper(RuntimeException.class));
        Assert.assertNotNull(processor.getProviders().getExceptionMapper(IllegalStateException.class));
    }
}
