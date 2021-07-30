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
 * Object field. Useful for initialization object field if type is used in* per-request mode.
 *
 * @author andrew00x
 */
public interface FieldInjector extends Parameter {

  /** @return field name */
  String getName();

  /**
   * Set Object {@link java.lang.reflect.Field} using ApplicationContext for resolve actual field
   * value.
   *
   * @param resource root resource or provider
   * @param context ApplicationContext
   */
  void inject(Object resource, ApplicationContext context);
}
