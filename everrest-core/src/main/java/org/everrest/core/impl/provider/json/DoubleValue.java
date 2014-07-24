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
public class DoubleValue extends NumericValue {

    /** Value. */
    private final double value;

    /**
     * Constructs new DoubleValue.
     *
     * @param value
     *         the value.
     */
    public DoubleValue(double value) {
        this.value = value;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDouble() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getStringValue() {
        return Double.toString(value);
    }

    /** {@inheritDoc} */
    @Override
    public byte getByteValue() {
        return (byte)value;
    }

    /** {@inheritDoc} */
    @Override
    public short getShortValue() {
        return (short)value;
    }

    /** {@inheritDoc} */
    @Override
    public int getIntValue() {
        return (int)value;
    }

    /** {@inheritDoc} */
    @Override
    public long getLongValue() {
        return (long)value;
    }

    /** {@inheritDoc} */
    @Override
    public float getFloatValue() {
        return (float)value;
    }

    /** {@inheritDoc} */
    @Override
    public double getDoubleValue() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getStringValue();
    }

    /** {@inheritDoc} */
    @Override
    public void writeTo(JsonWriter writer) throws JsonException {
        writer.writeValue(value);
    }

}
