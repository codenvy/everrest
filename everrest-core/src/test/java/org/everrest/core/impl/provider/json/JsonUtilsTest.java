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
package org.everrest.core.impl.provider.json;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.everrest.core.impl.provider.json.JsonUtils.Types;
import org.everrest.core.impl.provider.json.tst.BeanWithTransientField;
import org.everrest.core.impl.provider.json.tst.Book;
import org.everrest.core.impl.provider.json.tst.BookEnum;
import org.everrest.core.impl.provider.json.tst.IBook;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(DataProviderRunner.class)
public class JsonUtilsTest {
    @DataProvider
    public static Object[][] createJsonStringData() {
        return new Object[][]{
                {"string", "\"string\""},
                {"s\ntring\n", "\"s\\ntring\\n\""},
                {"s\tring", "\"s\\tring\""},
                {"st\ring", "\"st\\ring\""},
                {"str\\ing", "\"str\\\\ing\""},
                {"stri\"ng", "\"stri\\\"ng\""},
                {"stri\bng", "\"stri\\bng\""},
                {"stri\fng", "\"stri\\fng\""},
                {"stri/ng", "\"stri/ng\""},
                {"", "\"\""},
                {null, "\"\""}
        };
    }

    @UseDataProvider("createJsonStringData")
    @Test
    public void createsJsonString(String sourceString, String expectedJsonString) {
        assertEquals(expectedJsonString, JsonUtils.getJsonString(sourceString));
    }
    
    @DataProvider
    public static Object[][] knownTypesByClassData() {
        return new Object[][]{
                {Boolean.class,   Types.BOOLEAN},
                {Byte.class,      Types.BYTE},
                {Short.class,     Types.SHORT},
                {Integer.class,   Types.INT},
                {Long.class,      Types.LONG},
                {Float.class,     Types.FLOAT},
                {Double.class,    Types.DOUBLE},
                {Character.class, Types.CHAR},
                {String.class,    Types.STRING},
                {Class.class,     Types.CLASS},
                {boolean.class,   Types.BOOLEAN},
                {byte.class,      Types.BYTE},
                {short.class,     Types.SHORT},
                {int.class,       Types.INT},
                {long.class,      Types.LONG},
                {float.class,     Types.FLOAT},
                {double.class,    Types.DOUBLE},
                {char.class,      Types.CHAR},
                {boolean[].class, Types.ARRAY_BOOLEAN},
                {byte[].class,    Types.ARRAY_BYTE},
                {short[].class,   Types.ARRAY_SHORT},
                {int[].class,     Types.ARRAY_INT},
                {long[].class,    Types.ARRAY_LONG},
                {double[].class,  Types.ARRAY_DOUBLE},
                {float[].class,   Types.ARRAY_FLOAT},
                {char[].class,    Types.ARRAY_CHAR},
                {String[].class,  Types.ARRAY_STRING},
                {Enum.class,      Types.ENUM},
                {Object[].class,  Types.ARRAY_OBJECT},
                {List.class,      Types.COLLECTION},
                {Map.class,       Types.MAP}
                };
    }

    @UseDataProvider("knownTypesByClassData")
    @Test
    public void resolvesKnownTypesByClass(Class<?> aClass, Types expectedType) {
        assertEquals(expectedType, JsonUtils.getType(aClass));
    }

    @DataProvider
    public static Object[][] knownTypesByInstanceData() {
        return new Object[][]{
                {Boolean.TRUE,           Types.BOOLEAN},
                {Byte.valueOf("7"),      Types.BYTE},
                {Short.valueOf("7"),     Types.SHORT},
                {Integer.valueOf("7"),   Types.INT},
                {Long.valueOf("7"),      Types.LONG},
                {Float.valueOf("7.7"),   Types.FLOAT},
                {Double.valueOf("7.7"),  Types.DOUBLE},
                {Character.valueOf('a'), Types.CHAR},
                {"string",               Types.STRING},
                {true,                   Types.BOOLEAN},
                {(byte)7,                Types.BYTE},
                {(short)7,               Types.SHORT},
                {7,                      Types.INT},
                {7L,                     Types.LONG},
                {7.7F,                   Types.FLOAT},
                {7.7,                    Types.DOUBLE},
                {'a',                    Types.CHAR},
                {new boolean[0],         Types.ARRAY_BOOLEAN},
                {new byte[0],            Types.ARRAY_BYTE},
                {new short[0],           Types.ARRAY_SHORT},
                {new int[0],             Types.ARRAY_INT},
                {new long[0],            Types.ARRAY_LONG},
                {new double[0],          Types.ARRAY_DOUBLE},
                {new float[0],           Types.ARRAY_FLOAT},
                {new char[0],            Types.ARRAY_CHAR},
                {new String[0],          Types.ARRAY_STRING},
                {BookEnum.BEGINNING_C,   Types.ENUM},
                {new Object[0],          Types.ARRAY_OBJECT},
                {new ArrayList<>(),      Types.COLLECTION},
                {new HashMap<>(),        Types.MAP},
                {null,                   Types.NULL}
        };
    }

    @UseDataProvider("knownTypesByInstanceData")
    @Test
    public void resolvesKnownTypesByInstance(Object instance, Types expectedType) {
        assertEquals(expectedType, JsonUtils.getType(instance));
    }

    @Test
    public void getsListOfTransientFieldsFromClass() {
        Set<String> transientFields = JsonUtils.getTransientFields(BeanWithTransientField.class);
        transientFields = filterFieldsInsertedByJacocoFrameworkDuringInstrumentation(transientFields);
        assertEquals(newHashSet("transientField", "jsonTransientField"), transientFields);
    }

    @Test
    public void getsListOfTransientFieldsFromSuperClass() {
        Set<String> transientFields = JsonUtils.getTransientFields(ExtensionOfBeanWithTransientField.class);
        transientFields = filterFieldsInsertedByJacocoFrameworkDuringInstrumentation(transientFields);
        assertEquals(newHashSet("transientField", "jsonTransientField"), transientFields);
    }

    public static class ExtensionOfBeanWithTransientField extends BeanWithTransientField {}

    @Test
    public void createsProxyObjectForInterface() {
        IBook book = JsonUtils.createProxy(IBook.class);
        assertNotNull(book);

        assertEquals(null, book.getAuthor());
        assertEquals(null, book.getTitle());
        assertEquals(0, book.getPages());
        assertEquals(0.0, book.getPrice(), 0.01);
        assertEquals(0L, book.getIsdn());
        assertEquals(false, book.getAvailability());
        assertEquals(false, book.getDelivery());

        book.setAuthor("Vincent Massol");
        book.setTitle("JUnit in Action");
        book.setPages(386);
        book.setPrice(19.37);
        book.setIsdn(93011099534534L);
        book.setAvailability(true);
        book.setDelivery(false);

        assertEquals("Vincent Massol", book.getAuthor());
        assertEquals("JUnit in Action", book.getTitle());
        assertEquals(386, book.getPages());
        assertEquals(19.37, book.getPrice(), 0.01);
        assertEquals(93011099534534L, book.getIsdn());
        assertEquals(true, book.getAvailability());
        assertEquals(false, book.getDelivery());

        assertNotNull(book.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsCreateProxyObjectForNull() {
        JsonUtils.createProxy(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsCreateProxyObjectForNonInterface() {
        JsonUtils.createProxy(Book.class);
    }

    private Set<String> filterFieldsInsertedByJacocoFrameworkDuringInstrumentation(Set<String> initialSet) {
        return initialSet.stream().filter(field -> !field.startsWith("$jacocoData")).collect(toSet());
    }
}
