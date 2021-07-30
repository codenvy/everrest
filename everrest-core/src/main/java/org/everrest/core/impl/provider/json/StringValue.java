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

public class StringValue extends JsonValue {
    /** Value. */
    private final String value;

    /**
     * Constructs new StringValue.
     *
     * @param value
     *         the value.
     */
    public StringValue(String value) {
        this.value = value;
    }


    @Override
    public boolean isString() {
        return true;
    }


    @Override
    public String getStringValue() {
        return value;
    }

    @Override
    public boolean getBooleanValue() {
        return Boolean.parseBoolean(value);
    }

    @Override
    public Number getNumberValue() {
        return Double.parseDouble(value);
    }

    @Override
    public byte getByteValue() {
        return getNumberValue().byteValue();
    }

    @Override
    public short getShortValue() {
        return getNumberValue().shortValue();
    }

    @Override
    public int getIntValue() {
        return getNumberValue().intValue();
    }

    @Override
    public long getLongValue() {
        return getNumberValue().longValue();
    }

    @Override
    public float getFloatValue() {
        return getNumberValue().floatValue();
    }

    @Override
    public double getDoubleValue() {
        return getNumberValue().doubleValue();
    }


    @Override
    public String toString() {
        return JsonUtils.getJsonString(value);
    }


    @Override
    public void writeTo(JsonWriter writer) throws JsonException {
        writer.writeValue(value);
    }
}
