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

import com.google.common.collect.ImmutableMap;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.provider.json.DoubleValue;
import org.everrest.core.impl.provider.json.JsonTransient;
import org.everrest.core.impl.provider.json.JsonValue;
import org.everrest.core.impl.provider.json.LongValue;
import org.everrest.core.impl.provider.json.ObjectValue;
import org.everrest.core.impl.provider.json.StringValue;
import org.everrest.core.impl.provider.json.tst.Book;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.activation.DataSource;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.everrest.core.impl.provider.json.tst.Book.createJunitBook;
import static org.everrest.core.util.ParameterizedTypeImpl.newParameterizedType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({"unchecked"})
@RunWith(DataProviderRunner.class)
public class JsonEntityProviderTest {
    private JsonEntityProvider jsonEntityProvider;

    @Before
    public void setUp() throws Exception {
        jsonEntityProvider = new JsonEntityProvider();
    }

    @Test
    public void isReadableForAnyType() throws Exception {
        assertTrue(jsonEntityProvider.isReadable(Object.class, null, null, null));
    }

    @Test
    public void isWriteableForAnyType() throws Exception {
        assertTrue(jsonEntityProvider.isWriteable(Object.class, null, null, null));
    }

    @DataProvider
    public static Object[][] ignoresTypesData() {
        return new Object[][]{
                {byte[].class},
                {char[].class},
                {DataSource.class},
                {DOMSource.class},
                {File.class},
                {InputStream.class},
                {OutputStream.class},
                {JAXBElement.class},
                {MultivaluedMap.class},
                {Reader.class},
                {Writer.class},
                {SAXSource.class},
                {StreamingOutput.class},
                {StreamSource.class},
                {String.class},
                {JsonTransientAnnotated.class}
        };
    }

    @JsonTransient
    public static class JsonTransientAnnotated {
    }

    @UseDataProvider("ignoresTypesData")
    @Test
    public void isNotReadableForTypesSupportedByOtherProviders(Class<?> aClass) throws Exception {
        assertFalse(jsonEntityProvider.isReadable(aClass, null, null, null));
    }

    @UseDataProvider("ignoresTypesData")
    @Test
    public void isNotWriteableForTypesSupportedByOtherProviders(Class<?> aClass) throws Exception {
        assertFalse(jsonEntityProvider.isWriteable(aClass, null, null, null));
    }

    @Test
    public void readsContentOfEntityStreamAsJsonValue() throws Exception {
        String content = "{\"title\": \"JUnit in Action\", \"pages\": 386, \"isdn\": 93011099534534, \"price\": 19.37, \"author\": \"Vincent Massol\"}";
        JsonValue result = (JsonValue)jsonEntityProvider.readFrom(JsonValue.class, null, null, APPLICATION_JSON_TYPE, new MultivaluedMapImpl(),
                                                                  new ByteArrayInputStream(content.getBytes()));
        assertEquals("Vincent Massol", result.getElement("author").getStringValue());
        assertEquals("JUnit in Action", result.getElement("title").getStringValue());
        assertEquals(386, result.getElement("pages").getIntValue());
        assertEquals(19.37, result.getElement("price").getNumberValue());
        assertEquals(93011099534534L, result.getElement("isdn").getLongValue());
    }

    @Test
    public void readsContentOfEntityStreamAsObject() throws Exception {
        String content = "{\"title\": \"JUnit in Action\", \"pages\": 386, \"isdn\": 93011099534534, \"price\": 19.37, \"author\": \"Vincent Massol\"}";
        Book result = (Book)jsonEntityProvider.readFrom(Book.class, null, null, APPLICATION_JSON_TYPE, new MultivaluedMapImpl(),
                                                        new ByteArrayInputStream(content.getBytes()));
        assertEquals("Vincent Massol", result.getAuthor());
        assertEquals("JUnit in Action", result.getTitle());
        assertEquals(386, result.getPages());
        assertEquals(19.37, result.getPrice(), 0.01);
        assertEquals(93011099534534L, result.getIsdn());
    }

