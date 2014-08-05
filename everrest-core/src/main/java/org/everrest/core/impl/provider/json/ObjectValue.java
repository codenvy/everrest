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

import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
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
        StringWriter w = new StringWriter();
        JsonWriter jw = new JsonWriter(w);
        try {
            writeTo(jw);
        } catch (JsonException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return w.toString();
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
