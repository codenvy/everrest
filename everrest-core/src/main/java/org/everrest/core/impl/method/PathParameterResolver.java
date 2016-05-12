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

import org.everrest.core.ApplicationContext;
import org.everrest.core.method.TypeProducer;

import javax.ws.rs.PathParam;

/**
 * Creates object that might be injected to JAX-RS component through method (constructor) parameter or field annotated with
 * &#064;PathParam annotation.
 */
public class PathParameterResolver implements ParameterResolver<PathParam> {
    private final PathParam           pathParam;
    private final TypeProducerFactory typeProducerFactory;

    /**
     * @param pathParam
     *         PathParam
     */
    PathParameterResolver(PathParam pathParam, TypeProducerFactory typeProducerFactory) {
        this.pathParam = pathParam;
        this.typeProducerFactory = typeProducerFactory;
    }

    @Override
    public Object resolve(org.everrest.core.Parameter parameter, ApplicationContext context) throws Exception {
        String param = this.pathParam.value();
        TypeProducer typeProducer = typeProducerFactory.createTypeProducer(parameter.getParameterClass(), parameter.getGenericType());
        return typeProducer.createValue(param, context.getPathParameters(!parameter.isEncoded()), parameter.getDefaultValue());
    }
}
