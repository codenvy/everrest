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

import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.method.TypeProducer;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import java.lang.reflect.ParameterizedType;

import static org.everrest.core.util.ParameterizedTypeImpl.newParameterizedType;

/**
 * Creates object that might be injected to JAX-RS component through method (constructor) parameter or field annotated with
 * &#064;FormParam annotation.
 */
public class FormParameterResolver implements ParameterResolver<FormParam> {
    private final FormParam           formParam;
    private final TypeProducerFactory typeProducerFactory;

    /**
     * @param formParam
     *         FormParam
     */
    FormParameterResolver(FormParam formParam, TypeProducerFactory typeProducerFactory) {
        this.formParam = formParam;
        this.typeProducerFactory = typeProducerFactory;
    }

    @Override
    public Object resolve(org.everrest.core.Parameter parameter, ApplicationContext context) throws Exception {
        String param = this.formParam.value();
        TypeProducer typeProducer = typeProducerFactory.createTypeProducer(parameter.getParameterClass(), parameter.getGenericType());

        MultivaluedMap<String, String> form = readForm(context, !parameter.isEncoded());
        return typeProducer.createValue(param, form, parameter.getDefaultValue());
    }

    @SuppressWarnings({"unchecked"})
    private MultivaluedMap<String, String> readForm(ApplicationContext context, boolean decode) throws java.io.IOException {
        MediaType contentType = context.getHttpHeaders().getMediaType();
        ParameterizedType multivaluedMapType = newParameterizedType(MultivaluedMap.class, String.class, String.class);
        MessageBodyReader reader = context.getProviders().getMessageBodyReader(MultivaluedMap.class, multivaluedMapType, null, contentType);
        if (reader == null) {
            throw new IllegalStateException(String.format("Can't find appropriate entity reader for entity type %s and content-type %s",
                                                          MultivaluedMap.class.getName(), contentType));
        }

        reader.readFrom(MultivaluedMap.class,
                        multivaluedMapType,
                        null,
                        contentType,
                        context.getHttpHeaders().getRequestHeaders(),
                        context.getContainerRequest().getEntityStream());
        MultivaluedMap<String, String> form;
        if (decode) {
            form = (MultivaluedMap<String, String>)context.getAttributes().get("org.everrest.provider.entity.decoded.form");
        } else {
            form = (MultivaluedMap<String, String>)context.getAttributes().get("org.everrest.provider.entity.encoded.form");
        }
        if (form == null) {
            form = new MultivaluedMapImpl();
        }

        return form;
    }
}
