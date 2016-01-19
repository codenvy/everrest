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

import javax.ws.rs.CookieParam;
import javax.ws.rs.core.Cookie;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: CookieParameterResolver.java 285 2009-10-15 16:21:30Z aparfonov
 *          $
 */
public class CookieParameterResolver extends ParameterResolver<CookieParam> {
    /** See {@link CookieParam}. */
    private final CookieParam cookieParam;

    /**
     * @param cookieParam
     *         CookieParam
     */
    CookieParameterResolver(CookieParam cookieParam) {
        this.cookieParam = cookieParam;
    }


    @Override
    public Object resolve(org.everrest.core.Parameter parameter, ApplicationContext context) throws Exception {
        String param = this.cookieParam.value();
        Object c = context.getHttpHeaders().getCookies().get(param);
        if (c != null) {
            return c;
        }

        if (parameter.getDefaultValue() != null) {
            return Cookie.valueOf(parameter.getDefaultValue());
        }

        return null;
    }
}
