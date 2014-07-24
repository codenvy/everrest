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

import org.everrest.core.ComponentLifecycleScope;
import org.everrest.core.ConstructorDescriptor;
import org.everrest.core.FieldInjector;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.header.MediaTypeHelper;
import org.everrest.core.method.MethodParameter;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.resource.ResourceMethodMap;
import org.everrest.core.resource.SubResourceLocatorDescriptor;
import org.everrest.core.resource.SubResourceLocatorMap;
import org.everrest.core.resource.SubResourceMethodDescriptor;
import org.everrest.core.resource.SubResourceMethodMap;
import org.everrest.core.uri.UriPattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class ResourceDescriptorTest extends BaseTest {

   /*public void testFailedCreation1()
   {
      try
      {
         new AbstractResourceDescriptorImpl(Resource1.class, ComponentLifecycleScope.PER_REQUEST);
         fail("Should be failed here, resource does not contains JAX-RS methods");
      }
      catch (RuntimeException e)
      {
      }
   }*/

    public void testFailedCreation2() {
        try {
            new AbstractResourceDescriptorImpl(Resource2.class, ComponentLifecycleScope.PER_REQUEST);
            fail("Should be failed here, resource does not have public constructor");
        } catch (RuntimeException e) {
        }
    }

    public void testFailedCreation3() {
        try {
            new AbstractResourceDescriptorImpl(Resource3.class, ComponentLifecycleScope.PER_REQUEST);
            fail("Should be failed here, resource has two methods that have tha same HTTP method, consumes and produces annotation");
        } catch (RuntimeException e) {
        }
        try {
            new AbstractResourceDescriptorImpl(Resource4.class, ComponentLifecycleScope.PER_REQUEST);
            fail("Should be failed here, resource has two methods that have tha same HTTP method, consumes and produces annotation");
        } catch (RuntimeException e) {
        }
        try {
            new AbstractResourceDescriptorImpl(Resource5.class, ComponentLifecycleScope.PER_REQUEST);
            fail("Should be failed here, resource has two methods that have tha same HTTP method, path, consumes and produces annotation");
        } catch (RuntimeException e) {
        }
        try {
            new AbstractResourceDescriptorImpl(Resource6.class, ComponentLifecycleScope.PER_REQUEST);
            fail("Should be failed here, resource has two methods that have tha same HTTP method, path, consumes and produces annotation");
        } catch (RuntimeException e) {
        }
        try {
            new AbstractResourceDescriptorImpl(Resource7.class, ComponentLifecycleScope.PER_REQUEST);
            fail("Should be failed here, resource has two methods that have tha same path");
        } catch (RuntimeException e) {
        }
    }

    public void testFailedCreation4() {
        try {
            new AbstractResourceDescriptorImpl(Resource8.class, ComponentLifecycleScope.PER_REQUEST);
            fail("Should be failed here, method has two JAX-RS annotation on the same parameter");
        } catch (RuntimeException e) {
        }
        // must warn
        //new AbstractResourceDescriptorImpl(Resource9.class, ComponentLifecycleScope.PER_REQUEST);
    }

    public void testFailedCreation5() {
        try {
            new AbstractResourceDescriptorImpl(Resource10.class, ComponentLifecycleScope.PER_REQUEST);
            fail("Should be failed here, constructor of per-request resource has two JAX-RS annotation on the same parameter");
        } catch (RuntimeException e) {
        }
        // must warn
        //new AbstractResourceDescriptorImpl(Resource11.class, ComponentLifecycleScope.PER_REQUEST);
    }

    public void testFailedCreation6() {
        try {
            new AbstractResourceDescriptorImpl(Resource12.class, ComponentLifecycleScope.PER_REQUEST);
            fail("Should be failed here, fields of per-request resource has two JAX-RS annotation on the same parameter");
        } catch (RuntimeException e) {
        }
    }

/*
   public void testNotPublicMethodAnnotated()
   {
      new AbstractResourceDescriptorImpl(Resource14.class, ComponentLifecycleScope.PER_REQUEST);
   }
*/

    // ====================== all of this resource are not valid =========================

    @Path("a")
    public static class Resource1 {
        public void m1() {
            // no JAX-RS methods
        }
    }

    @Path("a")
    public static class Resource2 {
        Resource2() {
        }

        @GET
        public void m1() {
        }
    }

    @Path("a")
    public static class Resource3 {
        @GET
        public void m1() {
        }

        @GET
        public void m2() {
        }
    }

    @Path("a")
    public static class Resource4 {


        @GET
        @Consumes({"text/xml", "application/xml", "application/xml+xhtml"})
        @Produces("text/plain")
        public void m1() {
        }


        @GET
        @Consumes({"application/xml", "text/xml", "application/xml+xhtml"})
        @Produces("text/plain")
        public void m2() {
        }

        @GET
//      @Consumes({"application/xml", "text/xml", "application/xml+xhtml1"})
//      @Produces("text/plain")
        public void m0() {
        }
    }

    @Path("a")
    public static class Resource5 {
        @GET
        @Path("b")
        public void m1() {
        }

        @GET
        @Path("b")
        public void m2() {
        }
    }

    @Path("a")
    public static class Resource6 {
        @GET
        @Consumes({"text/xml", "application/xml", "application/xml+xhtml"})
        @Produces("text/plain")
        @Path("b")
        public void m1() {
        }

        @GET
        @Consumes({"application/xml", "text/xml", "application/xml+xhtml"})
        @Produces("text/plain")
        @Path("b")
        public void m2() {
        }
    }

    @Path("/a")
    public static class Resource7 {
        @Path("b")
        public Object m1() {
            return new Object();
        }

        @Path("/b")
        public Object m2() {
            return new Object();
        }
    }

    @Path("/a")
    public static class Resource8 {
        @GET
        @Path("b")
        public void m1(@PathParam("b") @HeaderParam("head1") String b) {
        }
    }

    @Path("/a")
    public static class Resource9 {
        @GET
        @Path("c")
        public void m1(@Test @HeaderParam("head1") String b) {
        }
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Test {

    }

    @Path("/a")
    public static class Resource10 {

        public Resource10(@PathParam("b") @HeaderParam("head1") String b) {
        }

        @GET
        public void m1() {
        }
    }

    @Path("/a")
    public static class Resource11 {

        // must warn about two constructors with the same number of parameters

        public Resource11(@PathParam("b") String b, @QueryParam("c") int c) {
        }

        public Resource11(@HeaderParam("head1") int b, @PathParam("c") String c) {
        }

        @GET
        public void m1() {
        }
    }

    public static class Resource12 {

        @SuppressWarnings("unused")
        @Context
        private UriInfo uriInfo;

        @PathParam("b")
        @QueryParam("query")
        String b;

        @GET
        public void m1() {
        }
    }

    @Path("/")
    public static class Resource14 {
        @GET
        void get() {
            // not public method annotated
        }

        @POST
        public void post() {
        }
    }

    // ===================================== end =================================

    public void testCreateAbstractResourceDescriptor() {
        AbstractResourceDescriptor resource =
                new AbstractResourceDescriptorImpl(SampleResource.class, ComponentLifecycleScope.PER_REQUEST);
        assertTrue(resource.isRootResource());
        assertEquals("/a/{b}/", resource.getPathValue().getPath());
        assertEquals(SampleResource.class, resource.getObjectClass());
        assertEquals(3, resource.getResourceMethods().size());
        assertEquals(1, resource.getSubResourceMethods().size());
        assertEquals(3, resource.getSubResourceMethods().values().iterator().next().size());
        assertEquals(1, resource.getSubResourceLocators().size());
    }

    public void testResourceMethods() {
        AbstractResourceDescriptor resource =
                new AbstractResourceDescriptorImpl(SampleResource.class, ComponentLifecycleScope.PER_REQUEST);
        // GET
        ResourceMethodDescriptor methodDescriptor = resource.getResourceMethods().getFirst("GET");
        assertEquals("GET", methodDescriptor.getHttpMethod());
        assertEquals(MediaTypeHelper.DEFAULT_TYPE, methodDescriptor.consumes().get(0));
        assertEquals(MediaType.valueOf("application/xml"), methodDescriptor.produces().get(0));
        assertEquals(SampleResource.class, methodDescriptor.getParentResource().getObjectClass());
        assertEquals(1, methodDescriptor.getMethodParameters().size());
        MethodParameter methodParameter = methodDescriptor.getMethodParameters().get(0);
        assertEquals("hello", methodParameter.getDefaultValue());
        assertEquals(String.class, methodParameter.getParameterClass());
        assertEquals(PathParam.class, methodParameter.getAnnotation().annotationType());
        assertEquals(2, methodParameter.getAnnotations().length);
        assertEquals(PathParam.class, methodParameter.getAnnotations()[0].annotationType());
        assertEquals(DefaultValue.class, methodParameter.getAnnotations()[1].annotationType());
        // the same must be for HEAD
        methodDescriptor = resource.getResourceMethods().getFirst("HEAD");
        assertEquals("HEAD", methodDescriptor.getHttpMethod());
        assertEquals(MediaTypeHelper.DEFAULT_TYPE, methodDescriptor.consumes().get(0));
        assertEquals(MediaType.valueOf("application/xml"), methodDescriptor.produces().get(0));
        assertEquals(SampleResource.class, methodDescriptor.getParentResource().getObjectClass());
        assertEquals(1, methodDescriptor.getMethodParameters().size());
        methodParameter = methodDescriptor.getMethodParameters().get(0);
        assertEquals("hello", methodParameter.getDefaultValue());
        assertEquals(String.class, methodParameter.getParameterClass());
        assertEquals(PathParam.class, methodParameter.getAnnotation().annotationType());
        assertEquals(2, methodParameter.getAnnotations().length);
        assertEquals(PathParam.class, methodParameter.getAnnotations()[0].annotationType());
        assertEquals(DefaultValue.class, methodParameter.getAnnotations()[1].annotationType());
    }

    public void testSubResourceMethods() {
        AbstractResourceDescriptor resource =
                new AbstractResourceDescriptorImpl(SampleResource.class, ComponentLifecycleScope.PER_REQUEST);
        Collection<ResourceMethodMap<SubResourceMethodDescriptor>> subRes = resource.getSubResourceMethods().values();
        // POST
        SubResourceMethodDescriptor subResourceMethodDescriptor = subRes.iterator().next().getFirst("POST");
        assertEquals("POST", subResourceMethodDescriptor.getHttpMethod());
        assertEquals("{c}", subResourceMethodDescriptor.getPathValue().getPath());
        assertEquals(MediaType.valueOf("text/plain"), subResourceMethodDescriptor.consumes().get(0));
        assertEquals(MediaType.valueOf("text/xml"), subResourceMethodDescriptor.consumes().get(1));
        assertEquals(MediaType.valueOf("text/html"), subResourceMethodDescriptor.produces().get(0));
        assertEquals(SampleResource.class, subResourceMethodDescriptor.getParentResource().getObjectClass());
        assertEquals(1, subResourceMethodDescriptor.getMethodParameters().size());
        MethodParameter methodParameter = subResourceMethodDescriptor.getMethodParameters().get(0);
        assertEquals(null, methodParameter.getDefaultValue());
        assertEquals(List.class, methodParameter.getParameterClass());
        assertEquals(PathParam.class, methodParameter.getAnnotation().annotationType());
        assertEquals(1, methodParameter.getAnnotations().length);
        assertEquals(PathParam.class, methodParameter.getAnnotations()[0].annotationType());
        // GET
        subResourceMethodDescriptor = subRes.iterator().next().getFirst("GET");
        assertEquals("GET", subResourceMethodDescriptor.getHttpMethod());
        assertEquals("{d}", subResourceMethodDescriptor.getPathValue().getPath());
        assertEquals(MediaType.valueOf("text/plain"), subResourceMethodDescriptor.consumes().get(0));
        assertEquals(MediaType.valueOf("text/xml"), subResourceMethodDescriptor.consumes().get(1));
        assertEquals(MediaType.valueOf("text/html"), subResourceMethodDescriptor.produces().get(0));
        assertEquals(SampleResource.class, subResourceMethodDescriptor.getParentResource().getObjectClass());
        assertEquals(1, subResourceMethodDescriptor.getMethodParameters().size());
        methodParameter = subResourceMethodDescriptor.getMethodParameters().get(0);
        assertEquals(null, methodParameter.getDefaultValue());
        assertEquals(List.class, methodParameter.getParameterClass());
        assertEquals(PathParam.class, methodParameter.getAnnotation().annotationType());
        assertEquals(1, methodParameter.getAnnotations().length);
        assertEquals(PathParam.class, methodParameter.getAnnotations()[0].annotationType());
        // HEAD
        subResourceMethodDescriptor = subRes.iterator().next().getFirst("HEAD");
        assertEquals("HEAD", subResourceMethodDescriptor.getHttpMethod());
        assertEquals("{d}", subResourceMethodDescriptor.getPathValue().getPath());
        assertEquals(MediaType.valueOf("text/plain"), subResourceMethodDescriptor.consumes().get(0));
        assertEquals(MediaType.valueOf("text/xml"), subResourceMethodDescriptor.consumes().get(1));
        assertEquals(MediaType.valueOf("text/html"), subResourceMethodDescriptor.produces().get(0));
        assertEquals(SampleResource.class, subResourceMethodDescriptor.getParentResource().getObjectClass());
        assertEquals(1, subResourceMethodDescriptor.getMethodParameters().size());
        methodParameter = subResourceMethodDescriptor.getMethodParameters().get(0);
        assertEquals(null, methodParameter.getDefaultValue());
        assertEquals(List.class, methodParameter.getParameterClass());
        assertEquals(PathParam.class, methodParameter.getAnnotation().annotationType());
        assertEquals(1, methodParameter.getAnnotations().length);
        assertEquals(PathParam.class, methodParameter.getAnnotations()[0].annotationType());
    }

    public void testSubResourceLocators() {
        // sub-resource method SampleResource#get2()
        AbstractResourceDescriptor resource =
                new AbstractResourceDescriptorImpl(SampleResource.class, ComponentLifecycleScope.PER_REQUEST);
        SubResourceLocatorDescriptor subResourceLocatorDescriptor =
                resource.getSubResourceLocators().values().iterator().next();
        assertEquals("{c}/d", subResourceLocatorDescriptor.getPathValue().getPath());
        assertEquals(SampleResource.class, subResourceLocatorDescriptor.getParentResource().getObjectClass());
        assertEquals(1, subResourceLocatorDescriptor.getMethodParameters().size());
        MethodParameter methodParameter = subResourceLocatorDescriptor.getMethodParameters().get(0);
        assertTrue(methodParameter.isEncoded());
        assertEquals(null, methodParameter.getDefaultValue());
        assertEquals(String.class, methodParameter.getParameterClass());
        assertEquals(PathParam.class, methodParameter.getAnnotation().annotationType());
        assertEquals(2, methodParameter.getAnnotations().length);
        assertEquals(PathParam.class, methodParameter.getAnnotations()[0].annotationType());
    }

    public void testFields() {
        AbstractResourceDescriptor resource =
                new AbstractResourceDescriptorImpl(SampleResource.class, ComponentLifecycleScope.PER_REQUEST);
        List<FieldInjector> fields = resource.getFieldInjectors();
        assertEquals(1, fields.size());
        FieldInjector f = fields.get(0);
        assertEquals(String.class, f.getParameterClass());
        assertEquals(String.class, f.getGenericType());
        assertEquals("default", f.getDefaultValue());
        assertEquals(PathParam.class, f.getAnnotation().annotationType());
        assertEquals("b", ((PathParam)f.getAnnotation()).value());
        assertTrue(f.isEncoded());
    }

    public void testConstructors() {
        AbstractResourceDescriptor resource =
                new AbstractResourceDescriptorImpl(SampleResource.class, ComponentLifecycleScope.PER_REQUEST);
        assertEquals(3, resource.getConstructorDescriptors().size());
        List<ConstructorDescriptor> c = resource.getConstructorDescriptors();
        assertEquals(2, c.get(0).getParameters().size());
        assertEquals(1, c.get(1).getParameters().size());
        assertEquals(0, c.get(2).getParameters().size());

        assertFalse(c.get(0).getParameters().get(0).isEncoded());
        assertTrue(c.get(0).getParameters().get(1).isEncoded());
        assertEquals(QueryParam.class, c.get(0).getParameters().get(0).getAnnotation().annotationType());
        assertEquals(PathParam.class, c.get(0).getParameters().get(1).getAnnotation().annotationType());
        assertEquals("test", ((QueryParam)c.get(0).getParameters().get(0).getAnnotation()).value());
        assertEquals("b", ((PathParam)c.get(0).getParameters().get(1).getAnnotation()).value());

        assertFalse(c.get(1).getParameters().get(0).isEncoded());
        assertEquals(PathParam.class, c.get(1).getParameters().get(0).getAnnotation().annotationType());
        assertEquals("b", ((PathParam)c.get(1).getParameters().get(0).getAnnotation()).value());
    }

    @SuppressWarnings("unused")
    @Path("/a/{b}/")
    public static class SampleResource {

        @DefaultValue("default")
        @PathParam("b")
        @Encoded
        private String field1;

        public SampleResource(@PathParam("b") String str) {
        }

        public SampleResource() {
        }

        public SampleResource(@QueryParam("test") int i, @Encoded @PathParam("b") String str) {
        }

        @POST
        @Path("{c}")
        @Consumes({"text/plain", "text/xml"})
        @Produces({"text/html"})
        public void post1(@PathParam("b") List<String> p) {
            // this is sub-resource method
        }

        @GET
        @Path("{d}")
        @Consumes({"text/plain", "text/xml"})
        @Produces({"text/html"})
        public void get1(@PathParam("b") List<String> p) {
            // this is sub-resource method
        }

        @Path("{c}/d")
        public void get2(@PathParam("b") @Encoded String p) {
            // this is sub-resource locator
        }

        @GET
        @Produces({"application/xml"})
        public void get3(@PathParam("b") @DefaultValue("hello") String p) {
            // this is resource method
        }
    }

    public void testResourceMethodSorting() {
        AbstractResourceDescriptor resource =
                new AbstractResourceDescriptorImpl(SampleResource1.class, ComponentLifecycleScope.PER_REQUEST);
        List<ResourceMethodDescriptor> l = resource.getResourceMethods().get("GET");
        assertEquals("m4", l.get(0).getMethod().getName());
        assertEquals("m3", l.get(1).getMethod().getName());
        assertEquals("m2", l.get(2).getMethod().getName());
        assertEquals("m5", l.get(3).getMethod().getName());
        assertEquals("m0", l.get(4).getMethod().getName());
        assertEquals("m1", l.get(5).getMethod().getName());
    }

    @Path("a")
    public static class SampleResource1 {
        @Consumes({"application/*", "application/xml", "text/*"})
        @Produces({"text/plain", "text/html", "text/*"})
        @GET
        public void m0() {
            // before last
        }

        @GET
        public void m1() {
            // last, weak defined consumes and produces
        }

        @Consumes({"application/*", "text/*"})
        @Produces({"text/plain", "text/html"})
        @GET
        public void m2() {
            // should be third, produces is strong defined
        }

        @Consumes({"application/xml", "text/plain"})
        @GET
        public void m3() {
            // should be second after sorting
        }

        @Consumes({"application/xml"})
        @GET
        public void m4() {
            // should be first after sorting, consumes type checked first and it is
            // strong defined and number of item in consumes less the for method m3
        }

        @Consumes({"text/*"})
        @Produces({"text/html", "text/*"})
        @GET
        public void m5() {
            // forth, less consumes and produces length then at method m0
        }
    }

    public void testSubResourceMethodSorting() {
        AbstractResourceDescriptor resource =
                new AbstractResourceDescriptorImpl(SampleResource2.class, ComponentLifecycleScope.PER_REQUEST);
        SubResourceMethodMap srmm = resource.getSubResourceMethods();
        Collection<UriPattern> uris = srmm.keySet();
        Iterator<UriPattern> i = uris.iterator();
        // NOTE template will be normalized, first slash added
        assertEquals("/b/c/d", i.next().getTemplate());
        assertEquals("/b/c", i.next().getTemplate());
        assertEquals("/b/{c}", i.next().getTemplate());
        assertEquals("/b", i.next().getTemplate());

        i = uris.iterator();
        ResourceMethodMap<SubResourceMethodDescriptor> rmm = srmm.getMethodMap(i.next());
        assertEquals(2, rmm.size()); // Method GET + HEAD (auto-generated)
        assertEquals(1, rmm.get("GET").size());
        assertEquals("m4", rmm.get("GET").get(0).getMethod().getName());
        rmm = srmm.getMethodMap(i.next());
        assertEquals(2, rmm.size());
        assertEquals(1, rmm.get("GET").size());
        assertEquals("m1", rmm.get("GET").get(0).getMethod().getName());
        rmm = srmm.getMethodMap(i.next());
        assertEquals(2, rmm.size());
        assertEquals(1, rmm.get("GET").size());
        assertEquals("m3", rmm.get("GET").get(0).getMethod().getName());
        rmm = srmm.getMethodMap(i.next());
        assertEquals(2, rmm.size());
        assertEquals(3, rmm.get("GET").size());
        assertEquals("m2", rmm.get("GET").get(0).getMethod().getName());
        assertEquals("m5", rmm.get("GET").get(1).getMethod().getName());
        assertEquals("m0", rmm.get("GET").get(2).getMethod().getName());
    }

    @Path("a")
    public static class SampleResource2 {
        @Consumes({"application/*", "application/xml", "text/*"})
        @Produces({"text/plain", "text/html", "text/*"})
        @GET
        @Path("b")
        public void m0() {
        }

        @GET
        @Path("b/c")
        public void m1() {
        }

        @Consumes({"application/*", "text/*"})
        @Produces({"text/plain", "text/html"})
        @GET
        @Path("b")
        public void m2() {
        }

        @Consumes({"application/xml", "text/plain"})
        @GET
        @Path("b/{c}")
        public void m3() {
        }

        @Consumes({"application/xml"})
        @GET
        @Path("b/c/d")
        public void m4() {
        }

        @Consumes({"text/*"})
        @Produces({"text/html", "text/*"})
        @GET
        @Path("b")
        public void m5() {
        }
    }

    public void testSubResourceLocatorSorting() {
        AbstractResourceDescriptor resource =
                new AbstractResourceDescriptorImpl(SampleResource3.class, ComponentLifecycleScope.PER_REQUEST);
        SubResourceLocatorMap locs = resource.getSubResourceLocators();
        Collection<UriPattern> uris = locs.keySet();
        Iterator<UriPattern> i = uris.iterator();
        // NOTE template will be normalized, first slash added
        assertEquals("/b/c/d", i.next().getTemplate());
        assertEquals("/b/c/z", i.next().getTemplate());
        assertEquals("/b/c", i.next().getTemplate());
        assertEquals("/b/{c}", i.next().getTemplate());
        assertEquals("/b", i.next().getTemplate());
        Iterator<SubResourceLocatorDescriptor> i2 = locs.values().iterator();
        assertEquals("m3", i2.next().getMethod().getName());
        assertEquals("m1", i2.next().getMethod().getName());
        assertEquals("m4", i2.next().getMethod().getName());
        assertEquals("m2", i2.next().getMethod().getName());
        assertEquals("m0", i2.next().getMethod().getName());
    }

    @Path("a")
    public static class SampleResource3 {
        @Path("b")
        public void m0() {
        }

        @Path("b/c/z")
        public void m1() {
        }

        @Path("b/{c}")
        public void m2() {
        }

        @Path("b/c/d")
        public void m3() {
        }

        @Path("b/c")
        public void m4() {
        }

    }

    // =========================================

    public void testInitializeFieldSuperClass() {
        AbstractResourceDescriptor resource =
                new AbstractResourceDescriptorImpl(EndResource.class, ComponentLifecycleScope.PER_REQUEST);
        assertEquals(6, resource.getFieldInjectors().size());
    }

    public abstract static class AbstractResource {
        @Context
        protected UriInfo uriInfo;

        @Context
        public Request request;

        @Context
      /*protected */ UriInfo something;
    }

    public abstract static class ExtResource extends AbstractResource {

        @Context
        protected SecurityContext sc;

    }

    public static class EndResource extends ExtResource {
        @SuppressWarnings("unused")
        @Context
        private HttpHeaders header;

        @SuppressWarnings("unused")
        @Context
        private UriInfo something;

        @GET
        public void m1() {
        }
    }
}
