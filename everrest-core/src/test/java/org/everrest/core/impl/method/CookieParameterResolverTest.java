/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.method;

import com.google.common.collect.ImmutableMap;

import org.everrest.core.ApplicationContext;
import org.everrest.core.Parameter;
import org.everrest.core.method.TypeProducer;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.CookieParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class CookieParameterResolverTest {
    private final Cookie cookie1 = new Cookie("cookie1", "value1");
    private final Cookie cookie2 = new Cookie("cookie2", "value2");

    private ApplicationContext applicationContext;
    private CookieParam        cookieParam;
    private Parameter          parameter;
    private TypeProducer       typeProducer;

    private CookieParameterResolver cookieParameterResolver;

    @Before
    public void setUp() throws Exception {
        Map<String, Cookie> cookies = ImmutableMap.of("cookie1", cookie1,
                                                      "cookie2", cookie2);
        applicationContext = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
        when(applicationContext.getHttpHeaders().getCookies()).thenReturn(cookies);
        cookieParam = mock(CookieParam.class);
        when(cookieParam.value()).thenReturn("cookie1");

        parameter = mock(Parameter.class);

        typeProducer = mock(TypeProducer.class);
        TypeProducerFactory typeProducerFactory = mock(TypeProducerFactory.class);
        when(typeProducerFactory.createTypeProducer(eq(String.class), any())).thenReturn(typeProducer);

        cookieParameterResolver = new CookieParameterResolver(cookieParam, typeProducerFactory);
    }

    @Test
    public void retrievesCookieFromRequest() throws Exception {
        when(parameter.getParameterClass()).thenReturn((Class)Cookie.class);

        Object resolvedCookie = cookieParameterResolver.resolve(parameter, applicationContext);

        assertSame(cookie1, resolvedCookie);
    }

    @Test
    public void createsCookieFromDefaultValueWhenCookieNotFoundInRequest() throws Exception {
        when(cookieParam.value()).thenReturn("no cookie");
        when(parameter.getParameterClass()).thenReturn((Class)Cookie.class);
        when(parameter.getDefaultValue()).thenReturn("default_cookie=xxx");

        Cookie resolvedCookie = (Cookie)cookieParameterResolver.resolve(parameter, applicationContext);

        assertEquals("default_cookie", resolvedCookie.getName());
        assertEquals("xxx", resolvedCookie.getValue());
    }

    @Test
    public void returnsNullWhenCookieNotFoundInRequestAndDefaultValueIsNotSet() throws Exception {
        when(cookieParam.value()).thenReturn("no cookie");
        when(parameter.getParameterClass()).thenReturn((Class)Cookie.class);

        Object resolvedCookie = cookieParameterResolver.resolve(parameter, applicationContext);

        assertNull(resolvedCookie);
    }

    @Test
    public void convertsCookieValueToTypeSpecifiedInParameter() throws Exception {
        when(cookieParam.value()).thenReturn("cookie1");
        when(parameter.getParameterClass()).thenReturn((Class)String.class);
        when(typeProducer.createValue(eq("cookie1"), any(MultivaluedMap.class), any())).thenAnswer(
                invocation -> ((MultivaluedMap<String, String>)invocation.getArguments()[1]).getFirst("cookie1"));

        Object resolvedValue = cookieParameterResolver.resolve(parameter, applicationContext);

        assertEquals(cookie1.getValue(), resolvedValue);
    }
}