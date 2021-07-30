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
package org.everrest.core.impl.provider.json.tst;

import org.everrest.core.impl.provider.json.JsonTransient;

public class BeanWithTransientField {
  private String field = "visible";
  @JsonTransient transient String jsonTransientField = "invisible";
  private transient String transientField = "invisible";

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
