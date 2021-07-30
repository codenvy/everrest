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

import com.google.common.collect.ImmutableMap;

import org.everrest.core.ApplicationContext;
import org.everrest.core.Parameter;
import org.everrest.core.Property;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class PropertyResolverTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ApplicationContext  applicationContext;
    private Parameter           parameter;

    private PropertyResolver propertyResolver;

    @Before
    public void setUp() throws Exception {
        Map<String, String> properties = ImmutableMap.of("foo", "hello",
                                                         "bar", "world");

        applicationContext = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
        when(applicationContext.getInitialProperties().getProperties()).thenReturn(properties);

        Property propertyAnnotation = mock(Property.class);
        when(propertyAnnotation.value()).thenReturn("foo");

        parameter = mock(Parameter.class);
        when(parameter.getParameterClass()).thenReturn((Class)String.class);

        propertyResolver = new PropertyResolver(propertyAnnotation);
    }

    @Test
    public void retrievesPropertyFromApplicationContext() throws Exception {
        Object resolvedObject = propertyResolver.resolve(parameter, applicationContext);

        assertEquals("hello", resolvedObject);
    }

    @Test
    public void retrievesDefaultValueWhenPropertyDoesNotPresentInApplicationContext() throws Exception {
        when(applicationContext.getInitialProperties().getProperties()).thenReturn(emptyMap());
        when(parameter.getDefaultValue()).thenReturn("default");

        Object resolvedHeader = propertyResolver.resolve(parameter, applicationContext);

        assertEquals("default", resolvedHeader);
    }

    @Test
    public void throwsExceptionWhenPropertyAnnotationIsAppliedToParameterOtherThanString() throws Exception {
        when(parameter.getParameterClass()).thenReturn((Class)Object.class);

        thrown.expect(IllegalArgumentException.class);

        propertyResolver.resolve(parameter, applicationContext);
    }
}