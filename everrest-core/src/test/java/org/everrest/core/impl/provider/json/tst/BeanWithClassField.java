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

public class BeanWithClassField {
  public static BeanWithClassField createBeanWithClassField() {
    BeanWithClassField beanWithClassField = new BeanWithClassField();
    beanWithClassField.setKlass(BeanWithClassField.class);
    return beanWithClassField;
  }

  private Class klass;

  public Class getKlass() {
    return klass;
  }

  public void setKlass(Class klass) {
    this.klass = klass;
  }
}
