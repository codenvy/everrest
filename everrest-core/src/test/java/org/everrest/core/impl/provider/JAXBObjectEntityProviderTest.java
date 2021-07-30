/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl.provider;

import org.everrest.core.generated.Book;
import org.everrest.core.generated.MemberPrice;
import org.everrest.core.generated.Price;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked"})
public class JAXBObjectEntityProviderTest {
    private static final String TEST_XML_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                                                   + "<book send-by-post=\"true\">"
                                                   + "  <title>Java and XML Data Binding</title>"
                                                   + "  <author>Brett McLaughlin</author>"
                                                   + "  <price currency=\"USD\">34.95</price>"
                                                   + "  <member-price currency=\"USD\">26.56</member-price>"
                                                   + "</book>";

    private static final byte[] TEST_XML_CONTENT_BYTES = TEST_XML_CONTENT.getBytes();

    private MessageBodyReader reader;
    private MessageBodyWriter writer;

    @Before
    public void setUp() throws Exception {
        JAXBContextResolver jaxbContextResolver = mock(JAXBContextResolver.class);
        when(jaxbContextResolver.getContext(any())).thenReturn(jaxbContextResolver);
        when(jaxbContextResolver.getJAXBContext(Book.class)).thenReturn(JAXBContext.newInstance(Book.class));

        Providers providers = mock(Providers.class);
        when(providers.getContextResolver(JAXBContextResolver.class, APPLICATION_XML_TYPE)).thenReturn(jaxbContextResolver);

        reader = new JAXBObjectEntityProvider(providers);
        writer = new JAXBObjectEntityProvider(providers);
    }

    @Test
    public void isReadableForTypeAnnotatedWithXmlRootElement() throws Exception {
        assertTrue(reader.isReadable(Book.class, null, null, null));
    }

    @Test
    public void isNotReadableForTypeNotAnnotatedWithXmlRootElement() throws Exception {
        assertFalse(reader.isReadable(String.class, null, null, null));
    }

    @Test
    public void isWriteableForTypeAnnotatedWithXmlRootElement() throws Exception {
        assertTrue(writer.isWriteable(Book.class, null, null, null));
    }

    @Test
    public void isNotWriteableForTypeNotAnnotatedWithXmlRootElement() throws Exception {
        assertFalse(writer.isWriteable(String.class, null, null, null));
    }

    @Test
    public void readsObjectFromEntityStream() throws Exception {
        Book book = (Book)reader.readFrom(Book.class, null, null, APPLICATION_XML_TYPE, new MultivaluedMapImpl(), new ByteArrayInputStream(TEST_XML_CONTENT_BYTES));

        assertEquals("Java and XML Data Binding", book.getTitle());
        assertTrue(book.isSendByPost());
        assertEquals("Brett McLaughlin", book.getAuthor());
        assertEquals("USD", book.getPrice().getCurrency());
        assertEquals(new BigDecimal("34.95"), book.getPrice().getValue());
        assertEquals("USD", book.getMemberPrice().getCurrency());
        assertEquals(new BigDecimal("26.56"), book.getMemberPrice().getValue());
    }

    @Test
    public void writesObjectToOutputStream() throws Exception {
        Book book = createBook();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeTo(book, Book.class, Book.class, null, APPLICATION_XML_TYPE, null, out);

        Book unmarshaledBook = unmarshalBook(out.toByteArray());
        assertEquals(book.getTitle(), unmarshaledBook.getTitle());
        assertEquals(book.isSendByPost(), unmarshaledBook.isSendByPost());
        assertEquals(book.getAuthor(), unmarshaledBook.getAuthor());
        assertEquals(book.getPrice().getCurrency(), unmarshaledBook.getPrice().getCurrency());
        assertEquals(book.getPrice().getValue(), unmarshaledBook.getPrice().getValue());
        assertEquals(book.getMemberPrice().getCurrency(), unmarshaledBook.getMemberPrice().getCurrency());
        assertEquals(book.getMemberPrice().getValue(), unmarshaledBook.getMemberPrice().getValue());
    }

    private Book createBook() {
        Book book = new Book();
        book.setAuthor("William Shakespeare");
        book.setTitle("Hamlet");
        book.setPrice(createPrice("EUR", "15.15"));
        book.setMemberPrice(createMemberPrice("EUR", "14.73"));
        book.setSendByPost(false);
        return book;
    }

    private Price createPrice(String currency, String amount) {
        Price price = new Price();
        price.setCurrency(currency);
        price.setValue(new BigDecimal(amount));
        return price;
    }

    private MemberPrice createMemberPrice(String currency, String amount) {
        MemberPrice memberPrice = new MemberPrice();
        memberPrice.setCurrency(currency);
        memberPrice.setValue(new BigDecimal(amount));
        return memberPrice;
    }

    private Book unmarshalBook(byte[] content) throws JAXBException {
        Unmarshaller unmarshaller = JAXBContext.newInstance(Book.class).createUnmarshaller();
        Source src = new StreamSource(new ByteArrayInputStream(content));
        return (Book)unmarshaller.unmarshal(src);
    }
}