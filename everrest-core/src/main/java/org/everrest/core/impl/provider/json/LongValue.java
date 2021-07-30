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

public class LongValue extends NumericValue {

  /** Value. */
  private final long value;

  /**
   * Constructs new LongValue.
   *
   * @param value the value.
   */
  public LongValue(long value) {
    this.value = value;
  }

  @Override
  public boolean isLong() {
    return true;
  }

  @Override
  public String getStringValue() {
    return Long.toString(value);
  }

  @Override
  public byte getByteValue() {
    return (byte) value;
  }

  @Override
  public short getShortValue() {
    return (short) value;
  }

  @Override
  public int getIntValue() {
    return (int) value;
  }

  @Override
  public long getLongValue() {
    return value;
  }

  @Override
  public double getDoubleValue() {
    return value;
  }

  @Override
  public float getFloatValue() {
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
