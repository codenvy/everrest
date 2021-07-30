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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED_TYPE;
import static org.everrest.core.util.ParameterizedTypeImpl.newParameterizedType;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class FormParameterResolverTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private MultivaluedMap<String, String> decodedForm;
    private MultivaluedMap<String, String> encodedForm;

    private ApplicationContext applicationContext;
    private MessageBodyReader  messageBodyReader;
    private Parameter          parameter;
    private TypeProducer       typeProducer;

    private FormParameterResolver formParameterResolver;

    @Before
    public void setUp() throws Exception {
        decodedForm = new MultivaluedHashMap<>();
        decodedForm.putSingle("foo", "to be or not to be");
        encodedForm = new MultivaluedHashMap<>();
        encodedForm.putSingle("foo", "to+be+or+not+to+be");
        messageBodyReader = mock(MessageBodyReader.class);
        applicationContext = mockApplicationContext();
        FormParam formParamAnnotation = mockFormParam();
        parameter = mockParameter();
        typeProducer = mock(TypeProducer.class);
        TypeProducerFactory typeProducerFactory = mockTypeProducerFactory();
        formParameterResolver = new FormParameterResolver(formParamAnnotation, typeProducerFactory);
    }

    @Test
    public void resolvesDecodedFormParamAsString() throws Exception {
        when(parameter.isEncoded()).thenReturn(false);
        when(typeProducer.createValue("foo", decodedForm, null)).thenReturn(decodedForm.getFirst("foo"));

        Object resolved = formParameterResolver.resolve(parameter, applicationContext);

        assertEquals(decodedForm.getFirst("foo"), resolved);
        assertThatMessageBodyWriterWasCalled();
    }

    @Test
    public void resolvesEncodedFormParamAsString() throws Exception {
        when(parameter.isEncoded()).thenReturn(true);
        when(typeProducer.createValue("foo", encodedForm, null)).thenReturn(encodedForm.getFirst("foo"));

        Object resolved = formParameterResolver.resolve(parameter, applicationContext);

        assertEquals(encodedForm.getFirst("foo"), resolved);
        assertThatMessageBodyWriterWasCalled();
    }

    @Test
    public void createsObjectFromDefaultValueWhenFormParamNotFound() throws Exception {
        when(parameter.isEncoded()).thenReturn(false);
        when(parameter.getDefaultValue()).thenReturn("default value");
        when(typeProducer.createValue("foo", decodedForm, "default value")).thenReturn("default value");

        Object resolved = formParameterResolver.resolve(parameter, applicationContext);

        assertEquals("default value", resolved);
        assertThatMessageBodyWriterWasCalled();
    }

    @Test
    public void throwsIllegalStateExceptionWhenFormMessageBodyReaderIsNotAvailable() throws Exception {
        Class<MultivaluedMap> type = MultivaluedMap.class;
        ParameterizedType genericType = newParameterizedType(type, String.class, String.class);
        when(applicationContext.getProviders().getMessageBodyReader(eq(type), eq(genericType), any(), eq(APPLICATION_FORM_URLENCODED_TYPE)))
                .thenReturn(null);

        thrown.expect(IllegalStateException.class);

        formParameterResolver.resolve(parameter, applicationContext);
    }

    private ApplicationContext mockApplicationContext() {
        ApplicationContext applicationContext = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);

        when(applicationContext.getHttpHeaders().getMediaType()).thenReturn(APPLICATION_FORM_URLENCODED_TYPE);
        when(applicationContext.getAttributes().get("org.everrest.provider.entity.decoded.form")).thenReturn(decodedForm);
        when(applicationContext.getAttributes().get("org.everrest.provider.entity.encoded.form")).thenReturn(encodedForm);

        Class<MultivaluedMap> type = MultivaluedMap.class;
        ParameterizedType genericType = newParameterizedType(type, String.class, String.class);
        when(applicationContext.getProviders().getMessageBodyReader(eq(type), eq(genericType), any(), eq(APPLICATION_FORM_URLENCODED_TYPE)))
                .thenReturn(messageBodyReader);
        return applicationContext;
    }

    private FormParam mockFormParam() {
        FormParam formParam = mock(FormParam.class);
        when(formParam.value()).thenReturn("foo");
        return formParam;
    }

    private Parameter mockParameter() {
        Parameter parameter = mock(Parameter.class);
        when(parameter.getParameterClass()).thenReturn((Class)String.class);
        return parameter;
    }

    private TypeProducerFactory mockTypeProducerFactory() {
        TypeProducerFactory typeProducerFactory = mock(TypeProducerFactory.class);
        when(typeProducerFactory.createTypeProducer(eq(String.class), any())).thenReturn(typeProducer);
        return typeProducerFactory;
    }

    private void assertThatMessageBodyWriterWasCalled() throws Exception {
        verify(messageBodyReader).readFrom(eq(MultivaluedMap.class),
                                           eq(newParameterizedType(MultivaluedMap.class, String.class, String.class)),
                                           any(),
                                           eq(APPLICATION_FORM_URLENCODED_TYPE),
                                           any(MultivaluedMap.class),
                                           any(InputStream.class));
    }
}