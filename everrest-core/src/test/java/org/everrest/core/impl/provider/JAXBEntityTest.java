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

import org.everrest.core.generated.Book;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBElement;
import java.util.Collections;
import java.util.Set;

/**
 * @author andrew00x
 */
public class JAXBEntityTest extends BaseTest {
    @Path("/")
    public static class Resource1 {
        @POST
        @Path("a")
        @Consumes("application/xml")
        public void m0(Book book) {
            Assert.assertEquals("Java and XML Data Binding", book.getTitle());
            Assert.assertEquals("Brett McLaughlin", book.getAuthor());
            Assert.assertEquals("EUR", book.getPrice().getCurrency());
            Assert.assertEquals("EUR", book.getMemberPrice().getCurrency());
            Assert.assertTrue(book.isSendByPost());
        }

        @POST
        @Path("b")
        @Consumes("application/xml")
        public void m1(JAXBElement<Book> e) {
            Book book = e.getValue();
            Assert.assertEquals("Java and XML Data Binding", book.getTitle());
            Assert.assertEquals("Brett McLaughlin", book.getAuthor());
            Assert.assertEquals("EUR", book.getPrice().getCurrency());
            Assert.assertEquals("EUR", book.getMemberPrice().getCurrency());
            Assert.assertTrue(book.isSendByPost());
        }
    }

    @Path("/")
    public static class Resource2 {
        @GET
        @Produces("application/xml")
        public Book m0() {
            Book book = new Book();
            book.setAuthor("William Shakespeare");
            book.setTitle("Hamlet");
            book.setSendByPost(true);
            // ignore some fields
            return book;
        }

        // Without @Produces annotation also should work.
        @POST
        public Book m1() {
            Book book = new Book();
            book.setAuthor("William Shakespeare\n");
            book.setTitle("Hamlet\n");
            book.setSendByPost(false);
            // ignore some fields
            return book;
        }
    }

    private static final String XML_DATA =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<book send-by-post=\"true\">"
            + "<title>Java and XML Data Binding</title>" + "<author>Brett McLaughlin</author>"
            + "<price currency=\"EUR\">34.95</price>" + "<member-price currency=\"EUR\">26.56</member-price>" + "</book>";

    @Test
    public void testJAXBEntityParameter() throws Exception {
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
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        // JAXBElement
        h.putSingle("content-type", "application/xml");
        byte[] data = XML_DATA.getBytes("UTF-8");
        h.putSingle("content-length", "" + data.length);
        Assert.assertEquals(204, launcher.service("POST", "/a", "", h, data, null).getStatus());
        // Object transfered via XML (JAXB)
        Assert.assertEquals(204, launcher.service("POST", "/b", "", h, data, null).getStatus());
    }

    @Test
    public void testJAXBEntityReturn() throws Exception {
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
        h.putSingle("accept", "application/xml");
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service("GET", "/", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/xml", response.getContentType().toString());
        Book book = (Book)response.getEntity();
        Assert.assertEquals("Hamlet", book.getTitle());
        Assert.assertEquals("William Shakespeare", book.getAuthor());
        Assert.assertTrue(book.isSendByPost());
    }

    @Test
    public void testJAXBEntityPostAndReturn() throws Exception {
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
        h.putSingle("accept", "application/xml");
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service("POST", "/", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/xml", response.getContentType().toString());
        Book book = (Book)response.getEntity();
        Assert.assertEquals("Hamlet\n", book.getTitle());
        Assert.assertEquals("William Shakespeare\n", book.getAuthor());
        Assert.assertFalse(book.isSendByPost());
    }
}
