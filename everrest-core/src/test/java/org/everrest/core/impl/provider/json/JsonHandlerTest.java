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

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked"})
public class JsonHandlerTest {

    private JsonStack jsonStack;
    private JsonValueFactory jsonValueFactory;

    private JsonHandler jsonHandler;

    @Before
    public void setUp() throws Exception {
        jsonStack = mock(JsonStack.class);
        jsonValueFactory = mock(JsonValueFactory.class);
        jsonHandler = new JsonHandler(jsonStack, jsonValueFactory);
    }

    @Test
    public void startsRootObject() throws Exception {
        jsonHandler.startObject();

        ArgumentCaptor<JsonValue> jsonCaptor = ArgumentCaptor.forClass(JsonValue.class);
        verify(jsonStack).push(jsonCaptor.capture());
        assertTrue(jsonCaptor.getValue().isObject());
        assertSame(jsonCaptor.getValue(), jsonHandler.getJsonObject());
    }

    @Test
    public void startsObjectAsChildOfParentObject() throws Exception {
        JsonValue parent = mock(JsonValue.class);
        when(parent.isObject()).thenReturn(true);
        jsonHandler.setJsonObject(parent);

        jsonHandler.key("key1");
        jsonHandler.startObject();

        ArgumentCaptor<JsonValue> jsonCaptor = ArgumentCaptor.forClass(JsonValue.class);
        verify(jsonStack).push(jsonCaptor.capture());
        assertSame(parent, jsonCaptor.getValue());

        verify(parent).addElement(eq("key1"), jsonCaptor.capture());
        assertTrue(jsonCaptor.getValue().isObject());
        assertSame(jsonCaptor.getValue(), jsonHandler.getJsonObject());
    }

    @Test
    public void startsObjectAsItemOfArray() throws Exception {
        JsonValue array = mock(JsonValue.class);
        when(array.isArray()).thenReturn(true);
        jsonHandler.setJsonObject(array);

        jsonHandler.startObject();

        ArgumentCaptor<JsonValue> jsonCaptor = ArgumentCaptor.forClass(JsonValue.class);
        verify(jsonStack).push(jsonCaptor.capture());
        assertSame(array, jsonCaptor.getValue());

        verify(array).addElement(jsonCaptor.capture());
        assertTrue(jsonCaptor.getValue().isObject());
        assertSame(jsonCaptor.getValue(), jsonHandler.getJsonObject());
    }

    @Test
    public void endsObject() throws Exception {
        JsonValue object = mock(JsonValue.class);
        when(object.isObject()).thenReturn(true);
        when(jsonStack.pop()).thenReturn(object);

        jsonHandler.endObject();

        verify(jsonStack).pop();
        assertSame(object, jsonHandler.getJsonObject());
    }

    @Test
    public void startsRootArray() throws Exception {
        jsonHandler.startArray();

        ArgumentCaptor<JsonValue> jsonCaptor = ArgumentCaptor.forClass(JsonValue.class);
        verify(jsonStack).push(jsonCaptor.capture());
        assertTrue(jsonCaptor.getValue().isArray());
        assertSame(jsonCaptor.getValue(), jsonHandler.getJsonObject());
    }

    @Test
    public void startsArrayAsChildOfParentObject() throws Exception {
        JsonValue parent = mock(JsonValue.class);
        when(parent.isObject()).thenReturn(true);
        jsonHandler.setJsonObject(parent);

        jsonHandler.key("key2");
        jsonHandler.startArray();

        ArgumentCaptor<JsonValue> jsonCaptor = ArgumentCaptor.forClass(JsonValue.class);
        verify(jsonStack).push(jsonCaptor.capture());
        assertSame(parent, jsonCaptor.getValue());

        verify(parent).addElement(eq("key2"), jsonCaptor.capture());
        assertTrue(jsonCaptor.getValue().isArray());
        assertSame(jsonCaptor.getValue(), jsonHandler.getJsonObject());
    }

    @Test
    public void endsArray() throws Exception {
        JsonValue array = mock(JsonValue.class);
        when(array.isArray()).thenReturn(true);
        when(jsonStack.pop()).thenReturn(array);

        jsonHandler.endArray();

        verify(jsonStack).pop();
        assertSame(array, jsonHandler.getJsonObject());
    }

    @Test
    public void startsArrayAsItemOfParentArray() throws Exception {
        JsonValue array = mock(JsonValue.class);
        when(array.isArray()).thenReturn(true);
        jsonHandler.setJsonObject(array);

        jsonHandler.startArray();

        ArgumentCaptor<JsonValue> jsonCaptor = ArgumentCaptor.forClass(JsonValue.class);
        verify(jsonStack).push(jsonCaptor.capture());
        assertSame(array, jsonCaptor.getValue());

        verify(array).addElement(jsonCaptor.capture());
        assertTrue(jsonCaptor.getValue().isArray());
        assertSame(jsonCaptor.getValue(), jsonHandler.getJsonObject());
    }

    @Test
    public void createsJsonValueFromCharArrayAndAddItAsChildOfParentObject() throws Exception {
        JsonValue parent = mock(JsonValue.class);
        when(parent.isObject()).thenReturn(true);
        jsonHandler.setJsonObject(parent);

        JsonValue stringValue = new StringValue("xxx");
        when(jsonValueFactory.createJsonValue("xxx")).thenReturn(stringValue);

        jsonHandler.key("key3");
        jsonHandler.characters("xxx".toCharArray());

        ArgumentCaptor<JsonValue> jsonCaptor = ArgumentCaptor.forClass(JsonValue.class);
        verify(parent).addElement(eq("key3"), jsonCaptor.capture());
        assertSame(stringValue, jsonCaptor.getValue());
        assertSame(parent, jsonHandler.getJsonObject());
    }

    @Test
    public void createsJsonValueFromCharArrayAndAddItAsItemOfParentArray() throws Exception {
        JsonValue array = mock(JsonValue.class);
        when(array.isArray()).thenReturn(true);
        jsonHandler.setJsonObject(array);

        JsonValue stringValue = new StringValue("zzz");
        when(jsonValueFactory.createJsonValue("zzz")).thenReturn(stringValue);

        jsonHandler.characters("zzz".toCharArray());

        ArgumentCaptor<JsonValue> jsonCaptor = ArgumentCaptor.forClass(JsonValue.class);
        verify(array).addElement(jsonCaptor.capture());
        assertSame(stringValue, jsonCaptor.getValue());
        assertSame(array, jsonHandler.getJsonObject());
    }

    @Test
    public void resetsInternalState() throws Exception {
        JsonValue parent = new ObjectValue();
        jsonHandler.setJsonObject(parent);
        jsonHandler.key("yyy");

        jsonHandler.reset();
        verify(jsonStack).clear();
        assertNull(jsonHandler.getJsonObject());
    }
}