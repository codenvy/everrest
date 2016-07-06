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
package org.everrest.core.impl.provider.json.tst;

import org.everrest.core.impl.provider.json.JsonTransient;

public class BeanWithTransientField {
    private String field = "visible";
    @JsonTransient transient String jsonTransientField = "invisible";
    transient private String transientField = "invisible";

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getTransientField() {
        return transientField;
    }

    public void setTransientField(String transientField) {
        this.transientField = transientField;
    }

    public String getJsonTransientField() {
        return jsonTransientField;
    }

    public void setJsonTransientField(String jsonTransientField) {
        this.jsonTransientField = jsonTransientField;
    }
}
