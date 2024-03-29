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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArrayValue extends JsonValue {

  /** List of children. */
  private final List<JsonValue> children = new ArrayList<>();

  @Override
  public void addElement(JsonValue child) {
    children.add(child);
  }

  @Override
  public boolean isArray() {
    return true;
  }

  @Override
  public Iterator<JsonValue> getElements() {
    return children.iterator();
  }

  @Override
  public int size() {
    return children.size();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    int i = 0;
    for (JsonValue child : children) {
      if (i > 0) {
        sb.append(',');
      }
      i++;
      sb.append(child.toString());
    }
    sb.append(']');
    return sb.toString();
  }

  @Override
  public void writeTo(JsonWriter writer) throws JsonException {
    writer.writeStartArray();
    for (JsonValue child : children) {
      child.writeTo(writer);
    }
    writer.writeEndArray();
  }
}