    @Test
    public void readsContentOfEntityStreamAsMapStringToObject() throws Exception {
        String content = "{\"book\": {\"title\": \"JUnit in Action\", \"pages\": 386, \"isdn\": 93011099534534, \"price\": 19.37, \"author\": \"Vincent Massol\"}}";
        Class aClass = Map.class;
        Type genericType = newParameterizedType(aClass, String.class, Book.class);
        Map<String, Book> result = (Map<String, Book>)jsonEntityProvider.readFrom(aClass, genericType, null, APPLICATION_JSON_TYPE, new MultivaluedMapImpl(),
                                                                                  new ByteArrayInputStream(content.getBytes()));
        assertEquals(1, result.size());
        assertNotNull(result.get("book"));
        Book book = result.get("book");
        assertEquals("Vincent Massol", book.getAuthor());
        assertEquals("JUnit in Action", book.getTitle());
        assertEquals(386, book.getPages());
        assertEquals(19.37, book.getPrice(), 0.01);
        assertEquals(93011099534534L, book.getIsdn());
    }

    @Test
    public void readsContentOfEntityStreamAsCollectionOfObjects() throws Exception {
        String content = "[{\"title\": \"JUnit in Action\", \"pages\": 386, \"isdn\": 93011099534534, \"price\": 19.37, \"author\": \"Vincent Massol\"}]";
        Class aClass = List.class;
        Type genericType = newParameterizedType(aClass, Book.class);
        List<Book> result = (List<Book>)jsonEntityProvider.readFrom(aClass, genericType, null, APPLICATION_JSON_TYPE, new MultivaluedMapImpl(),
                                                                    new ByteArrayInputStream(content.getBytes()));
        assertEquals(1, result.size());
        Book book = result.get(0);
        assertEquals("Vincent Massol", book.getAuthor());
        assertEquals("JUnit in Action", book.getTitle());
        assertEquals(386, book.getPages());
        assertEquals(19.37, book.getPrice(), 0.01);
        assertEquals(93011099534534L, book.getIsdn());
    }

    @Test
    public void readsContentOfEntityStreamAsArrayOfObjects() throws Exception {
        String content = "[{\"title\": \"JUnit in Action\", \"pages\": 386, \"isdn\": 93011099534534, \"price\": 19.37, \"author\": \"Vincent Massol\"}]";
        Class aClass = Book[].class;
        Book[] result = (Book[])jsonEntityProvider.readFrom(aClass, null, null, APPLICATION_JSON_TYPE, new MultivaluedMapImpl(),
                                                            new ByteArrayInputStream(content.getBytes()));
        assertEquals(1, result.length);
        Book book = result[0];
        assertEquals("Vincent Massol", book.getAuthor());
        assertEquals("JUnit in Action", book.getTitle());
        assertEquals(386, book.getPages());
        assertEquals(19.37, book.getPrice(), 0.01);
        assertEquals(93011099534534L, book.getIsdn());
    }

    @Test(expected = IOException.class)
    public void throwsIOExceptionWhenParsingFailed() throws Exception {
        String invalidContent = "\"title\": \"JUnit in Action\"}";
        jsonEntityProvider.readFrom(Book.class, null, null, APPLICATION_JSON_TYPE, new MultivaluedMapImpl(),
                                    new ByteArrayInputStream(invalidContent.getBytes()));
    }

    @Test
    public void writesJsonValueToOutputStream() throws Exception {
        ObjectValue jsonBook = createJsonBook(createJunitBook());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        jsonEntityProvider.writeTo(jsonBook, Book.class, null, null, APPLICATION_JSON_TYPE, new MultivaluedHashMap<>(), out);

        assertEquals("{\"author\":\"Vincent Massol\",\"title\":\"JUnit in Action\",\"pages\":386,\"price\":19.37,\"isdn\":93011099534534}", out.toString());
    }

