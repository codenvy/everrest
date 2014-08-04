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

import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.resource.ResourceMethodMap;
import org.everrest.core.resource.SubResourceLocatorDescriptor;
import org.everrest.core.resource.SubResourceMethodDescriptor;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author andrew00x
 */
public class ResourceDescriptorValidatorTest {

    @Test
    public void testAbstractResourceDescriptorValidator() {
        AbstractResourceDescriptor resource = new AbstractResourceDescriptorImpl(Resource2.class);
        try {
            resource.accept(ResourceDescriptorValidator.getInstance());
            Assert.fail("Exception should be here");
        } catch (RuntimeException e) {
        }
    }

    @Test
    public void testResourceMethodDescriptorValidator() {
        AbstractResourceDescriptor resource = new AbstractResourceDescriptorImpl(Resource3.class);
        for (List<ResourceMethodDescriptor> l : resource.getResourceMethods().values()) {
            ResourceDescriptorValidator validator = ResourceDescriptorValidator.getInstance();
            for (ResourceMethodDescriptor rmd : l) {
                Method m = rmd.getMethod();
                // maybe null for OPTIONS
                if (m == null) {
                    continue;
                }
                String mn = rmd.getMethod().getName();
                if ("m1".equals(mn)) {
                    rmd.accept(validator);
                } else {
                    try {
                        rmd.accept(validator);
                        Assert.fail("Exception should be here");
                    } catch (RuntimeException e) {
                    }
                }
            }
        }
    }

    @Test
    public void testSubResourceMethodDescriptorValidator() {
        AbstractResourceDescriptor resource = new AbstractResourceDescriptorImpl(Resource4.class);
        ResourceDescriptorValidator validator = ResourceDescriptorValidator.getInstance();
        for (ResourceMethodMap<SubResourceMethodDescriptor> srmm : resource.getSubResourceMethods().values()) {
            for (List<SubResourceMethodDescriptor> l : srmm.values()) {
                for (SubResourceMethodDescriptor srmd : l) {
                    String mn = srmd.getMethod().getName();
                    if ("m1".equals(mn) || "m3".equals(mn)) {
                        srmd.accept(validator);
                    } else {
                        try {
                            srmd.accept(validator);
                            Assert.fail("Exception should be here");
                        } catch (RuntimeException e) {
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testSubResourceLocatorDescriptorValidator() {
        AbstractResourceDescriptor resource = new AbstractResourceDescriptorImpl(Resource5.class);
        ResourceDescriptorValidator validator = ResourceDescriptorValidator.getInstance();
        for (SubResourceLocatorDescriptor rmd : resource.getSubResourceLocators().values()) {
            String mn = rmd.getMethod().getName();
            if ("m1".equals(mn)) {
                rmd.accept(validator);
            } else {
                try {
                    rmd.accept(validator);
                    Assert.fail("Exception should be here");
                } catch (RuntimeException e) {
                }
            }
        }
    }

    //

    @Path("")
    // wrong
    public static class Resource2 {
        @GET
        public void m1() {
        }
    }

    @Path("/a/b")
    public static class Resource3 {
        @GET
        public void m1(@FormParam("a") String t, MultivaluedMap<String, String> entity) {
            // OK
        }

        @SuppressWarnings("rawtypes")
        @POST
        public void m2(@FormParam("a") String t, MultivaluedMap entity) {
            // wrong ?
        }

        @PUT
        public void m3(@FormParam("a") String t, String entity) {
            // wrong
        }

        @HEAD
        public void m4(String entity1, String entity2) {
            // wrong
        }
    }

    @Path("/a/b")
    public static class Resource4 {
        @GET
        @Path("c")
        public void m1() {
            // OK
        }

        @GET
        @Path("")
        // wrong
        public void m2() {
        }

        @GET
        @Path("c/d")
        public void m3(@FormParam("a") String t, MultivaluedMap<String, String> entity) {
            // OK
        }

        @SuppressWarnings("rawtypes")
        @POST
        @Path("c/d/e")
        public void m4(@FormParam("a") String t, MultivaluedMap entity) {
            // wrong ?
        }

        @PUT
        @Path("c/d/e/f")
        public void m5(@FormParam("a") String t, String entity) {
            // wrong
        }

        @GET
        @Path("c/d/e/f/g")
        public void m6(String entity1, String entity2) {
            // wrong
        }
    }

    @Path("/a/b")
    public static class Resource5 {
        @Path("c")
        public void m1() {
            // OK
        }

        @Path("")
        // wrong
        public void m2() {
        }

        @Path("c/d")
        public void m3(@PathParam("a") String t, String entity) {
            // wrong
        }
    }
}
