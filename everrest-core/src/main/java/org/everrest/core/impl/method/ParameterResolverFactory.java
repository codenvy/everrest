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
package org.everrest.core.impl.method;

import java.lang.annotation.Annotation;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import org.everrest.core.Property;

/** @author andrew00x */
public class ParameterResolverFactory {
  /**
   * Create parameter resolver for supplied annotation.
   *
   * @param annotation JAX-RS annotation
   * @return ParameterResolver
   */
  public ParameterResolver createParameterResolver(Annotation annotation) {
    final Class annotationType = annotation.annotationType();
    if (annotationType == CookieParam.class) {
      return new CookieParameterResolver((CookieParam) annotation, new TypeProducerFactory());
    }
    if (annotationType == Context.class) {
      return new ContextParameterResolver();
    }
    if (annotationType == FormParam.class) {
      return new FormParameterResolver((FormParam) annotation, new TypeProducerFactory());
    }
    if (annotationType == HeaderParam.class) {
      return new HeaderParameterResolver((HeaderParam) annotation, new TypeProducerFactory());
    }
    if (annotationType == MatrixParam.class) {
      return new MatrixParameterResolver((MatrixParam) annotation, new TypeProducerFactory());
    }
    if (annotationType == PathParam.class) {
      return new PathParameterResolver((PathParam) annotation, new TypeProducerFactory());
    }
    if (annotationType == QueryParam.class) {
      return new QueryParameterResolver((QueryParam) annotation, new TypeProducerFactory());
    }
    if (annotationType == Property.class) {
      return new PropertyResolver((Property) annotation);
    }
    throw new IllegalArgumentException(String.format("Unsupported annotation %s", annotationType));
  }
}
