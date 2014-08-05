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

import org.everrest.core.Property;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.lang.annotation.Annotation;

/**
 * @author andrew00x
 */
public final class ParameterResolverFactory {
    /**
     * Create parameter resolver for supplied annotation.
     *
     * @param annotation
     *         JAX-RS annotation
     * @return ParameterResolver
     */
    public static ParameterResolver createParameterResolver(Annotation annotation) {
        Class clazz = annotation.annotationType();
        if (clazz == CookieParam.class) {
            return new CookieParameterResolver((CookieParam)annotation);
        }
        if (clazz == Context.class) {
            return new ContextParameterResolver((Context)annotation);
        }
        if (clazz == FormParam.class) {
            return new FormParameterResolver((FormParam)annotation);
        }
        if (clazz == HeaderParam.class) {
            return new HeaderParameterResolver((HeaderParam)annotation);
        }
        if (clazz == MatrixParam.class) {
            return new MatrixParameterResolver((MatrixParam)annotation);
        }
        if (clazz == PathParam.class) {
            return new PathParameterResolver((PathParam)annotation);
        }
        if (clazz == QueryParam.class) {
            return new QueryParameterResolver((QueryParam)annotation);
        }
        if (clazz == Property.class) {
            return new PropertyResolver((Property)annotation);
        }
        return null;
    }

    /** Constructor. */
    private ParameterResolverFactory() {
    }
}
