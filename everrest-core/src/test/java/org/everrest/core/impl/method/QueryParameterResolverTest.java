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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.everrest.core.ApplicationContext;
import org.everrest.core.Parameter;
import org.everrest.core.method.TypeProducer;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class QueryParameterResolverTest {
  private MultivaluedMap<String, String> queryParameters;
  private ApplicationContext applicationContext;
  private Parameter parameter;
  private TypeProducer typeProducer;

  private QueryParameterResolver queryParameterResolver;

  @Before
  public void setUp() throws Exception {
    queryParameters = new MultivaluedHashMap<>();
    queryParameters.putSingle("foo", "aaa");
    queryParameters.putSingle("bar", "bbb");

    applicationContext = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
    when(applicationContext.getQueryParameters(true)).thenReturn(queryParameters);

    QueryParam queryParamAnnotation = mock(QueryParam.class);
    when(queryParamAnnotation.value()).thenReturn("foo");

    parameter = mock(Parameter.class);
    when(parameter.getParameterClass()).thenReturn((Class) String.class);

    typeProducer = mock(TypeProducer.class);
    TypeProducerFactory typeProducerFactory = mock(TypeProducerFactory.class);
    when(typeProducerFactory.createTypeProducer(eq(String.class), any())).thenReturn(typeProducer);

    queryParameterResolver = new QueryParameterResolver(queryParamAnnotation, typeProducerFactory);
  }

  @Test
  public void retrievesQueryParameterFromRequest() throws Exception {
    when(typeProducer.createValue("foo", queryParameters, null))
        .thenReturn(queryParameters.getFirst("foo"));

    Object resolvedHeader = queryParameterResolver.resolve(parameter, applicationContext);

    assertEquals(queryParameters.getFirst("foo"), resolvedHeader);
  }

  @Test
  public void retrievesDefaultValueWhenQueryParameterDoesNotPresentInRequest() throws Exception {
    when(parameter.getDefaultValue()).thenReturn("default");
    when(typeProducer.createValue("foo", queryParameters, "default")).thenReturn("default");

    Object resolvedHeader = queryParameterResolver.resolve(parameter, applicationContext);

    assertEquals("default", resolvedHeader);
  }
}
