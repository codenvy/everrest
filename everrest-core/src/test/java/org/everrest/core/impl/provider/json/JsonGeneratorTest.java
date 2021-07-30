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
package org.everrest.core.impl.provider.json;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyObject;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.everrest.core.impl.provider.json.tst.BeanWithClassField;
import org.everrest.core.impl.provider.json.tst.BeanWithEnums;
import org.everrest.core.impl.provider.json.tst.BeanWithPrimitiveFields;
import org.everrest.core.impl.provider.json.tst.BeanWithTransientField;
import org.everrest.core.impl.provider.json.tst.Book;
import org.everrest.core.impl.provider.json.tst.BookArrays;
import org.everrest.core.impl.provider.json.tst.BookCollections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.everrest.core.impl.provider.json.tst.BeanWithClassField.createBeanWithClassField;
import static org.everrest.core.impl.provider.json.tst.BeanWithEnums.createBeanWithEnums;
import static org.everrest.core.impl.provider.json.tst.BeanWithPrimitiveFields.createBeanWithPrimitiveFields;
import static org.everrest.core.impl.provider.json.tst.Book.createCSharpBook;
import static org.everrest.core.impl.provider.json.tst.Book.createJavaScriptBook;
import static org.everrest.core.impl.provider.json.tst.Book.createJunitBook;
import static org.everrest.core.impl.provider.json.tst.BookArrays.createBookArrays;
import static org.everrest.core.impl.provider.json.tst.BookCollections.createBookCollections;
import static org.everrest.core.impl.provider.json.tst.BookWrapperOne.createBookWrapperOne;
import static org.everrest.core.impl.provider.json.tst.BookWrapperThree.createBookWrapperThree;
import static org.everrest.core.impl.provider.json.tst.BookWrapperTwo.createBookWrapperTwo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(DataProviderRunner.class)
public class JsonGeneratorTest {
    private Book junitBook;
    private Book csharpBook;
    private Book javaScriptBook;

    private JsonAssertionHelper assertionHelper;

    @Before
    public void setUp() throws Exception {
        junitBook = createJunitBook();
        csharpBook = createCSharpBook();
        javaScriptBook = createJavaScriptBook();
        assertionHelper = new JsonAssertionHelper();
    }

    @Test
    public void convertsObjectToJson() throws Exception {
        JsonValue json = JsonGenerator.createJsonObject(junitBook);
        assertTrue(json.isObject());
        assertionHelper.assertThatJsonHasAllFieldsFromObject(junitBook, json);
    }

    @Test
    public void convertsNullObjectToJson() throws Exception {
        JsonValue json = JsonGenerator.createJsonObject(null);
        assertTrue(json.isNull());
    }

    @Test
    public void convertsArrayToJson() throws Exception {
        Book[] books = new Book[]{junitBook, csharpBook, javaScriptBook};
        JsonValue jsonArray = JsonGenerator.createJsonArray(books);
        assertTrue(jsonArray.isArray());
        assertionHelper.assertThatJsonArrayHasAllItemsFromArray(books, jsonArray);
    }

    @Test(expected = JsonException.class)
    public void doesNotAcceptOtherThanArrayArgumentsWhenCreateJsonArray() throws Exception {
        JsonGenerator.createJsonArray(junitBook);
    }

    @Test
    public void convertsNullArrayToJson() throws Exception {
        JsonValue jsonValue = JsonGenerator.createJsonArray((Object)null);
        assertTrue(jsonValue.isNull());
    }

    @Test
    public void convertsListToJson() throws Exception {
        List<Book> books = newArrayList(junitBook, csharpBook, javaScriptBook);
        JsonValue jsonArray = JsonGenerator.createJsonArray(books);
        assertTrue(jsonArray.isArray());
        assertionHelper.assertThatJsonArrayHasAllItemsFromCollection(books, jsonArray);
    }

    @Test
    public void convertsSetToJsonArray() throws Exception {
        Set<Book> books = newHashSet(junitBook, csharpBook, javaScriptBook);
        JsonValue jsonArray = JsonGenerator.createJsonArray(books);
        assertTrue(jsonArray.isArray());
        assertionHelper.assertThatJsonArrayHasAllItemsFromCollection(books, jsonArray);
    }

    @Test
    public void convertsNullCollectionToJson() throws Exception {
        JsonValue jsonValue = JsonGenerator.createJsonArray(null);
        assertTrue(jsonValue.isNull());
    }

    @Test
    public void convertsMapToJson() throws Exception {
        Map<String, Book> books = ImmutableMap.of("0", junitBook,
                                                  "1", csharpBook,
                                                  "2", javaScriptBook);
        JsonValue jsonValue = JsonGenerator.createJsonObjectFromMap(books);
        assertTrue(jsonValue.isObject());
        assertionHelper.assertThatJsonHasAllItemsFromMap(books, jsonValue);
    }

    @Test
    public void convertsNullMapToJson() throws Exception {
        JsonValue jsonValue = JsonGenerator.createJsonObjectFromMap(null);
        assertTrue(jsonValue.isNull());
    }


    @DataProvider
    public static Object[][] complexObjectsForConvertToJson() {
        return new Object[][] {
                {createBookWrapperOne(createJunitBook())},
                {createBookWrapperTwo(createJavaScriptBook())},
                {createBookWrapperThree(createCSharpBook())}
        };
    }