    @Test
    public void writesObjectToOutputStream() throws Exception {
        Book book = createJunitBook();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        jsonEntityProvider.writeTo(book, Book.class, null, null, APPLICATION_JSON_TYPE, new MultivaluedHashMap<>(), out);

        String outputAsString = out.toString();
        assertTrue(outputAsString.contains("\"author\":\"Vincent Massol\""));
        assertTrue(outputAsString.contains("\"title\":\"JUnit in Action\""));
        assertTrue(outputAsString.contains("\"pages\":386"));
        assertTrue(outputAsString.contains("\"price\":19.37"));
        assertTrue(outputAsString.contains("\"isdn\":93011099534534"));
    }

    @Test
    public void writesArrayOfObjectsToOutputStream() throws Exception {
        Book[] books = new Book[]{createJunitBook()};
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        jsonEntityProvider.writeTo(books, Book[].class, null, null, APPLICATION_JSON_TYPE, new MultivaluedHashMap<>(), out);

        String outputAsString = out.toString();
        assertTrue(outputAsString.startsWith("["));
        assertTrue(outputAsString.endsWith("]"));
        assertTrue(outputAsString.contains("\"author\":\"Vincent Massol\""));
        assertTrue(outputAsString.contains("\"title\":\"JUnit in Action\""));
        assertTrue(outputAsString.contains("\"pages\":386"));
        assertTrue(outputAsString.contains("\"price\":19.37"));
        assertTrue(outputAsString.contains("\"isdn\":93011099534534"));
    }

    @Test
    public void writesCollectionOfObjectsToOutputStream() throws Exception {
        List<Book> books = newArrayList(createJunitBook());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        jsonEntityProvider.writeTo(books, List.class, newParameterizedType(List.class, Book.class), null, APPLICATION_JSON_TYPE, new MultivaluedHashMap<>(), out);

        String outputAsString = out.toString();
        assertTrue(outputAsString.startsWith("["));
        assertTrue(outputAsString.endsWith("]"));
        assertTrue(outputAsString.contains("\"author\":\"Vincent Massol\""));
        assertTrue(outputAsString.contains("\"title\":\"JUnit in Action\""));
        assertTrue(outputAsString.contains("\"pages\":386"));
        assertTrue(outputAsString.contains("\"price\":19.37"));
        assertTrue(outputAsString.contains("\"isdn\":93011099534534"));
    }

    @Test
    public void writesMapStringToObjectToOutputStream() throws Exception {
        Map<String, Book> books = ImmutableMap.of("1", createJunitBook());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        jsonEntityProvider.writeTo(books, Map.class, newParameterizedType(Map.class, String.class, Book.class), null, APPLICATION_JSON_TYPE,
                                   new MultivaluedHashMap<>(), out);

        String outputAsString = out.toString();
        assertTrue(outputAsString.startsWith("{\"1\":{"));
        assertTrue(outputAsString.endsWith("}"));
        assertTrue(outputAsString.contains("\"author\":\"Vincent Massol\""));
        assertTrue(outputAsString.contains("\"title\":\"JUnit in Action\""));
        assertTrue(outputAsString.contains("\"pages\":386"));
        assertTrue(outputAsString.contains("\"price\":19.37"));
        assertTrue(outputAsString.contains("\"isdn\":93011099534534"));
    }

    private ObjectValue createJsonBook(Book book) {
        ObjectValue objectValue = new ObjectValue();
        objectValue.addElement("author", new StringValue(book.getAuthor()));
        objectValue.addElement("title", new StringValue(book.getTitle()));
        objectValue.addElement("pages", new LongValue(book.getPages()));
        objectValue.addElement("price", new DoubleValue(book.getPrice()));
        objectValue.addElement("isdn", new LongValue(book.getIsdn()));
        return objectValue;
    }
}
