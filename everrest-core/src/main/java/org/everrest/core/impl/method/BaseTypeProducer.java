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
package org.everrest.core.impl.method;

import jakarta.ws.rs.core.MultivaluedMap;
import org.everrest.core.method.TypeProducer;

/**
 * Abstraction for creation single (not collection) type.
 *
 * @author andrew00x
 */
public abstract class BaseTypeProducer implements TypeProducer {
  /**
   * Creates object from given string. String will be used as parameter for constructor or static
   * valueOf method.
   *
   * @param value string value
   * @return newly created object
   * @throws Exception if any error occurs
   */
  protected abstract Object createValue(String value) throws Exception;

  @Override
  public Object createValue(
      String param, MultivaluedMap<String, String> values, String defaultValue) throws Exception {

    String value = values.getFirst(param);

    if (value != null) {
      return createValue(value);
    } else if (defaultValue != null) {
      return createValue(defaultValue);
    }

    return null;
  }
}
