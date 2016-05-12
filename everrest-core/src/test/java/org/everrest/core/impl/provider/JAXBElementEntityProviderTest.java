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
import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;

import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static org.everrest.core.util.ParameterizedTypeImpl.newParameterizedType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked"})
public class JAXBElementEntityProviderTest {
    private static final String TEST_XML_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                                                   + "<book send-by-post=\"true\">"
                                                   + "  <title>Java and XML Data Binding</title>"
                                                   + "  <author>Brett McLaughlin</author>"
                                                   + "  <price currency=\"USD\">34.95</price>"
                                                   + "  <member-price currency=\"USD\">26.56</member-price>"
                                                   + "</book>";

    private static final byte[] TEST_XML_CONTENT_BYTES = TEST_XML_CONTENT.getBytes();

    private MessageBodyReader   reader;
    private MessageBodyWriter   writer;

    @Before
    public void setUp() throws Exception {
        JAXBContextResolver jaxbContextResolver = mock(JAXBContextResolver.class);
        when(jaxbContextResolver.getContext(any())).thenReturn(jaxbContextResolver);
        when(jaxbContextResolver.getJAXBContext(Book.class)).thenReturn(JAXBContext.newInstance(Book.class));

        Providers providers = mock(Providers.class);
        when(providers.getContextResolver(JAXBContextResolver.class, APPLICATION_XML_TYPE)).thenReturn(jaxbContextResolver);

        reader = new JAXBElementEntityProvider(providers);
        writer = new JAXBElementEntityProvider(providers);
    }

    @Test
    public void isReadableForJAXBElement() throws Exception {
        Type genericType = newParameterizedType(JAXBElement.class, Book.class);
        assertTrue(reader.isReadable(JAXBElement.class, genericType, null, null));
    }

    @Test
    public void isNotReadableForTypeOtherThanJAXBElement() throws Exception {
        Type genericType = newParameterizedType(JAXBElement.class, Book.class);
        assertFalse(reader.isReadable(String.class, genericType, null, null));
    }

    @Test
    public void isNotReadableWhenGenericTypeIsNotAvailable() throws Exception {
        assertFalse(reader.isReadable(JAXBElement.class, null, null, null));
    }

    @Test
    public void isWriteableForJAXBElement() throws Exception {
        assertTrue(writer.isWriteable(JAXBElement.class, null, null, null));
    }

    @Test
    public void isNotWriteableForTypeOtherThanJAXBElement() throws Exception {
        Type genericType = newParameterizedType(JAXBElement.class, Book.class);
        assertFalse(writer.isWriteable(String.class, genericType, null, null));
    }

    @Test
    public void readsEntityStreamAsJAXBElement() throws Exception {
        Class<JAXBElement> type = JAXBElement.class;
        Type genericType = newParameterizedType(JAXBElement.class, Book.class);
        InputStream in = new ByteArrayInputStream(TEST_XML_CONTENT_BYTES);

        JAXBElement<Book> bookJAXBElement = (JAXBElement<Book>)reader.readFrom(type, genericType, null, APPLICATION_XML_TYPE, new MultivaluedMapImpl(), in);

        Book book = bookJAXBElement.getValue();
        assertEquals("Java and XML Data Binding", book.getTitle());
        assertTrue(book.isSendByPost());
        assertEquals("Brett McLaughlin", book.getAuthor());
        assertEquals("USD", book.getPrice().getCurrency());
        assertEquals(new BigDecimal("34.95"), book.getPrice().getValue());
        assertEquals("USD", book.getMemberPrice().getCurrency());
        assertEquals(new BigDecimal("26.56"), book.getMemberPrice().getValue());
    }

    @Test
    public void writesJAXBElementToOutputStream() throws Exception {
        Class<?> type = JAXBElement.class;
        Type genericType = newParameterizedType(JAXBElement.class, Book.class);
        JAXBElement<Book> bookJAXBElement = unmarshalJAXBElement(TEST_XML_CONTENT_BYTES);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        writer.writeTo(bookJAXBElement, type, genericType, null, APPLICATION_XML_TYPE, null, out);

        JAXBElement<Book> writtenBookJAXBElement = unmarshalJAXBElement(out.toByteArray());
        Book book = writtenBookJAXBElement.getValue();
        assertEquals("Java and XML Data Binding", book.getTitle());
        assertTrue(book.isSendByPost());
        assertEquals("Brett McLaughlin", book.getAuthor());
        assertEquals("USD", book.getPrice().getCurrency());
        assertEquals(new BigDecimal("34.95"), book.getPrice().getValue());
        assertEquals("USD", book.getMemberPrice().getCurrency());
        assertEquals(new BigDecimal("26.56"), book.getMemberPrice().getValue());
    }

    private JAXBElement<Book> unmarshalJAXBElement(byte[] content) throws JAXBException {
        Unmarshaller unmarshaller = JAXBContext.newInstance(Book.class).createUnmarshaller();
        Source src = new StreamSource(new ByteArrayInputStream(content));
        return unmarshaller.unmarshal(src, Book.class);
    }
}
