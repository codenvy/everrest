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

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.everrest.core.ApplicationContext;
import org.everrest.core.Parameter;
import org.everrest.core.method.TypeProducer;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class HeaderParameterResolverTest {
  private MultivaluedMap<String, String> headers;
  private ApplicationContext applicationContext;
  private Parameter parameter;
  private TypeProducer typeProducer;

  private HeaderParameterResolver headerParameterResolver;

  @Before
  public void setUp() throws Exception {
    headers = new MultivaluedHashMap<>();
    headers.putSingle("foo", "to be or not to be");
    headers.putSingle("bar", "hello world");

    applicationContext = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
    when(applicationContext.getHttpHeaders().getRequestHeaders()).thenReturn(headers);

    HeaderParam headerParam = mock(HeaderParam.class);
    when(headerParam.value()).thenReturn("foo");

    parameter = mock(Parameter.class);
    when(parameter.getParameterClass()).thenReturn((Class) String.class);

    typeProducer = mock(TypeProducer.class);
    TypeProducerFactory typeProducerFactory = mock(TypeProducerFactory.class);
    when(typeProducerFactory.createTypeProducer(eq(String.class), any())).thenReturn(typeProducer);

    headerParameterResolver = new HeaderParameterResolver(headerParam, typeProducerFactory);
  }

  @Test
  public void retrievesHeaderFromRequest() throws Exception {
    when(typeProducer.createValue("foo", headers, null)).thenReturn(headers.getFirst("foo"));

    Object resolvedHeader = headerParameterResolver.resolve(parameter, applicationContext);

    assertEquals(headers.getFirst("foo"), resolvedHeader);
  }

  @Test
  public void retrievesDefaultValueWhenHeaderDoesNotPresentInRequest() throws Exception {
    when(parameter.getDefaultValue()).thenReturn("default value");
    when(typeProducer.createValue("foo", headers, "default value")).thenReturn("default value");

    Object resolvedHeader = headerParameterResolver.resolve(parameter, applicationContext);

    assertEquals("default value", resolvedHeader);
  }
}
