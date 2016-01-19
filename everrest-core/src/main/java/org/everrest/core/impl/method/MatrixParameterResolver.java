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
import org.everrest.core.method.TypeProducer;

import javax.ws.rs.MatrixParam;
import javax.ws.rs.core.PathSegment;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: MatrixParameterResolver.java 285 2009-10-15 16:21:30Z aparfonov
 *          $
 */
public class MatrixParameterResolver extends ParameterResolver<MatrixParam> {
    /** See {@link MatrixParam}. */
    private final MatrixParam matrixParam;

    /**
     * @param matrixParam
     *         MatrixParam
     */
    MatrixParameterResolver(MatrixParam matrixParam) {
        this.matrixParam = matrixParam;
    }


    @Override
    public Object resolve(org.everrest.core.Parameter parameter, ApplicationContext context) throws Exception {
        String param = matrixParam.value();
        TypeProducer typeProducer =
                ParameterHelper.createTypeProducer(parameter.getParameterClass(), parameter.getGenericType());
        List<PathSegment> pathSegments = context.getUriInfo().getPathSegments(!parameter.isEncoded());

        PathSegment pathSegment = pathSegments.get(pathSegments.size() - 1);

        return typeProducer.createValue(param, pathSegment.getMatrixParameters(), parameter.getDefaultValue());
    }
}
