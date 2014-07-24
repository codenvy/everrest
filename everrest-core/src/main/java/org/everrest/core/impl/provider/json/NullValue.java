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
public class NullValue extends JsonValue {

    /** {@inheritDoc} */
    @Override
    public boolean isNull() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "null";
    }

    /** {@inheritDoc} */
    @Override
    public void writeTo(JsonWriter writer) throws JsonException {
        writer.writeNull();
    }

}
