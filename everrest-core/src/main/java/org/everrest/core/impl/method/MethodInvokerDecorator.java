/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.method;

import org.everrest.core.ApplicationContext;
import org.everrest.core.method.MethodInvoker;
import org.everrest.core.resource.GenericMethodResource;

/**
 * MethodInvokerDecorator can be used to extend the functionality of {@link MethodInvoker}.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public abstract class MethodInvokerDecorator implements MethodInvoker {
    protected final MethodInvoker decoratedInvoker;

    /**
     * @param decoratedInvoker
     *         decorated MethodInvoker
     */
    public MethodInvokerDecorator(MethodInvoker decoratedInvoker) {
        this.decoratedInvoker = decoratedInvoker;
    }

    /**
     * @see org.everrest.core.method.MethodInvoker#invokeMethod(java.lang.Object,
     * org.everrest.core.resource.GenericMethodResource, org.everrest.core.ApplicationContext)
     */
    @Override
    public Object invokeMethod(Object resource, GenericMethodResource genericMethodResource, ApplicationContext context) {
        return decoratedInvoker.invokeMethod(resource, genericMethodResource, context);
    }
}
