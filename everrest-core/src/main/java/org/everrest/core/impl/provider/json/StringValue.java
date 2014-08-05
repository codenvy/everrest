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

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
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
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
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
        writer.writeString(value);
    }
}
