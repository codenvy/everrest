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

public class DoubleValue extends NumericValue {

    private final double value;

    public DoubleValue(double value) {
        this.value = value;
    }


    @Override
    public boolean isDouble() {
        return true;
    }


    @Override
    public String getStringValue() {
        return Double.toString(value);
    }


    @Override
    public byte getByteValue() {
        return (byte)value;
    }


    @Override
    public short getShortValue() {
        return (short)value;
    }


    @Override
    public int getIntValue() {
        return (int)value;
    }


    @Override
    public long getLongValue() {
        return (long)value;
    }


    @Override
    public float getFloatValue() {
        return (float)value;
    }


    @Override
    public double getDoubleValue() {
        return value;
    }


    @Override
    public String toString() {
        return getStringValue();
    }


    @Override
    public void writeTo(JsonWriter writer) throws JsonException {
        writer.writeValue(value);
    }

}
