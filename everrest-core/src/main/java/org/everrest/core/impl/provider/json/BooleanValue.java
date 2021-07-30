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

public class BooleanValue extends JsonValue {

  private final boolean value;

  public BooleanValue(boolean value) {
    this.value = value;
  }

  @Override
  public boolean isBoolean() {
    return true;
  }

  @Override
  public String toString() {
    return getStringValue();
  }

  @Override
  public boolean getBooleanValue() {
    return value;
  }

  @Override
  public String getStringValue() {
    return value ? "true" : "false";
  }

  @Override
  public void writeTo(JsonWriter writer) throws JsonException {
    writer.writeValue(value);
  }
}
