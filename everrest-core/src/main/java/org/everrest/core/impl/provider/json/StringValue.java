/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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

    /** {@inheritDoc} */
    @Override
    public boolean isString() {
        return true;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return JsonUtils.getJsonString(value);
    }

    /** {@inheritDoc} */
    @Override
    public void writeTo(JsonWriter writer) throws JsonException {
        writer.writeString(value);
    }
}
