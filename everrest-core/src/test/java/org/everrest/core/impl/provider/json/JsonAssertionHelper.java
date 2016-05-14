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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JsonAssertionHelper {
    public void assertThatJsonArrayHasAllItemsFromArray(Object array, JsonValue json) throws Exception {
        if (array == null) {
            assertTrue(json.isNull());
            return;
        }

        assertTrue(json.isArray());

        int length = Array.getLength(array);
        assertEquals(length, json.size());

        Iterator<JsonValue> jsonIterator = json.getElements();

        for (int i = 0; i < length; i++) {
            Object value = Array.get(array, i);
            JsonValue jsonValue = jsonIterator.next();
            if (value == null) {
                assertNull(jsonValue);
            } else {
                assertNotNull(jsonValue);
                Class<?> type = value.getClass();
                if (isString(type)) {
                    assertEquals(value, jsonValue.getStringValue());
                } else if (type.isEnum()) {
                    assertEquals(((Enum)value).name(), jsonValue.getStringValue());
                } else if (isBoolean(type)) {
                    assertEquals(value, jsonValue.getBooleanValue());
                } else if (isByte(type)) {
                    assertEquals(value, jsonValue.getByteValue());
                } else if (isShort(type)) {
                    assertEquals(value, jsonValue.getShortValue());
                } else if (isInteger(type)) {
                    assertEquals(value, jsonValue.getIntValue());
                } else if (isLong(type)) {
                    assertEquals(value, jsonValue.getLongValue());
                } else if (isFloat(type)) {
                    assertEquals((Float)value, jsonValue.getFloatValue(), 0.01f);
                } else if (isDouble(type)) {
                    assertEquals((Double)value, jsonValue.getDoubleValue(), 0.01);
                } else if (isChar(type)) {
                    assertEquals(String.valueOf(value), jsonValue.getStringValue());
                } else if (type.isArray()) {
                    assertThatJsonArrayHasAllItemsFromArray((Object[])value, jsonValue);
                } else if (isCollection(type)) {
                    assertThatJsonArrayHasAllItemsFromCollection((Collection<?>)value, jsonValue);
                } else if (isMap(type)) {
                    assertThatJsonHasAllItemsFromMap((Map<String, ?>)value, jsonValue);
                } else {
                    assertThatJsonHasAllFieldsFromObject(value, jsonValue);
                }
            }
        }
    }

    public void assertThatJsonArrayHasAllItemsFromCollection(Collection<?> collection, JsonValue json) throws Exception {
        Object array = collection == null ? null : collection.toArray();
        assertThatJsonArrayHasAllItemsFromArray(array, json);
    }

    public void assertThatJsonHasAllItemsFromMap(Map<String, ?> map, JsonValue json) throws Exception {
        if (map == null) {
            assertTrue(json.isNull());
            return;
        }
        assertTrue(json.isObject());
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            Object value = entry.getValue();
            String name = entry.getKey();
            JsonValue jsonValue = json.getElement(name);
            if (value == null) {
                assertNull(name, jsonValue);
            } else {
                assertNotNull(name, jsonValue);
                Class<?> type = value.getClass();
                if (isString(type)) {
                    assertEquals(value, jsonValue.getStringValue());
                } else if (type.isEnum()) {
                    assertEquals(((Enum)value).name(), jsonValue.getStringValue());
                } else if (isBoolean(type)) {
                    assertEquals(value, jsonValue.getBooleanValue());
                } else if (isByte(type)) {
                    assertEquals(value, jsonValue.getByteValue());
                } else if (isShort(type)) {
                    assertEquals(value, jsonValue.getShortValue());
                } else if (isInteger(type)) {
                    assertEquals(value, jsonValue.getIntValue());
                } else if (isLong(type)) {
                    assertEquals(value, jsonValue.getLongValue());
                } else if (isFloat(type)) {
                    assertEquals((Float)value, jsonValue.getFloatValue(), 0.01f);
                } else if (isDouble(type)) {
                    assertEquals((Double)value, jsonValue.getDoubleValue(), 0.01);
                } else if (isChar(type)) {
                    assertEquals(String.valueOf(value), jsonValue.getStringValue());
                } else if (type.isArray()) {
                    assertThatJsonArrayHasAllItemsFromArray((Object[])value, jsonValue);
                } else if (isCollection(type)) {
                    assertThatJsonArrayHasAllItemsFromCollection((Collection<?>)value, jsonValue);
                } else if (isMap(type)) {
                    assertThatJsonHasAllItemsFromMap((Map<String, ?>)value, jsonValue);
                } else {
                    assertThatJsonHasAllFieldsFromObject(value, jsonValue);
                }
            }
        }
    }

    public void assertThatJsonHasAllFieldsFromObject(Object object, JsonValue json) throws Exception {
        if (object == null) {
            assertTrue(json.isNull());
        } else {
            assertTrue(object.toString(), json.isObject());
            for (Field field : getAllFields(object)) {
                field.setAccessible(true);
                String fieldName = field.getName();
                JsonValue jsonField = json.getElement(fieldName);
                assertNotNull(String.format("Expected field %s missed in json", fieldName), jsonField);
                Class<?> type = field.getType();
                Object value = field.get(object);
                if (value == null) {
                    assertTrue(fieldName, jsonField.isNull());
                } else if (isString(type)) {
                    assertEquals(value, jsonField.getStringValue());
                } else if (type.isEnum()) {
                    assertEquals(((Enum)field.get(object)).name(), jsonField.getStringValue());
                } else if (isBoolean(type)) {
                    assertEquals(value, jsonField.getBooleanValue());
                } else if (isByte(type)) {
                    assertEquals(value, jsonField.getByteValue());
                } else if (isShort(type)) {
                    assertEquals(value, jsonField.getShortValue());
                } else if (isInteger(type)) {
                    assertEquals(value, jsonField.getIntValue());
                } else if (isLong(type)) {
                    assertEquals(value, jsonField.getLongValue());
                } else if (isFloat(type)) {
                    assertEquals((Float)value, jsonField.getFloatValue(), 0.01f);
                } else if (isDouble(type)) {
                    assertEquals((Double)value, jsonField.getDoubleValue(), 0.01);
                } else if (isChar(type)) {
                    assertEquals(String.valueOf(value), jsonField.getStringValue());
                } else if (type.isArray()) {
                    assertThatJsonArrayHasAllItemsFromArray(value, jsonField);
                } else if (isCollection(type)) {
                    assertThatJsonArrayHasAllItemsFromCollection((Collection<?>)value, jsonField);
                } else if (isMap(type)) {
                    assertThatJsonHasAllItemsFromMap((Map<String, ?>)value, jsonField);
                } else {
                    assertThatJsonHasAllFieldsFromObject(value, jsonField);
                }
            }
        }
    }

    private boolean isBoolean(Class<?> type) {
        return type == boolean.class || type == Boolean.class;
    }

    private boolean isByte(Class<?> type) {
        return type == byte.class || type == Byte.class;
    }

    private boolean isShort(Class<?> type) {
        return type == short.class || type == Short.class;
    }

    private boolean isInteger(Class<?> type) {
        return type == int.class || type == Integer.class;
    }

    private boolean isLong(Class<?> type) {
        return type == long.class || type == Long.class;
    }

    private boolean isFloat(Class<?> type) {
        return type == float.class || type == Float.class;
    }

    private boolean isDouble(Class<?> type) {
        return type == double.class || type == Double.class;
    }

    private boolean isChar(Class<?> type) {
        return type == char.class || type == Character.class;
    }

    private boolean isString(Class<?> type) {
        return type == String.class;
    }

    private boolean isCollection(Class<?> type) {
        return Collection.class.isAssignableFrom(type);
    }

    private boolean isMap(Class<?> type) {
        return Map.class.isAssignableFrom(type);
    }

    private List<Field> getAllFields(Object object) {
        List<Field> allFields = new ArrayList<>();
        Class<?> aClass = object.getClass();
        while (aClass.getSuperclass() != null) {
            List<Field> classFields = Arrays.stream(aClass.getDeclaredFields())
                                            .filter((field) -> isNotJsonTransient(field) && isNotGroovyMetaClassField(field))
                                            .collect(toList());
            allFields.addAll(classFields);
            aClass = aClass.getSuperclass();
        }
        return allFields;
    }

    private boolean isNotJsonTransient(Field field) {
        return !(Modifier.isTransient(field.getModifiers()) || field.isAnnotationPresent(JsonTransient.class));
    }

    private boolean isNotGroovyMetaClassField(Field field) {
        return !(field.getName().startsWith("$") || field.getName().startsWith("__timeStamp"));
    }
}
