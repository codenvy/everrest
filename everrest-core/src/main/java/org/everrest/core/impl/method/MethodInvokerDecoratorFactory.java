/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
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
