/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl.provider.json;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyObject;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;

import org.everrest.core.impl.provider.json.tst.Book;
import org.everrest.core.impl.provider.json.tst.BookCollections;
import org.everrest.core.impl.provider.json.tst.BookEnum;
import org.everrest.core.impl.provider.json.tst.IBook;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.everrest.core.impl.provider.json.tst.Book.createCSharpBook;
import static org.everrest.core.impl.provider.json.tst.Book.createJavaScriptBook;
import static org.everrest.core.impl.provider.json.tst.Book.createJunitBook;
import static org.everrest.core.impl.provider.json.tst.BookCollections.createBookCollections;
import static org.everrest.core.impl.provider.json.tst.BookEnum.ADVANCED_JAVA_SCRIPT;
import static org.everrest.core.impl.provider.json.tst.BookEnum.BEGINNING_C;
import static org.everrest.core.impl.provider.json.tst.BookEnum.JUNIT_IN_ACTION;
import static org.everrest.core.util.ParameterizedTypeImpl.newParameterizedType;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ObjectBuilderTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void returnsNullWhenCreateObjectFromNull() throws Exception {
        assertNull(ObjectBuilder.createObject(Book.class, null));
    }

    @Test
    public void returnsNullWhenCreateObjectFromNullValue() throws Exception {
        assertNull(ObjectBuilder.createObject(Book.class, new NullValue()));
    }

    @Test
    public void createsObject() throws Exception {
        ObjectValue jsonBook = createJsonBook(createJunitBook());
        Book book = ObjectBuilder.createObject(Book.class, jsonBook);
        assertEquals(createJunitBook(), book);
    }

    @Test
    public void createsEnum() throws Exception {
        StringValue enumName = new StringValue(JUNIT_IN_ACTION.name());
        BookEnum book = ObjectBuilder.createObject(BookEnum.class, enumName);
        assertEquals(JUNIT_IN_ACTION, book);
    }

    @Test
    public void failsCreateObjectWhenJsonValueIsNotObject() throws Exception {
        thrown.expect(JsonException.class);
        ObjectBuilder.createObject(Book.class, new StringValue(""));
    }

    @Test
    public void failsCreateObjectWhenClassDoesNotHaveSimpleConstructor() throws Exception {
        thrown.expect(JsonException.class);
        ObjectValue jsonBook = createJsonBook(createJunitBook());
        ObjectBuilder.createObject(NoSimpleConstructorBook.class, jsonBook);
    }

    public static class NoSimpleConstructorBook extends Book {
        public NoSimpleConstructorBook(String dummy) {}
    }

    @Test
    public void createsProxyObjectForInterface() throws Exception {
        ObjectValue jsonBook = createJsonBook(createJunitBook());
        IBook book = ObjectBuilder.createObject(IBook.class, jsonBook);

        assertEquals("Vincent Massol", book.getAuthor());
        assertEquals("JUnit in Action", book.getTitle());
        assertEquals(386, book.getPages());
        assertEquals(19.37, book.getPrice(), 0.01);
        assertEquals(93011099534534L, book.getIsdn());
        assertEquals(false, book.getAvailability());
        assertEquals(false, book.getDelivery());
    }

    @Test
    public void throwsJsonExceptionWhenConstructorThrowsExceptionWhileInstanceOfClassCreated() throws Exception {
        thrown.expect(JsonException.class);
        ObjectValue jsonBook = createJsonBook(createJunitBook());
        ObjectBuilder.createObject(ThrowsExceptionInConstructorBook.class, jsonBook);
    }

    public static class ThrowsExceptionInConstructorBook extends Book {
        public ThrowsExceptionInConstructorBook() {throw new RuntimeException();}
    }

    @Test
    public void returnsNullWhenCreateArrayFromNull() throws Exception {
        Object array = ObjectBuilder.createArray(String[].class, null);
        assertNull(array);
    }

    @Test
    public void returnsNullWhenCreateArrayFromNullValue() throws Exception {
        Object array = ObjectBuilder.createArray(String[].class, new NullValue());
        assertNull(array);
    }

    @Test
    public void createsArrayOfStrings() throws Exception {
        ArrayValue jsonArray = createJsonArray("to", "be", "or", "not", "to", "be");
        Object array = ObjectBuilder.createArray(String[].class, jsonArray);
        assertArrayEquals(new String[]{"to", "be", "or", "not", "to", "be"}, (Object[])array);
    }

    @Test
    public void createsArrayOfObjects() throws Exception {
        ArrayValue jsonArray = createJsonArray(createJsonBook(createJunitBook()),
                                               createJsonBook(createCSharpBook())
                                              );
        Object array = ObjectBuilder.createArray(Book[].class, jsonArray);
        assertArrayEquals(new Object[]{createJunitBook(), createCSharpBook()}, (Object[])array);
    }

    @Test
    public void createsMultiDimensionArrayOfStrings() throws Exception {
        ArrayValue jsonArray = new ArrayValue();
        jsonArray.addElement(createJsonArray("to", "be", "or"));
        jsonArray.addElement(createJsonArray("not", "to", "be"));
        Object array = ObjectBuilder.createArray(String[][].class, jsonArray);
        assertArrayEquals(new String[][]{{"to", "be", "or"}, {"not", "to", "be"}}, (Object[][])array);
    }

    @Test
    public void createsMultiDimensionArrayOfObjects() throws Exception {
        ArrayValue jsonArray = new ArrayValue();
        ArrayValue childArray1 = createJsonArray(
                createJsonBook(createJunitBook()),
                createJsonBook(createCSharpBook()));
        ArrayValue childArray2 = createJsonArray(createJsonBook(createJavaScriptBook()));
        jsonArray.addElement(childArray1);
        jsonArray.addElement(childArray2);

        Object array = ObjectBuilder.createArray(Book[][].class, jsonArray);
        assertArrayEquals(new Book[][]{{createJunitBook(), createCSharpBook()}, {createJavaScriptBook()}}, (Object[][])array);
    }

    @Test
    public void returnsNullWhenCreateCollectionFromNull() throws Exception {
        Object array = ObjectBuilder.createCollection(List.class, newParameterizedType(List.class, String.class), null);
        assertNull(array);
    }

    @Test
    public void returnsNullWhenCreateCollectionFromNullValue() throws Exception {
        Object array = ObjectBuilder.createCollection(List.class, newParameterizedType(List.class, String.class), new NullValue());
        assertNull(array);
    }

    @Test
    public void createsListOfStrings() throws Exception {
        ArrayValue jsonArray = createJsonArray("to", "be", "or", "not", "to", "be");
        List list = ObjectBuilder.createCollection(List.class, newParameterizedType(List.class, String.class), jsonArray);
        assertEquals(newArrayList("to", "be", "or", "not", "to", "be"), list);
    }

    @Test
    public void createsListOfObjects() throws Exception {
        ArrayValue jsonArray = createJsonArray(createJsonBook(createJunitBook()),
                                               createJsonBook(createCSharpBook())
                                              );
        List list = ObjectBuilder.createCollection(List.class, newParameterizedType(List.class, Book.class), jsonArray);
        assertEquals(newArrayList(createJunitBook(), createCSharpBook()), list);
    }

    @Test
    public void createsSetOfStrings() throws Exception {
        ArrayValue jsonArray = createJsonArray("to", "be", "or", "not", "to", "be");
        Set set = ObjectBuilder.createCollection(Set.class, newParameterizedType(Set.class, String.class), jsonArray);
        assertEquals(newHashSet("to", "be", "or", "not", "to", "be"), set);
    }

    @Test
    public void createsSetOfObjects() throws Exception {
        ArrayValue jsonArray = createJsonArray(createJsonBook(createJunitBook()),
                                               createJsonBook(createCSharpBook())
                                              );
        Set set = ObjectBuilder.createCollection(Set.class, newParameterizedType(Set.class, Book.class), jsonArray);
        assertEquals(newHashSet(createJunitBook(), createCSharpBook()), set);
    }

    @Test
    public void createsArrayListOfStrings() throws Exception {
        ArrayValue jsonArray = createJsonArray("to", "be", "or", "not", "to", "be");
        ArrayList arrayList = ObjectBuilder.createCollection(ArrayList.class, newParameterizedType(ArrayList.class, String.class), jsonArray);
        assertEquals(newArrayList("to", "be", "or", "not", "to", "be"), arrayList);
    }

    @Test
    public void createsListOfListOfStrings() throws Exception {
        ArrayValue jsonArray = new ArrayValue();
        jsonArray.addElement(createJsonArray("to", "be", "or"));
        jsonArray.addElement(createJsonArray("not", "to", "be"));
        List listOfList = ObjectBuilder.createCollection(List.class,
                                                         newParameterizedType(List.class, newParameterizedType(List.class, String.class)),
                                                         jsonArray);
        assertEquals(newArrayList(newArrayList("to", "be", "or"), newArrayList("not", "to", "be")), listOfList);
    }

    @Test
    public void failsCreateCollectionOfRawType() throws Exception {
        ArrayValue jsonArray = createJsonArray("to", "be", "or", "not", "to", "be");
        thrown.expect(JsonException.class);
        ObjectBuilder.createCollection(List.class, List.class, jsonArray);
    }

    @Test
    public void createsSetOfEnums() throws Exception {
        ArrayValue jsonArray = createJsonArray(JUNIT_IN_ACTION.name(), BEGINNING_C.name());
        Set set = ObjectBuilder.createCollection(Set.class, newParameterizedType(Set.class, BookEnum.class), jsonArray);
        assertEquals(newHashSet(JUNIT_IN_ACTION, BEGINNING_C), set);
    }

    @Test
    public void createsListOfMaps() throws Exception {
        ObjectValue jsonObject1 = createJsonObject("1", createJsonBook(createJavaScriptBook()));
        ObjectValue jsonObject2 = createJsonObject("1", createJsonBook(createJunitBook()),
                                                   "2", createJsonBook(createCSharpBook())
                                                  );
        ArrayValue jsonArray = createJsonArray(jsonObject1, jsonObject2);

        List list = ObjectBuilder.createCollection(List.class,
                                                   newParameterizedType(List.class, newParameterizedType(Map.class, String.class, Book.class)),
                                                   jsonArray);

        assertEquals(newArrayList(ImmutableMap.of("1", createJavaScriptBook()), ImmutableMap.of("1", createJunitBook(), "2", createCSharpBook())),
                     list);
    }

    @Test
    public void createsListOfArrays() throws Exception {
        ArrayValue childArray1 = createJsonArray(createJsonBook(createJavaScriptBook()));
        ArrayValue childArray2 = createJsonArray(createJsonBook(createJunitBook()), createJsonBook(createCSharpBook()));
        ArrayValue jsonArray = createJsonArray(childArray1, childArray2);

        List list = ObjectBuilder.createCollection(List.class, newParameterizedType(List.class, Book[].class), jsonArray);
        assertEquals(2, list.size());
        assertArrayEquals(new Book[]{createJavaScriptBook()}, (Object[])list.get(0));
        assertArrayEquals(new Book[]{createJunitBook(), createCSharpBook()}, (Object[])list.get(1));
    }

    @Test
    public void createsMapOfStringToString() throws Exception {
        ObjectValue jsonObject = new ObjectValue();
        jsonObject.addElement("1", new StringValue("to be or"));
        jsonObject.addElement("2", new StringValue("not to be"));
        Map map = ObjectBuilder.createObject(Map.class, newParameterizedType(Map.class, String.class, String.class), jsonObject);

        assertEquals(ImmutableMap.of("1", "to be or", "2", "not to be"), map);
    }

    @Test
    public void createsObjectWithChildCollections() throws Exception {
        ObjectValue jsonObject = givenJsonObjectThatContainsCollections();

        BookCollections bookCollections = ObjectBuilder.createObject(BookCollections.class, jsonObject);

        assertEquals(createBookCollections(), bookCollections);
    }

    private ObjectValue givenJsonObjectThatContainsCollections() {
        ObjectValue jsonObject = new ObjectValue();
        ArrayValue list = createJsonArray(createJsonBook(createJunitBook()), createJsonBook(createCSharpBook()), createJsonBook(createJavaScriptBook()));
        jsonObject.addElement("list", list);

        ArrayValue listList = createJsonArray(
                createJsonArray(createJsonBook(createJunitBook()), createJsonBook(createJavaScriptBook())),
                createJsonArray(createJsonBook(createCSharpBook()), createJsonBook(createJavaScriptBook())),
                createJsonArray(createJsonBook(createJunitBook()), createJsonBook(createCSharpBook())));
        jsonObject.addElement("listList", listList);

        ArrayValue listMap = createJsonArray(
                createJsonObject("1", createJsonBook(createJunitBook()), "3", createJsonBook(createJavaScriptBook())),
                createJsonObject("2", createJsonBook(createCSharpBook()), "3", createJsonBook(createJavaScriptBook())),
                createJsonObject("1", createJsonBook(createJunitBook()), "2", createJsonBook(createCSharpBook())));
        jsonObject.addElement("listMap", listMap);

        ArrayValue listArray = createJsonArray(
                createJsonArray(createJsonBook(createJunitBook()), createJsonBook(createJavaScriptBook())),
                createJsonArray(createJsonBook(createJunitBook()), createJsonBook(createCSharpBook())),
                createJsonArray(createJsonBook(createCSharpBook()), createJsonBook(createJavaScriptBook())));
        jsonObject.addElement("listArray", listArray);

        ArrayValue set = createJsonArray(createJsonBook(createJunitBook()), createJsonBook(createCSharpBook()), createJsonBook(createJavaScriptBook()));
        jsonObject.addElement("set", set);

        ArrayValue setSet = createJsonArray(
                createJsonArray(createJsonBook(createJunitBook()), createJsonBook(createJavaScriptBook())),
                createJsonArray(createJsonBook(createCSharpBook()), createJsonBook(createJavaScriptBook())),
                createJsonArray(createJsonBook(createJunitBook()), createJsonBook(createCSharpBook())));
        jsonObject.addElement("setSet", setSet);

        ArrayValue setMap = createJsonArray(
                createJsonObject("1", createJsonBook(createJunitBook()), "3", createJsonBook(createJavaScriptBook())),
                createJsonObject("2", createJsonBook(createCSharpBook()), "3", createJsonBook(createJavaScriptBook())),
                createJsonObject("1", createJsonBook(createJunitBook()), "2", createJsonBook(createCSharpBook())));
        jsonObject.addElement("setMap", setMap);

        ArrayValue setArray = createJsonArray(
                createJsonArray(createJsonBook(createJunitBook()), createJsonBook(createJavaScriptBook())),
                createJsonArray(createJsonBook(createJunitBook()), createJsonBook(createCSharpBook())),
                createJsonArray(createJsonBook(createCSharpBook()), createJsonBook(createJavaScriptBook())));
        jsonObject.addElement("setArray", setArray);

        ObjectValue map = createJsonObject("1", createJsonBook(createJunitBook()),
                                           "2", createJsonBook(createCSharpBook()),
                                           "3", createJsonBook(createJavaScriptBook()));
        jsonObject.addElement("map", map);

        ObjectValue mapMap = createJsonObject("1", createJsonObject("1", createJsonBook(createJunitBook()), "2", createJsonBook(createCSharpBook())),
                                              "2", createJsonObject("1", createJsonBook(createJunitBook()), "3", createJsonBook(createJavaScriptBook())),
                                              "3", createJsonObject("2", createJsonBook(createCSharpBook()), "3", createJsonBook(createJavaScriptBook())));
        jsonObject.addElement("mapMap", mapMap);

        ObjectValue mapList = createJsonObject("1", createJsonArray(createJsonBook(createJunitBook()), createJsonBook(createJavaScriptBook())),
                                               "2", createJsonArray(createJsonBook(createJunitBook()), createJsonBook(createCSharpBook())),
                                               "3", createJsonArray(createJsonBook(createCSharpBook()), createJsonBook(createJavaScriptBook())));
        jsonObject.addElement("mapList", mapList);

        ObjectValue mapArray = createJsonObject("1", createJsonArray(createJsonBook(createJunitBook()), createJsonBook(createJavaScriptBook())),
                                                "2", createJsonArray(createJsonBook(createJunitBook()), createJsonBook(createCSharpBook())),
                                                "3", createJsonArray(createJsonBook(createCSharpBook()), createJsonBook(createJavaScriptBook())));
        jsonObject.addElement("mapArray", mapArray);

        jsonObject.addElement("listEnum", createJsonArray(JUNIT_IN_ACTION.name(), BEGINNING_C.name(), ADVANCED_JAVA_SCRIPT.name()));
        return jsonObject;
    }

    @Test
    public void createsSimpleGroovyBean() throws Exception {
        Class<?> aClass = parseGroovyClass("SimpleBean.groovy");
        JsonValue jsonObject = createJsonObject("value", new StringValue("test restore groovy bean"));
        GroovyObject simpleBean = (GroovyObject)ObjectBuilder.createObject(aClass, jsonObject);
        assertEquals("test restore groovy bean", simpleBean.invokeMethod("getValue", new Object[0]));
    }

    @Test
    public void createsComplexGroovyBean() throws Exception {
        Class<?> aBookClass = parseGroovyClass("BookBean.groovy");
        Class<?> aStorageClass = parseGroovyClass("BookStorage.groovy");
        GroovyObject junitBook = (GroovyObject)aBookClass.getDeclaredMethod("createJunitBook").invoke(null);
        GroovyObject cSharpBook = (GroovyObject)aBookClass.getDeclaredMethod("createCSharpBook").invoke(null);
        GroovyObject javaScriptBook = (GroovyObject)aBookClass.getDeclaredMethod("createJavaScriptBook").invoke(null);
        JsonValue jsonObject = createJsonObject("books",
                                                createJsonArray(createJsonBook(junitBook), createJsonBook(cSharpBook), createJsonBook(javaScriptBook)));

        GroovyObject groovyObject = (GroovyObject)ObjectBuilder.createObject(aStorageClass, jsonObject);

        assertEquals(aStorageClass.getDeclaredMethod("createBookStorage").invoke(null), groovyObject);
    }


    private GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

    private Class<?> parseGroovyClass(String fileName) throws IOException {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
             Reader reader = new InputStreamReader(stream)) {
            String script = CharStreams.toString(reader);
            return groovyClassLoader.parseClass(new GroovyCodeSource(script, fileName, "groovy/script"));
        }
    }

    private ArrayValue createJsonArray(String... values) {
        ArrayValue jsonArray = new ArrayValue();
        for (String value : values) {
            jsonArray.addElement(new StringValue(value));
        }
        return jsonArray;
    }

    private ArrayValue createJsonArray(JsonValue... values) {
        ArrayValue jsonArray = new ArrayValue();
        for (JsonValue value : values) {
            jsonArray.addElement(value);
        }
        return jsonArray;
    }

    private ObjectValue createJsonObject(String name, JsonValue value) {
        ObjectValue jsonObject = new ObjectValue();
        jsonObject.addElement(name, value);
        return jsonObject;
    }

    private ObjectValue createJsonObject(String name1, JsonValue value1, String name2, JsonValue value2) {
        ObjectValue jsonObject = new ObjectValue();
        jsonObject.addElement(name1, value1);
        jsonObject.addElement(name2, value2);
        return jsonObject;
    }

    private ObjectValue createJsonObject(String name1, JsonValue value1, String name2, JsonValue value2, String name3, JsonValue value3) {
        ObjectValue jsonObject = createJsonObject(name1, value1, name2, value2);
        jsonObject.addElement(name3, value3);
        return jsonObject;
    }

    private ObjectValue createJsonBook(Book book) {
        ObjectValue objectValue = new ObjectValue();
        objectValue.addElement("author", new StringValue(book.getAuthor()));
        objectValue.addElement("title", new StringValue(book.getTitle()));
        objectValue.addElement("pages", new LongValue(book.getPages()));
        objectValue.addElement("isdn", new LongValue(book.getIsdn()));
        objectValue.addElement("price", new DoubleValue(book.getPrice()));
        objectValue.addElement("availability", new BooleanValue(book.getAvailability()));
        objectValue.addElement("delivery", new BooleanValue(book.getDelivery()));
        return objectValue;
    }

    private ObjectValue createJsonBook(GroovyObject book) {
        ObjectValue objectValue = new ObjectValue();
        objectValue.addElement("author", new StringValue((String)book.getProperty("author")));
        objectValue.addElement("title", new StringValue((String)book.getProperty("title")));
        objectValue.addElement("pages", new LongValue((Integer)book.getProperty("pages")));
        objectValue.addElement("isdn", new LongValue((Long)book.getProperty("isdn")));
        objectValue.addElement("price", new DoubleValue((Double)book.getProperty("price")));
        objectValue.addElement("availability", new BooleanValue((Boolean)book.getProperty("availability")));
        objectValue.addElement("delivery", new BooleanValue((Boolean)book.getProperty("delivery")));
        return objectValue;
    }
}
