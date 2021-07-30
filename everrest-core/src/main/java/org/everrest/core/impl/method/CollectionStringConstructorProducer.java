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

import java.lang.reflect.Constructor;

/**
 * Produces collections each element of it is object of class which has constructor with single
 * String argument.
 *
 * @author andrew00x
 */
public class CollectionStringConstructorProducer extends BaseCollectionProducer {

  /** This constructor will be used for creation collection elements. */
  private Constructor<?> constructor;

  /**
   * Constructs new instance of CollectionStringConstructorProducer.
   *
   * @param collectionClass class of collection which must be created
   * @param constructor this constructor will be used for produce elements of collection
   */
  CollectionStringConstructorProducer(Class<?> collectionClass, Constructor<?> constructor) {
    super(collectionClass);
    this.constructor = constructor;
  }

  @Override
  protected Object createCollectionItem(String value) throws Exception {
    if (value == null) {
      return null;
    }

    return constructor.newInstance(value);
  }
}