    @UseDataProvider("complexObjectsForConvertToJson")
    @Test
    public void convertsComplexObjectToJson(Object object) throws Exception {
        JsonValue jsonValue = JsonGenerator.createJsonObject(object);
        assertTrue(jsonValue.isObject());

        assertionHelper.assertThatJsonHasAllFieldsFromObject(object, jsonValue);
    }


    @DataProvider
    public static Object[][] collectionsOfComplexObjectsForConvertToJson() {
        return new Object[][] {
                {newArrayList(createBookWrapperOne(createJunitBook()))},
                {newArrayList(createBookWrapperOne(createJunitBook()), createBookWrapperTwo(createJavaScriptBook()))},
                {newArrayList(createBookWrapperTwo(createJavaScriptBook()), createBookWrapperThree(createCSharpBook()))}
        };
    }

    @UseDataProvider("collectionsOfComplexObjectsForConvertToJson")
    @Test
    public void convertsCollectionsOfComplexObjectsToJson(Collection<Object> objects) throws Exception {
        JsonValue jsonValue = JsonGenerator.createJsonArray(objects);
        assertTrue(jsonValue.isArray());

        assertionHelper.assertThatJsonArrayHasAllItemsFromCollection(objects, jsonValue);
    }

    @Test
    public void convertsObjectThatContainsCollectionsOfComplexObjects() throws Exception {
        BookCollections bookCollections = createBookCollections();

        JsonValue jsonValue = JsonGenerator.createJsonObject(bookCollections);
        assertTrue(jsonValue.isObject());

        assertionHelper.assertThatJsonHasAllFieldsFromObject(bookCollections, jsonValue);
    }

    @Test
    public void convertsObjectThatContainsArraysOfComplexObjects() throws Exception {
        BookArrays bookArrays = createBookArrays();

        JsonValue jsonValue = JsonGenerator.createJsonObject(bookArrays);
        assertTrue(jsonValue.isObject());

        assertionHelper.assertThatJsonHasAllFieldsFromObject(bookArrays, jsonValue);
    }

    @Test
    public void ignoresTransientFields() throws Exception {
        BeanWithTransientField bean = new BeanWithTransientField();
        JsonValue jsonValue = JsonGenerator.createJsonObject(bean);
        assertEquals("visible", jsonValue.getElement("field").getStringValue());
        assertNull(jsonValue.getElement("transientField"));
    }

    @Test
    public void ignoresFieldsAnnotatedWithJsonTransient() throws Exception {
        BeanWithTransientField bean = new BeanWithTransientField();
        JsonValue jsonValue = JsonGenerator.createJsonObject(bean);
        assertEquals("visible", jsonValue.getElement("field").getStringValue());
        assertNull(jsonValue.getElement("jsonTransientField"));
    }

    @Test
    public void convertsObjectWithEnumsToJson() throws Exception {
        BeanWithEnums beanWithEnums = createBeanWithEnums();

        JsonValue jsonValue = JsonGenerator.createJsonObject(beanWithEnums);

        assertTrue(jsonValue.isObject());

        assertionHelper.assertThatJsonHasAllFieldsFromObject(beanWithEnums, jsonValue);
    }

    @Test
    public void convertsObjectWithClassFieldToJson() throws Exception {
        BeanWithClassField beanWithClassField = createBeanWithClassField();
        JsonValue jsonValue = JsonGenerator.createJsonObject(beanWithClassField);
        assertEquals(BeanWithClassField.class.getName(), jsonValue.getElement("klass").getStringValue());
    }

    @Test
    public void convertsObjectWithPrimitiveFieldsAndTheirArraysToJson() throws Exception {
        BeanWithPrimitiveFields beanWithPrimitiveFields = createBeanWithPrimitiveFields();
        JsonValue jsonValue = JsonGenerator.createJsonObject(beanWithPrimitiveFields);

        assertionHelper.assertThatJsonHasAllFieldsFromObject(beanWithPrimitiveFields, jsonValue);
    }

    @Test
    public void convertsSimpleGroovyBeanToJson() throws Exception {
        Class<?> aClass = parseGroovyClass("SimpleBean.groovy");
        GroovyObject groovyObject = (GroovyObject)aClass.newInstance();
        groovyObject.invokeMethod("setValue", new Object[]{"test serialize groovy bean"});
        JsonValue jsonValue = JsonGenerator.createJsonObject(groovyObject);

        assertionHelper.assertThatJsonHasAllFieldsFromObject(groovyObject, jsonValue);
    }

    @Test
    public void convertsComplexGroovyBeanToJson() throws Exception {
        Class<?> aClass = parseGroovyClass("BookStorage.groovy");
        Object groovyObject = aClass.getDeclaredMethod("createBookStorage").invoke(null);

        JsonValue jsonValue = JsonGenerator.createJsonObject(groovyObject);

        assertionHelper.assertThatJsonHasAllFieldsFromObject(groovyObject, jsonValue);
    }

    private GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

    private Class<?> parseGroovyClass(String fileName) throws IOException {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
             Reader reader = new InputStreamReader(stream)) {
            String script = CharStreams.toString(reader);
            return groovyClassLoader.parseClass(new GroovyCodeSource(script, fileName, "groovy/script"));
        }
    }
}
