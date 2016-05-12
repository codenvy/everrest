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

public class BooleanValue extends JsonValue {

    private final boolean value;

    public BooleanValue(boolean value) {
        this.value = value;
    }


    @Override
    public boolean isBoolean() {
        return true;
    }


    @Override
    public String toString() {
        return getStringValue();
    }


    @Override
    public boolean getBooleanValue() {
        return value;
    }


    @Override
    public String getStringValue() {
        return value ? "true" : "false";
    }


    @Override
    public void writeTo(JsonWriter writer) throws JsonException {
        writer.writeValue(value);
    }
}
