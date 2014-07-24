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
public class BooleanValue extends JsonValue {

    /** Value. */
    private final boolean value;

    /**
     * Constructs new BooleanValue.
     *
     * @param value
     *         value.
     */
    public BooleanValue(boolean value) {
        this.value = value;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isBoolean() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getStringValue();
    }

    /** {@inheritDoc} */
    @Override
    public boolean getBooleanValue() {
        return value;
    }

    /** {@inheritDoc} */
    public String getStringValue() {
        return value ? "true" : "false";
    }

    /** {@inheritDoc} */
    @Override
    public void writeTo(JsonWriter writer) throws JsonException {
        writer.writeValue(value);
    }

}
