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

import java.util.Collections;
import java.util.Iterator;

public abstract class JsonValue {

  /** @return true if value is 'object', false otherwise. Should be overridden. */
  public boolean isObject() {
    return false;
  }

  /** @return true if value is 'array', false otherwise. Should be overridden. */
  public boolean isArray() {
    return false;
  }

  /** @return true if value is 'numeric', false otherwise. Should be overridden. */
  public boolean isNumeric() {
    return false;
  }

  /** @return true if value is 'long', false otherwise. Should be overridden. */
  public boolean isLong() {
    return false;
  }

  /** @return true if value is 'double', false otherwise. Should be overridden. */
  public boolean isDouble() {
    return false;
  }

  /** @return true if value is 'String', false otherwise. Should be overridden. */
  public boolean isString() {
    return false;
  }

  /** @return true if value is 'boolean', false otherwise. Should be overridden. */
  public boolean isBoolean() {
    return false;
  }

  /** @return true if value is 'null', false otherwise. Should be overridden. */
  public boolean isNull() {
    return false;
  }

  /**
   * Add child value. This method must be used if isArray() gives true.
   *
   * @param child the child value.
   */
  public void addElement(JsonValue child) {
    throw new UnsupportedOperationException("This type of JsonValue can't have child.");
  }

  /**
   * Add child value. This method must be used if isObject() gives true.
   *
   * @param key the key.
   * @param child the child value.
   */
  public void addElement(String key, JsonValue child) {
    throw new UnsupportedOperationException("This type of JsonValue can't have child.");
  }

  /**
   * Get all element of this value.
   *
   * @return Iterator.
   */
  public Iterator<JsonValue> getElements() {
    return Collections.<JsonValue>emptyList().iterator();
  }

  /**
   * Get all keys for access values.
   *
   * @return Iterator.
   */
  public Iterator<String> getKeys() {
    return Collections.<String>emptyList().iterator();
  }

  /**
   * Get value by key.
   *
   * @param key the key.
   * @return JsonValue with specified key.
   */
  public JsonValue getElement(String key) {
    return null;
  }

  /** @return number of child elements. */
  public int size() {
    return 0;
  }

  /** @return string value. Should be overridden. */
  public String getStringValue() {
    return null;
  }

  /** @return boolean value. Should be overridden. */
  public boolean getBooleanValue() {
    return false;
  }

  /** @return Number value. Should be overridden. */
  public Number getNumberValue() {
    return getDoubleValue();
  }

  /** @return byte value. Should be overridden. */
  public byte getByteValue() {
    return 0;
  }

  /** @return short Value. Should be overridden. */
  public short getShortValue() {
    return 0;
  }

  /** @return int value. Should be overridden. */
  public int getIntValue() {
    return 0;
  }

  /** @return long value. Should be overridden. */
  public long getLongValue() {
    return 0L;
  }

  /** @return float value. Should be overridden. */
  public float getFloatValue() {
    return 0.0F;
  }

  /** @return double value. Should be overridden. */
  public double getDoubleValue() {
    return 0.0;
  }

  @Override
  public abstract String toString();

  /**
   * Write value in given writer.
   *
   * @param writer Writer.
   * @throws JsonException if any errors occurs.
   */
  public abstract void writeTo(JsonWriter writer) throws JsonException;
}
