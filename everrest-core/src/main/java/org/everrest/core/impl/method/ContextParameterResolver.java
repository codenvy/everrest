/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
