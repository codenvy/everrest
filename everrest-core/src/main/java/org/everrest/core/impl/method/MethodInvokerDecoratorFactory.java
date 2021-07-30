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

import org.everrest.core.method.MethodInvoker;

/**
 * This factory is intended for produce instance of {@link MethodInvokerDecorator}.
 *
 * @author andrew00x
 */
public interface MethodInvokerDecoratorFactory {
  MethodInvokerDecorator makeDecorator(MethodInvoker invoker);
}
