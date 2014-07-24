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
import org.everrest.core.InitialProperties;
import org.everrest.core.impl.EnvironmentContext;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ContextParameterResolver.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
 */
public class ContextParameterResolver extends ParameterResolver<Context> {
    /**
     * @param contextParam
     *         {@link Context}
     */
    ContextParameterResolver(Context contextParam) {
        // @Context annotation has not value.
    }

    /** {@inheritDoc} */
    @Override
    public Object resolve(org.everrest.core.Parameter parameter, ApplicationContext context) throws Exception {
        Class<?> parameterClass = parameter.getParameterClass();
        if (parameterClass == HttpHeaders.class) {
            return context.getHttpHeaders();
        } else if (parameterClass == SecurityContext.class) {
            return context.getSecurityContext();
        } else if (parameterClass == Request.class) {
            return context.getRequest();
        } else if (parameterClass == UriInfo.class) {
            return context.getUriInfo();
        } else if (parameterClass == Providers.class) {
            return context.getProviders();
        } else if (parameterClass == InitialProperties.class) {
            return context.getInitialProperties();
        }
        // For servlet container environment context contains HttpServletRequest, HttpServletResponse, ServletConfig,
        // ServletContext
        return EnvironmentContext.getCurrent().get(parameter.getParameterClass());
    }
}
