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

/**
 * Implementation of DependencySupplier should be able to provide objects that required for
 * constructors or fields of Resource or Provider.
 *
 * @author andrew00x
 */
public interface DependencySupplier {
  /**
   * Get object that is approach do description of {@code parameter}.
   *
   * @param parameter required parameter description
   * @return object of required type or null if instance described by {@code parameter} may not be
   *     produced
   * @throws RuntimeException if any error occurs while creating instance described by {@code
   *     parameter}
   * @see Parameter#getParameterClass()
   * @see Parameter#getGenericType()
   */
  Object getInstance(Parameter parameter);

  /**
   * Get instance of {@code type}.
   *
   * @param type required parameter class
   * @return object of required type or null if instance described by {@code type} may not be
   *     produced
   * @throws RuntimeException if any error occurs while creating instance of {@code type}
   */
  Object getInstance(Class<?> type);
}
