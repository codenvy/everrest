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
package org.everrest.core.servlet;

import org.everrest.core.BaseDependencySupplier;

import javax.servlet.ServletContext;
import java.lang.annotation.Annotation;

/**
 * Resolve dependency by look up instance of object in {@link ServletContext}.
 * Instance of object must be present in servlet context as attribute with name
 * which is the same as class or interface name of requested parameter, e.g.
 * instance of org.foo.bar.MyClass must be bound to attribute name
 * org.foo.bar.MyClass
 *
 * @author andrew00x
 */
public class ServletContextDependencySupplier extends BaseDependencySupplier {
    private final ServletContext ctx;

    public ServletContextDependencySupplier(ServletContext ctx, Class<? extends Annotation> injectAnnotation) {
        super(injectAnnotation);
        this.ctx = ctx;
    }

    public ServletContextDependencySupplier(ServletContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Object getInstance(Class<?> type) {
        return ctx.getAttribute(type.getName());
    }

    @Override
    public Object getInstanceByName(String name) {
        return ctx.getAttribute(name);
    }
}
