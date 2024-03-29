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
package org.everrest.core.util;

/** Caseless wrapper for strings. */
public final class CaselessStringWrapper {
  private final String string;

  private final String caselessString;

  public CaselessStringWrapper(String string) {
    this.string = string;
    this.caselessString = string != null ? string.toLowerCase() : null;
  }

  /**
   * Get original string value.
   *
   * @return original string
   */
  public String getString() {
    return string;
  }

  public String toString() {
    return string == null ? "null" : string;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }
    CaselessStringWrapper other = (CaselessStringWrapper) obj;
    return caselessString == null && other.caselessString == null
        || caselessString != null && caselessString.equals(other.caselessString);
  }

  @Override
  public int hashCode() {
    return caselessString == null ? 0 : caselessString.hashCode();
  }
}
