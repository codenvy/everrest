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

import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ObjectValue extends JsonValue {

    /** Children. */
    private final Map<String, JsonValue> children = new LinkedHashMap<String, JsonValue>();


    @Override
    public void addElement(String key, JsonValue child) {
        children.put(key, child);
    }


    @Override
    public boolean isObject() {
        return true;
    }


    @Override
    public Iterator<String> getKeys() {
        return children.keySet().iterator();
    }


    @Override
    public JsonValue getElement(String key) {
        return children.get(key);
    }


    @Override
    public String toString() {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        try {
            writeTo(jsonWriter);
        } catch (JsonException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return stringWriter.toString();
    }


    @Override
    public void writeTo(JsonWriter writer) throws JsonException {
        writer.writeStartObject();
        for (String key : children.keySet()) {
            writer.writeKey(key);
            JsonValue v = children.get(key);
            v.writeTo(writer);
        }
        writer.writeEndObject();
    }

}
