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

public class NullValue extends JsonValue {


    @Override
    public boolean isNull() {
        return true;
    }


    @Override
    public String toString() {
        return "null";
    }


    @Override
    public void writeTo(JsonWriter writer) throws JsonException {
        writer.writeNull();
    }

}
