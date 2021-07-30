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
package org.everrest.core.servlet;

import java.lang.annotation.Annotation;
import javax.servlet.ServletContext;
import org.everrest.core.BaseDependencySupplier;

/**
 * Resolve dependency by look up instance of object in {@link ServletContext}. Instance of object
 * must be present in servlet context as attribute with name which is the same as class or interface
 * name of requested parameter, e.g. instance of org.foo.bar.MyClass must be bound to attribute name
 * org.foo.bar.MyClass
 *
 * @author andrew00x
 */
public class ServletContextDependencySupplier extends BaseDependencySupplier {
  private final ServletContext ctx;

  public ServletContextDependencySupplier(
      ServletContext ctx, Class<? extends Annotation> injectAnnotation) {
    super(injectAnnotation);
    this.ctx = ctx;
  }

  public ServletContextDependencySupplier(ServletContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Object getInstance(Class<?> type) {
    return ctx.getAttribute(type.getName());
  }

  @Override
  public Object getInstanceByName(String name) {
    return ctx.getAttribute(name);
  }
}
