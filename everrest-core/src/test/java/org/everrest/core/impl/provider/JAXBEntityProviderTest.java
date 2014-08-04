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
package org.everrest.core.impl.provider;

import org.everrest.core.generated.Book;
import org.everrest.core.generated.MemberPrice;
import org.everrest.core.generated.Price;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.ContainerRequest;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.tools.EmptyInputStream;
import org.everrest.core.tools.SimpleSecurityContext;
import org.everrest.core.util.ParameterizedTypeImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;

/**
 * @author andrew00x
 */
public class JAXBEntityProviderTest {

    private byte[]    data;
    private MediaType mediaType;

    @Before
    public void setUp() throws Exception {
        mediaType = new MediaType("application", "xml");
        data =
                ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<book send-by-post=\"true\">"
                 + "<title>Java and XML Data Binding</title>" + "<author>Brett McLaughlin</author>" + "<price>34.95</price>"
                 + "<member-price currency=\"US\">26.56</member-price>" + "</book>").getBytes("UTF-8");
        ApplicationContextImpl.setCurrent(new ApplicationContextImpl(
                new ContainerRequest("", URI.create(""), URI.create(""), new EmptyInputStream(), new MultivaluedMapImpl(),
                                     new SimpleSecurityContext(false)), null, ProviderBinder.getInstance()));
    }

    @After
    public void tearDown() throws Exception {
        ApplicationContextImpl.setCurrent(null);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testReadJAXBElement() throws Exception {
        Class<?> type = JAXBElement.class;
        Type genericType = ParameterizedTypeImpl.newParameterizedType(JAXBElement.class, Book.class);
        MessageBodyReader reader = new JAXBElementEntityProvider(ProviderBinder.getInstance());
        Assert.assertTrue(reader.isReadable(type, genericType, null, mediaType));
        InputStream in = new ByteArrayInputStream(data);
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle(HttpHeaders.CONTENT_LENGTH, "" + data.length);
        JAXBElement<Book> je = (JAXBElement<Book>)reader.readFrom(type, genericType, null, mediaType, h, in);
        Assert.assertTrue("Java and XML Data Binding".equals(je.getValue().getTitle()));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testWriteJAXBElement() throws Exception {
        Class<?> type = JAXBElement.class;
        Type genericType = ParameterizedTypeImpl.newParameterizedType(JAXBElement.class, Book.class);
        MessageBodyWriter writer = new JAXBElementEntityProvider(ProviderBinder.getInstance());
        Assert.assertTrue(writer.isWriteable(type, genericType, null, mediaType));
        JAXBContext ctx = JAXBContext.newInstance(Book.class);
        Unmarshaller um = ctx.createUnmarshaller();
        Source src = new StreamSource(new ByteArrayInputStream(data));
        JAXBElement<Book> je = um.unmarshal(src, Book.class);
        writer.writeTo(je, type, genericType, null, mediaType, null, new ByteArrayOutputStream());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testReadJAXBObject() throws Exception {
        MessageBodyReader reader = new JAXBObjectEntityProvider(ProviderBinder.getInstance());
        Assert.assertTrue(reader.isReadable(Book.class, Book.class, null, mediaType));
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle(HttpHeaders.CONTENT_LENGTH, "" + data.length);
        Book book = (Book)reader.readFrom(Book.class, Book.class, null, mediaType, h, new ByteArrayInputStream(data));
        Assert.assertEquals("Brett McLaughlin", book.getAuthor());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testWriteJAXBObject() throws Exception {
        MessageBodyWriter writer = new JAXBObjectEntityProvider(ProviderBinder.getInstance());
        Assert.assertTrue(writer.isWriteable(Book.class, Book.class, null, mediaType));
        Book book = new Book();
        book.setAuthor("William Shakespeare");
        book.setTitle("Hamlet");
        book.setPrice(createPrice("EUR", 15.15F));
        book.setMemberPrice(createMemberPrice("EUR", 14.73F));
        book.setSendByPost(true);
        writer.writeTo(book, Book.class, Book.class, null, mediaType, null, new ByteArrayOutputStream());
    }

    private static Price createPrice(String currency, Float value) {
        Price price = new Price();
        price.setCurrency(currency);
        price.setValue(new BigDecimal(value));
        return price;
    }

    private static MemberPrice createMemberPrice(String currency, Float value) {
        MemberPrice mprice = new MemberPrice();
        mprice.setCurrency(currency);
        mprice.setValue(new BigDecimal(value));
        return mprice;
    }
}
