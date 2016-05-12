/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.provider;

import com.google.common.collect.ImmutableMap;

import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.tools.EmptyInputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED_TYPE;
import static org.everrest.core.util.ParameterizedTypeImpl.newParameterizedType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MultivaluedMapEntityProviderTest {
    private final ParameterizedType multiValuedMapGenericType = newParameterizedType(MultivaluedMap.class, String.class, String.class);

    private byte[] testContent;

    private ApplicationContext           context;
    private HttpServletRequest           servletRequest;
    private MultivaluedMapEntityProvider multivaluedMapEntityProvider;

    @Before
    public void setUp() throws Exception {
        testContent = "foo=to%20be%20or%20not%20to%20be&bar=hello%20world".getBytes("UTF-8");

        servletRequest = mock(HttpServletRequest.class);

        context = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
        when(context.getAttributes().get("org.everrest.provider.entity.decoded.form")).thenReturn(null);
        ApplicationContext.setCurrent(context);

        multivaluedMapEntityProvider = new MultivaluedMapEntityProvider(servletRequest);
    }

    @After
    public void tearDown() throws Exception {
        ApplicationContext.setCurrent(null);
    }

    @Test
    public void isReadableForMultivaluedMap() throws Exception {
        assertTrue(multivaluedMapEntityProvider.isReadable(MultivaluedMap.class, multiValuedMapGenericType, null, null));
    }

    @Test
    public void isNotReadableForTypeOtherThanMultivaluedMap() throws Exception {
        assertFalse(multivaluedMapEntityProvider.isReadable(Object.class, null, null, null));
    }

    @Test
    public void isWritableForMultivaluedMap() throws Exception {
        assertTrue(multivaluedMapEntityProvider.isWriteable(MultivaluedMap.class, null, null, null));
    }

    @Test
    public void isNotWritableForTypeOtherThanMultivaluedMap() throws Exception {
        assertFalse(multivaluedMapEntityProvider.isWriteable(Object.class, null, null, null));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void readsContentOfEntityStreamAsMultivaluedMap() throws Exception {
        MultivaluedMap<String, String> multivaluedMap = multivaluedMapEntityProvider.readFrom((Class)MultivaluedMap.class, multiValuedMapGenericType,
                                                                                              null, APPLICATION_FORM_URLENCODED_TYPE,
                                                                                              new MultivaluedMapImpl(),
                                                                                              new ByteArrayInputStream(testContent));
        Map<String, List<String>> decodedMap = ImmutableMap.of("foo", newArrayList("to be or not to be"), "bar", newArrayList("hello world"));
        Map<String, List<String>> encodedMap = ImmutableMap.of("foo", newArrayList("to%20be%20or%20not%20to%20be"), "bar", newArrayList("hello%20world"));

        assertEquals(decodedMap, multivaluedMap);

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);

        verify(context.getAttributes()).put(eq("org.everrest.provider.entity.decoded.form"), argumentCaptor.capture());
        assertEquals(decodedMap, argumentCaptor.getValue());

        verify(context.getAttributes()).put(eq("org.everrest.provider.entity.encoded.form"), argumentCaptor.capture());
        assertEquals(encodedMap, argumentCaptor.getValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getsParsedDecodedMultivaluedMapFromContextIfPresent() throws Exception {
        MultivaluedMap<String, String> decodedMap = new MultivaluedHashMap<>();
        when(context.getAttributes().get("org.everrest.provider.entity.decoded.form")).thenReturn(decodedMap);

        MultivaluedMap<String, String> multivaluedMap = multivaluedMapEntityProvider.readFrom((Class)MultivaluedMap.class, multiValuedMapGenericType,
                                                                                              null, APPLICATION_FORM_URLENCODED_TYPE,
                                                                                              new MultivaluedMapImpl(),
                                                                                              new ByteArrayInputStream(testContent));
        assertSame(decodedMap, multivaluedMap);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getsParametersFromServletRequestWhenEntityStreamIsEmpty() throws Exception {
        Map<String, String[]> parameterMap = ImmutableMap.of("foo", new String[]{"to be or not to be"}, "bar", new String[]{"hello world"});
        when(servletRequest.getParameterMap()).thenReturn(parameterMap);

        MultivaluedMap<String, String> multivaluedMap = multivaluedMapEntityProvider.readFrom((Class)MultivaluedMap.class, multiValuedMapGenericType,
                                                                                              null, APPLICATION_FORM_URLENCODED_TYPE,
                                                                                              new MultivaluedMapImpl(),
                                                                                              new EmptyInputStream());

        Map<String, List<String>> decodedMap = ImmutableMap.of("foo", newArrayList("to be or not to be"), "bar", newArrayList("hello world"));

        assertEquals(decodedMap, multivaluedMap);

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);

        verify(context.getAttributes()).put(eq("org.everrest.provider.entity.decoded.form"), argumentCaptor.capture());
        assertEquals(decodedMap, argumentCaptor.getValue());

        verify(context.getAttributes()).put(eq("org.everrest.provider.entity.encoded.form"), argumentCaptor.capture());
        assertTrue(((MultivaluedMap)argumentCaptor.getValue()).isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void readsContentOfEntityStreamAsMultivaluedMapWhenFormFieldMissedValue() throws Exception {
        testContent = "foo&bar=hello%20world".getBytes("UTF-8");
        MultivaluedMap<String, String> multivaluedMap = multivaluedMapEntityProvider.readFrom((Class)MultivaluedMap.class,
                                                                                              multiValuedMapGenericType,
                                                                                              null, APPLICATION_FORM_URLENCODED_TYPE,
                                                                                              new MultivaluedMapImpl(),
                                                                                              new ByteArrayInputStream(testContent));
        Map<String, List<String>> decodedMap = ImmutableMap.of("foo", newArrayList(""), "bar", newArrayList("hello world"));

        assertEquals(decodedMap, multivaluedMap);
    }

    @Test
    public void writesMultivaluedMapToOutputStream() throws Exception {
        MultivaluedMap<String, String> multivaluedMap = new MultivaluedHashMap<>();
        multivaluedMap.put("bar", newArrayList("hello world"));
        multivaluedMap.put("foo", newArrayList("to be or not to be"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        multivaluedMapEntityProvider.writeTo(multivaluedMap, MultivaluedMap.class, multiValuedMapGenericType, null,
                                             APPLICATION_FORM_URLENCODED_TYPE, new MultivaluedHashMap<>(), out);

        assertEquals("bar=hello+world&foo=to+be+or+not+to+be", out.toString());
    }

}