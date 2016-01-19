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
package org.everrest.core.impl.method.filter;

import org.everrest.core.Filter;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.resource.GenericMethodResource;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.lang.annotation.Annotation;

/**
 * Contract of this class is constraint access to the resource method that use JSR-250 security common annotations. See also
 * https://jsr250.dev.java.net
 *
 * @author andrew00x
 */
@Filter
public class SecurityConstraint implements MethodInvokerFilter {
    /**
     * Check does <tt>method</tt> contains one on of security annotations PermitAll, DenyAll, RolesAllowed.
     *
     * @see PermitAll
     * @see DenyAll
     * @see RolesAllowed
     */
    @Override
    public void accept(GenericMethodResource method, Object[] params) throws WebApplicationException {
        for (Annotation a : method.getAnnotations()) {
            Class<?> aClass = a.annotationType();
            if (aClass == PermitAll.class) {
                return;
            } else if (aClass == DenyAll.class) {
                throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN)
                                                          .entity("User not authorized to call this method.").type(MediaType.TEXT_PLAIN)
                                                          .build());
            } else if (aClass == RolesAllowed.class) {
                SecurityContext security = ApplicationContextImpl.getCurrent().getSecurityContext();
                for (String role : ((RolesAllowed)a).value()) {
                    if (security.isUserInRole(role)) {
                        return;
                    }
                }
                throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN)
                                                          .entity("User not authorized to call this method.").type(MediaType.TEXT_PLAIN)
                                                          .build());
            }
        }
    }
}
