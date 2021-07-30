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
package org.everrest.core.method;

import org.everrest.core.ApplicationContext;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Invoke resource methods.
 *
 * @author andrew00x
 * @see GenericResourceMethod
 */
public interface MethodInvoker {

  /**
   * Invoke supplied method and return result of method invoking.
   *
   * @param resource object that contains method
   * @param genericResourceMethod See {@link GenericResourceMethod}
   * @param context See {@link ApplicationContext}
   * @return result of method invoking
   */
  Object invokeMethod(
      Object resource, GenericResourceMethod genericResourceMethod, ApplicationContext context);
}
