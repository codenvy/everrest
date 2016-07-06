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
import org.everrest.core.Parameter;
import org.everrest.core.method.TypeProducer;

import javax.ws.rs.HeaderParam;

/**
 * Creates object that might be injected to JAX-RS component through method (constructor) parameter or field annotated with
 * &#064;HeaderParam annotation.
 */
public class HeaderParameterResolver implements ParameterResolver<HeaderParam> {
    private final HeaderParam         headerParam;
    private final TypeProducerFactory typeProducerFactory;

    /**
     * @param headerParam
     *         HeaderParam
     */
    HeaderParameterResolver(HeaderParam headerParam, TypeProducerFactory typeProducerFactory) {
        this.headerParam = headerParam;
        this.typeProducerFactory = typeProducerFactory;
    }


    @Override
    public Object resolve(Parameter parameter, ApplicationContext context) throws Exception {
        String param = this.headerParam.value();
        TypeProducer typeProducer = typeProducerFactory.createTypeProducer(parameter.getParameterClass(), parameter.getGenericType());
        return typeProducer.createValue(param, context.getHttpHeaders().getRequestHeaders(), parameter.getDefaultValue());
    }
}
