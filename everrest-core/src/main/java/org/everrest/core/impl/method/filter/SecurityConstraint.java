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
package org.everrest.core.impl.method.filter;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.lang.annotation.Annotation;
import org.everrest.core.ApplicationContext;
import org.everrest.core.Filter;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Contract of this class is constraint access to the resource method that use JSR-250 security
 * common annotations. See also https://jsr250.dev.java.net
 *
 * @author andrew00x
 */
@Filter
public class SecurityConstraint implements MethodInvokerFilter {
  /**
   * Check does <tt>method</tt> contains one on of security annotations PermitAll, DenyAll,
   * RolesAllowed.
   *
   * @see PermitAll
   * @see DenyAll
   * @see RolesAllowed
   */
  @Override
  public void accept(GenericResourceMethod method, Object[] params) throws WebApplicationException {
    for (Annotation annotation : method.getAnnotations()) {
      Class<?> annotationType = annotation.annotationType();
      if (annotationType == PermitAll.class) {
        return;
      } else if (annotationType == DenyAll.class) {
        throw new WebApplicationException(
            Response.status(FORBIDDEN)
                .entity("User not authorized to call this method")
                .type(TEXT_PLAIN)
                .build());
      } else if (annotationType == RolesAllowed.class) {
        SecurityContext security = ApplicationContext.getCurrent().getSecurityContext();
        for (String role : ((RolesAllowed) annotation).value()) {
          if (security.isUserInRole(role)) {
            return;
          }
        }
        throw new WebApplicationException(
            Response.status(FORBIDDEN)
                .entity("User not authorized to call this method")
                .type(TEXT_PLAIN)
                .build());
      }
    }
  }
}
