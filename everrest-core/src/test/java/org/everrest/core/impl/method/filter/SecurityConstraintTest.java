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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.SecurityContext;
import java.lang.annotation.Annotation;
import org.everrest.core.ApplicationContext;
import org.everrest.core.resource.GenericResourceMethod;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SecurityConstraintTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private PermitAll permitAll;
  private DenyAll denyAll;
  private RolesAllowed rolesAllowed;
  private GenericResourceMethod resourceMethod;
  private SecurityContext securityContext;

  private SecurityConstraint securityConstraint;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    resourceMethod = mock(GenericResourceMethod.class);
    permitAll = mock(PermitAll.class);
    when(permitAll.annotationType()).thenReturn((Class) PermitAll.class);
    denyAll = mock(DenyAll.class);
    when(denyAll.annotationType()).thenReturn((Class) DenyAll.class);
    rolesAllowed = mock(RolesAllowed.class);
    when(rolesAllowed.annotationType()).thenReturn((Class) RolesAllowed.class);
    when(rolesAllowed.value()).thenReturn(new String[] {"user"});
    ApplicationContext applicationContext = mock(ApplicationContext.class);
    securityContext = mock(SecurityContext.class);
    when(applicationContext.getSecurityContext()).thenReturn(securityContext);
    ApplicationContext.setCurrent(applicationContext);

    securityConstraint = new SecurityConstraint();
  }

  @Test
  public void allowsAccessWhenPermitAllAnnotationPresents() {
    when(resourceMethod.getAnnotations()).thenReturn(new Annotation[] {permitAll});

    securityConstraint.accept(resourceMethod, null);
  }

  @Test
  public void allowsAccessWhenSecurityAnnotationAbsents() {
    when(resourceMethod.getAnnotations()).thenReturn(new Annotation[0]);

    securityConstraint.accept(resourceMethod, null);
  }

  @Test
  public void denysAccessWhenDenyAllAnnotationPresents() {
    when(resourceMethod.getAnnotations()).thenReturn(new Annotation[] {denyAll});
    thrown.expect(webApplicationExceptionForbiddenMatcher());

    securityConstraint.accept(resourceMethod, null);
  }

  @Test
  public void allowsAccessWhenUserHasAcceptableRole() {
    when(resourceMethod.getAnnotations()).thenReturn(new Annotation[] {rolesAllowed});
    when(securityContext.isUserInRole("user")).thenReturn(true);

    securityConstraint.accept(resourceMethod, null);
  }

  @Test
  public void denysAccessWhenUserDoesNotHaveAcceptableRole() {
    when(resourceMethod.getAnnotations()).thenReturn(new Annotation[] {rolesAllowed});
    when(securityContext.isUserInRole("user")).thenReturn(false);
    thrown.expect(webApplicationExceptionForbiddenMatcher());

    securityConstraint.accept(resourceMethod, null);
  }

  private BaseMatcher<Throwable> webApplicationExceptionForbiddenMatcher() {
    return new BaseMatcher<Throwable>() {
      @Override
      public boolean matches(Object item) {
        return item instanceof WebApplicationException
            && ((WebApplicationException) item).getResponse().getStatus() == 403;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("WebApplicationException with status 403, \"Forbidden\"");
      }
    };
  }
}
