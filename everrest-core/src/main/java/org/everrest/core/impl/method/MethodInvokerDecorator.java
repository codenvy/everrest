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

import org.everrest.core.ApplicationContext;
import org.everrest.core.method.MethodInvoker;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * MethodInvokerDecorator can be used to extend the functionality of {@link MethodInvoker}.
 *
 * @author andrew00x
 */
public class MethodInvokerDecorator implements MethodInvoker {
  protected final MethodInvoker decoratedInvoker;

  /** @param decoratedInvoker decorated MethodInvoker */
  public MethodInvokerDecorator(MethodInvoker decoratedInvoker) {
    this.decoratedInvoker = decoratedInvoker;
  }

  /**
   * @see org.everrest.core.method.MethodInvoker#invokeMethod(java.lang.Object,
   *     GenericResourceMethod, org.everrest.core.ApplicationContext)
   */
  @Override
  public Object invokeMethod(
      Object resource, GenericResourceMethod genericResourceMethod, ApplicationContext context) {
    return decoratedInvoker.invokeMethod(resource, genericResourceMethod, context);
  }
}
