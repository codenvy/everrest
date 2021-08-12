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

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import org.everrest.core.ApplicationContext;
import org.everrest.core.Parameter;
import org.everrest.core.method.TypeProducer;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class MatrixParameterResolverTest {
  private MultivaluedMap<String, String> matrixParameters;
  private ApplicationContext applicationContext;
  private Parameter parameter;
  private TypeProducer typeProducer;

  private MatrixParameterResolver matrixParameterResolver;

  @Before
  public void setUp() throws Exception {
    matrixParameters = new MultivaluedHashMap<>();
    matrixParameters.putSingle("foo", "to be or not to be");
    matrixParameters.putSingle("bar", "hello world");

    PathSegment firstPathSegment = mock(PathSegment.class);
    PathSegment lastPathSegment = mock(PathSegment.class);
    when(lastPathSegment.getMatrixParameters()).thenReturn(matrixParameters);

    applicationContext = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
    when(applicationContext.getUriInfo().getPathSegments(true))
        .thenReturn(newArrayList(firstPathSegment, lastPathSegment));

    MatrixParam matrixParamAnnotation = mock(MatrixParam.class);
    when(matrixParamAnnotation.value()).thenReturn("foo");

    parameter = mock(Parameter.class);
    when(parameter.getParameterClass()).thenReturn((Class) String.class);

    typeProducer = mock(TypeProducer.class);
    TypeProducerFactory typeProducerFactory = mock(TypeProducerFactory.class);
    when(typeProducerFactory.createTypeProducer(eq(String.class), any())).thenReturn(typeProducer);

    matrixParameterResolver =
        new MatrixParameterResolver(matrixParamAnnotation, typeProducerFactory);
  }

  @Test
  public void retrievesMatrixParamValueFromLastPathSegment() throws Exception {
    when(typeProducer.createValue("foo", matrixParameters, null))
        .thenReturn(matrixParameters.getFirst("foo"));

    Object resolvedMatrixParameter = matrixParameterResolver.resolve(parameter, applicationContext);

    assertEquals(matrixParameters.getFirst("foo"), resolvedMatrixParameter);
  }

  @Test
  public void returnsNullWhenListOfPathSegmentsIsEmptyAndDefaultValueIsNull() throws Exception {
    when(applicationContext.getUriInfo().getPathSegments(true)).thenReturn(newArrayList());

    Object resolvedMatrixParameter = matrixParameterResolver.resolve(parameter, applicationContext);

    assertNull(resolvedMatrixParameter);
    verify(typeProducer).createValue(eq("foo"), emptyMultivaluedMap(), (String) isNull());
  }

  @Test
  public void returnsDefaultValueWhenListOfPathSegmentsIsEmpty() throws Exception {
    when(parameter.getDefaultValue()).thenReturn("default value");
    when(typeProducer.createValue(eq("foo"), emptyMultivaluedMap(), eq("default value")))
        .thenReturn("default value");
    when(applicationContext.getUriInfo().getPathSegments(true)).thenReturn(newArrayList());

    Object resolvedMatrixParameter = matrixParameterResolver.resolve(parameter, applicationContext);

    assertEquals("default value", resolvedMatrixParameter);
  }

  private MultivaluedMap emptyMultivaluedMap() {
    return argThat(argument -> argument != null && argument.isEmpty());
  }
}
