/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
     *      org.everrest.core.resource.GenericMethodResource, org.everrest.core.ApplicationContext)
     */
    @Override
    public Object invokeMethod(Object resource, GenericMethodResource genericMethodResource, ApplicationContext context) {
        return decoratedInvoker.invokeMethod(resource, genericMethodResource, context);
    }
}
