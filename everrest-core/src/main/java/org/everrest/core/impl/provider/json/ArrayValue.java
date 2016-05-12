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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArrayValue extends JsonValue {

    /** List of children. */
    private final List<JsonValue> children = new ArrayList<JsonValue>();


    @Override
    public void addElement(JsonValue child) {
        children.add(child);
    }


    @Override
    public boolean isArray() {
        return true;
    }


    @Override
    public Iterator<JsonValue> getElements() {
        return children.iterator();
    }


    @Override
    public int size() {
        return children.size();
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        int i = 0;
        for (JsonValue v : children) {
            if (i > 0) {
                sb.append(',');
            }
            i++;
            sb.append(v.toString());
        }
        sb.append(']');
        return sb.toString();
    }


    @Override
    public void writeTo(JsonWriter writer) throws JsonException {
        writer.writeStartArray();
        for (JsonValue v : children) {
            v.writeTo(writer);
        }
        writer.writeEndArray();
    }

}
