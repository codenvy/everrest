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

import jakarta.ws.rs.QueryParam;
import org.everrest.core.ApplicationContext;
import org.everrest.core.method.TypeProducer;

/**
 * Creates object that might be injected to JAX-RS component through method (constructor) parameter
 * or field annotated with &#064;QueryParam annotation.
 */
public class QueryParameterResolver implements ParameterResolver<QueryParam> {
  private final QueryParam queryParam;
  private final TypeProducerFactory typeProducerFactory;

  /** @param queryParam QueryParam */
  QueryParameterResolver(QueryParam queryParam, TypeProducerFactory typeProducerFactory) {
    this.queryParam = queryParam;
    this.typeProducerFactory = typeProducerFactory;
  }

  @Override
  public Object resolve(org.everrest.core.Parameter parameter, ApplicationContext context)
      throws Exception {
    String param = this.queryParam.value();
    TypeProducer typeProducer =
        typeProducerFactory.createTypeProducer(
            parameter.getParameterClass(), parameter.getGenericType());
    return typeProducer.createValue(
        param, context.getQueryParameters(!parameter.isEncoded()), parameter.getDefaultValue());
  }
}
