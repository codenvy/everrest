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
package org.everrest.core;

/** Call "initialize" and "destroy" methods of object. */
public interface LifecycleMethodStrategy {
  /**
   * Call "initialize" method on the specified object. It is up to the implementation how to find
   * "initialize" method. It is possible to have more than one initialize method but any particular
   * order of methods invocation is not guaranteed.
   *
   * @param o the object
   * @throws org.everrest.core.impl.InternalException if initialize method throws any exception
   */
  void invokeInitializeMethods(Object o);

  /**
   * Call "destroy" method on the specified object. It is up to the implementation how to find
   * "destroy" method. It is possible to have more than one destroy method but any particular order
   * of methods invocation is not guaranteed.
   *
   * @param o the object
   * @throws org.everrest.core.impl.InternalException if destroy method throws any exception
   */
  void invokeDestroyMethods(Object o);
}
