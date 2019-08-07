/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
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

import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.method.TypeProducer;

import javax.ws.rs.CookieParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Map;

/**
 * Creates object that might be injected to JAX-RS component through method (constructor) parameter or field annotated with
 * &#064;CookieParam annotation.
 */
public class CookieParameterResolver implements ParameterResolver<CookieParam> {
    private final CookieParam         cookieParam;
    private final TypeProducerFactory typeProducerFactory;

    /**
     * @param cookieParam
     *         CookieParam
     */
    CookieParameterResolver(CookieParam cookieParam, TypeProducerFactory typeProducerFactory) {
        this.cookieParam = cookieParam;
        this.typeProducerFactory = typeProducerFactory;
    }


    @Override
    public Object resolve(org.everrest.core.Parameter parameter, ApplicationContext context) throws Exception {
        String param = cookieParam.value();
        if (Cookie.class.isAssignableFrom(parameter.getParameterClass())) {
            Cookie cookie = context.getHttpHeaders().getCookies().get(param);
            if (cookie == null && parameter.getDefaultValue() != null) {
                cookie = Cookie.valueOf(parameter.getDefaultValue());
            }
            return cookie;
        } else {
            TypeProducer typeProducer = typeProducerFactory.createTypeProducer(parameter.getParameterClass(), parameter.getGenericType());
            MultivaluedMap<String, String> cookieValues = new MultivaluedMapImpl();
            for (Map.Entry<String, Cookie> entry : context.getHttpHeaders().getCookies().entrySet()) {
                cookieValues.putSingle(entry.getKey(), entry.getValue().getValue());
            }
            return typeProducer.createValue(param, cookieValues, parameter.getDefaultValue());
        }
    }
}
