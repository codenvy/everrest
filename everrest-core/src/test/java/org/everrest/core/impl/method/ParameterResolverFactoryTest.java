/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
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

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.everrest.core.Property;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.lang.annotation.Annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(DataProviderRunner.class)
public class ParameterResolverFactoryTest {

    @DataProvider
    public static Object[][] supportedAnnotations() {
        return new Object[][]{
                {annotationOfType(CookieParam.class), CookieParameterResolver.class},
                {annotationOfType(Context.class),     ContextParameterResolver.class},
                {annotationOfType(FormParam.class),   FormParameterResolver.class},
                {annotationOfType(HeaderParam.class), HeaderParameterResolver.class},
                {annotationOfType(MatrixParam.class), MatrixParameterResolver.class},
                {annotationOfType(PathParam.class),   PathParameterResolver.class},
                {annotationOfType(QueryParam.class),  QueryParameterResolver.class},
                {annotationOfType(Property.class),    PropertyResolver.class}
        };
    }

    @SuppressWarnings("unchecked")
    private static <A extends Annotation> A annotationOfType(Class<A> annotationType) {
        A annotation = mock(annotationType);
        when(annotation.annotationType()).thenReturn((Class)annotationType);
        return annotation;
    }

    private ParameterResolverFactory parameterResolverFactory;

    @Before
    public void setUp() throws Exception {
        parameterResolverFactory = new ParameterResolverFactory();
    }

    @UseDataProvider("supportedAnnotations")
    @Test
    public void returnsCorrectImplementationOfParameterResolverForInputAnnotation(Annotation annotation,
                                                                                  Class<? extends ParameterResolver> expectedClassOfParameterResolver) {
        ParameterResolver parameterResolver = parameterResolverFactory.createParameterResolver(annotation);

        assertNotNull(parameterResolver);
        assertEquals(expectedClassOfParameterResolver, parameterResolver.getClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenInputAnnotationIsNotSupported() {
        parameterResolverFactory.createParameterResolver(annotationOfType(Path.class));
    }
}