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

import com.google.common.collect.Iterables;

import org.everrest.core.ApplicationContext;
import org.everrest.core.method.TypeProducer;

import javax.ws.rs.MatrixParam;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.PathSegment;
import java.util.List;

/**
 * Creates object that might be injected to JAX-RS component through method (constructor) parameter or field annotated with
 * &#064;MatrixParam annotation.
 */
public class MatrixParameterResolver implements ParameterResolver<MatrixParam> {
    private final MatrixParam         matrixParam;
    private final TypeProducerFactory typeProducerFactory;

    /**
     * @param matrixParam
     *         MatrixParam
     */
    MatrixParameterResolver(MatrixParam matrixParam, TypeProducerFactory typeProducerFactory) {
        this.matrixParam = matrixParam;
        this.typeProducerFactory = typeProducerFactory;
    }

    @Override
    public Object resolve(org.everrest.core.Parameter parameter, ApplicationContext context) throws Exception {
        String param = matrixParam.value();
        TypeProducer typeProducer = typeProducerFactory.createTypeProducer(parameter.getParameterClass(), parameter.getGenericType());
        List<PathSegment> pathSegments = context.getUriInfo().getPathSegments(!parameter.isEncoded());

        PathSegment pathSegment = Iterables.getLast(pathSegments, null);

        return typeProducer.createValue(param,
                                        pathSegment == null ? new MultivaluedHashMap<>() : pathSegment.getMatrixParameters(),
                                        parameter.getDefaultValue());
    }
}
