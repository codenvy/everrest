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

import org.everrest.core.ApplicationContext;
import org.everrest.core.Parameter;
import org.everrest.core.method.TypeProducer;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class PathParameterResolverTest {
    private MultivaluedMap<String, String> pathParameters;
    private ApplicationContext             applicationContext;
    private Parameter                      parameter;
    private TypeProducer                   typeProducer;

    private PathParameterResolver pathParameterResolver;

    @Before
    public void setUp() throws Exception {
        pathParameters = new MultivaluedHashMap<>();
        pathParameters.putSingle("foo", "aaa");
        pathParameters.putSingle("bar", "bbb");

        applicationContext = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
        when(applicationContext.getPathParameters(true)).thenReturn(pathParameters);

        PathParam pathParamAnnotation = mock(PathParam.class);
        when(pathParamAnnotation.value()).thenReturn("foo");

        parameter = mock(Parameter.class);
        when(parameter.getParameterClass()).thenReturn((Class)String.class);

        typeProducer = mock(TypeProducer.class);
        TypeProducerFactory typeProducerFactory = mock(TypeProducerFactory.class);
        when(typeProducerFactory.createTypeProducer(eq(String.class), any())).thenReturn(typeProducer);

        pathParameterResolver = new PathParameterResolver(pathParamAnnotation, typeProducerFactory);
    }

    @Test
    public void retrievesPathParameterFromRequest() throws Exception {
        when(typeProducer.createValue("foo", pathParameters, null)).thenReturn(pathParameters.getFirst("foo"));

        Object resolvedHeader = pathParameterResolver.resolve(parameter, applicationContext);

        assertEquals(pathParameters.getFirst("foo"), resolvedHeader);
    }

    @Test
    public void retrievesDefaultValueWhenPathParameterDoesNotPresentInRequest() throws Exception {
        when(parameter.getDefaultValue()).thenReturn("default");
        when(typeProducer.createValue("foo", pathParameters, "default")).thenReturn("default");

        Object resolvedHeader = pathParameterResolver.resolve(parameter, applicationContext);

        assertEquals("default", resolvedHeader);
    }
}