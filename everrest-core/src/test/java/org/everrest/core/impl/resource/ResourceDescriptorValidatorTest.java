/*
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.everrest.core.impl.resource;

import org.everrest.core.ComponentLifecycleScope;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.resource.ResourceMethodMap;
import org.everrest.core.resource.SubResourceLocatorDescriptor;
import org.everrest.core.resource.SubResourceMethodDescriptor;

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
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class ResourceDescriptorValidatorTest extends BaseTest {

    public void testAbstractResourceDescriptorValidator() {
        AbstractResourceDescriptor resource =
                new AbstractResourceDescriptorImpl(Resource2.class, ComponentLifecycleScope.PER_REQUEST);
        try {
            resource.accept(ResourceDescriptorValidator.getInstance());
            fail("Exception should be here");
        } catch (RuntimeException e) {
        }
    }

    public void testResourceMethodDescriptorValidator() {
        AbstractResourceDescriptor resource =
                new AbstractResourceDescriptorImpl(Resource3.class, ComponentLifecycleScope.PER_REQUEST);
        for (List<ResourceMethodDescriptor> l : resource.getResourceMethods().values()) {
            ResourceDescriptorValidator validator = ResourceDescriptorValidator.getInstance();
            for (ResourceMethodDescriptor rmd : l) {
                Method m = rmd.getMethod();
                if (m == null) // maybe null for OPTIONS
                    continue;
                String mn = rmd.getMethod().getName();
                if ("m1".equals(mn))
                    rmd.accept(validator);
                else {
                    try {
                        rmd.accept(validator);
                        fail("Exception should be here");
                    } catch (RuntimeException e) {
                    }
                }
            }
        }
    }

    public void testSubResourceMethodDescriptorValidator() {
        AbstractResourceDescriptor resource =
                new AbstractResourceDescriptorImpl(Resource4.class, ComponentLifecycleScope.PER_REQUEST);
        ResourceDescriptorValidator validator = ResourceDescriptorValidator.getInstance();
        for (ResourceMethodMap<SubResourceMethodDescriptor> srmm : resource.getSubResourceMethods().values()) {
            for (List<SubResourceMethodDescriptor> l : srmm.values()) {
                for (SubResourceMethodDescriptor srmd : l) {
                    String mn = srmd.getMethod().getName();
                    if ("m1".equals(mn) || "m3".equals(mn))
                        srmd.accept(validator);
                    else {
                        try {
                            srmd.accept(validator);
                            fail("Exception should be here");
                        } catch (RuntimeException e) {
                        }
                    }
                }
            }
        }
    }

    public void testSubResourceLocatorDescriptorValidator() {
        AbstractResourceDescriptor resource =
                new AbstractResourceDescriptorImpl(Resource5.class, ComponentLifecycleScope.PER_REQUEST);
        ResourceDescriptorValidator validator = ResourceDescriptorValidator.getInstance();
        for (SubResourceLocatorDescriptor rmd : resource.getSubResourceLocators().values()) {
            String mn = rmd.getMethod().getName();
            if ("m1".equals(mn))
                rmd.accept(validator);
            else {
                try {
                    rmd.accept(validator);
                    fail("Exception should be here");
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
