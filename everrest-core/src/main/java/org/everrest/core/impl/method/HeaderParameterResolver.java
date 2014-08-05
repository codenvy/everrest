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

import javax.ws.rs.HeaderParam;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: HeaderParameterResolver.java 285 2009-10-15 16:21:30Z aparfonov
 *          $
 */
public class HeaderParameterResolver extends ParameterResolver<HeaderParam> {
    /** See {@link HeaderParam}. */
    private final HeaderParam headerParam;

    /**
     * @param headerParam
     *         HeaderParam
     */
    HeaderParameterResolver(HeaderParam headerParam) {
        this.headerParam = headerParam;
    }


    @Override
    public Object resolve(org.everrest.core.Parameter parameter, ApplicationContext context) throws Exception {
        String param = this.headerParam.value();
        TypeProducer typeProducer =
                ParameterHelper.createTypeProducer(parameter.getParameterClass(), parameter.getGenericType());
        return typeProducer.createValue(param, context.getHttpHeaders().getRequestHeaders(), parameter.getDefaultValue());
    }
}
