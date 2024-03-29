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

import java.lang.reflect.Method;

/**
 * Creates object from class which has method {@code valueOf} with single String argument.
 *
 * @author andrew00x
 */
public final class StringValueOfProducer extends BaseTypeProducer {
  /** This method will be used for creation object. */
  private Method valueOfMethod;

  /** @param valueOfMethod static method with single String parameter */
  StringValueOfProducer(Method valueOfMethod) {
    this.valueOfMethod = valueOfMethod;
  }

  @Override
  protected Object createValue(String value) throws Exception {
    if (value == null) {
      return null;
    }

    return valueOfMethod.invoke(null, value);
  }
}
