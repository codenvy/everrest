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

import java.lang.annotation.Annotation;
import org.everrest.core.ApplicationContext;

/**
 * Create object that might be injected in JAX-RS component.
 *
 * @param <T> on of JAX-RS annotation that used for method, constructor parameters or fields
 * @author andrew00x
 */
public interface ParameterResolver<T extends Annotation> {
  /**
   * Creates object which will be passed in resource method or locator.
   *
   * @param parameter See {@link org.everrest.core.Parameter}
   * @param context See {@link ApplicationContext}
   * @return newly created instance of class {@link org.everrest.core.Parameter#getParameterClass()}
   * @throws Exception if any errors occurs
   */
  Object resolve(org.everrest.core.Parameter parameter, ApplicationContext context)
      throws Exception;
}
