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
package org.everrest.core.impl.provider.json;

public class JsonHandler {

    private String key;
    /** JsonValue which is currently in process. */
    private JsonValue current;
    private JsonValueFactory jsonValueFactory;

    /** Stack of JsonValues. */
    private JsonStack<JsonValue> jsonStack;

    public JsonHandler() {
        this.jsonStack = new JsonStack<>();
        this.jsonValueFactory = new JsonValueFactory();
    }

    JsonHandler(JsonStack<JsonValue> jsonStack, JsonValueFactory jsonValueFactory) {
        this.jsonStack = jsonStack;
        this.jsonValueFactory = jsonValueFactory;
    }

    public void characters(char[] characters) {
        if (current.isObject()) {
            current.addElement(key, jsonValueFactory.createJsonValue(new String(characters)));
        } else if (current.isArray()) {
            current.addElement(jsonValueFactory.createJsonValue(new String(characters)));
        }
    }


    public void endArray() {
        current = jsonStack.pop();
    }


    public void endObject() {
        current = jsonStack.pop();
    }


    public void key(String key) {
        this.key = key;
    }


    public void startArray() {
        ArrayValue newArray = new ArrayValue();
        if (current == null) {
            current = newArray;
        } else if (current.isObject()) {
            current.addElement(key, newArray);
        } else if (current.isArray()) {
            current.addElement(newArray);
        }
        jsonStack.push(current);
        current = newArray;
    }


    public void startObject() {
        ObjectValue newObject = new ObjectValue();
        if (current == null) {
            current = newObject;
        } else if (current.isObject()) {
            current.addElement(key, newObject);
        } else if (current.isArray()) {
            current.addElement(newObject);
        }
        jsonStack.push(current);
        current = newObject;
    }

    /** Reset JSON events handler and prepare it for next usage. */
    public void reset() {
        current = null;
        key = null;
        jsonStack.clear();
    }


    public JsonValue getJsonObject() {
        return current;
    }

    void setJsonObject(JsonValue jsonValue) {
        current = jsonValue;
    }

}
