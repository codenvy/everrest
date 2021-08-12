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

import jakarta.ws.rs.WebApplicationException;
import org.everrest.core.ApplicationContext;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Can be used for check is {@link GenericResourceMethod} can be invoked. For example can be checked
 * permission to invoke method according to annotation JSR-250.
 *
 * @author andrew00x
 */
public interface MethodInvokerFilter {

  /**
   * Check does supplied method can be invoked.
   *
   * @param genericResourceMethod See {@link GenericResourceMethod}
   * @param params actual method parameters that were created from request
   * @throws WebApplicationException if method can not be invoked cause current environment context,
   *     e.g. for current user, with current request attributes, etc. Actual context can be obtained
   *     as next {@link ApplicationContext#getCurrent()}. WebApplicationException should contain
   *     Response with corresponded status and message.
   */
  void accept(GenericResourceMethod genericResourceMethod, Object[] params)
      throws WebApplicationException;
}
