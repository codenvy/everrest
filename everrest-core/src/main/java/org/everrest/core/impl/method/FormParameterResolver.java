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

import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.method.TypeProducer;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author andrew00x
 */
public class FormParameterResolver extends ParameterResolver<FormParam> {
    /** Form generic type. */
    private static final Type FORM_TYPE = (ParameterizedType)MultivaluedMapImpl.class.getGenericInterfaces()[0];

    /** See {@link FormParam}. */
    private final FormParam formParam;

    /**
     * @param formParam
     *         FormParam
     */
    FormParameterResolver(FormParam formParam) {
        this.formParam = formParam;
    }


    @SuppressWarnings({"unchecked"})
    @Override
    public Object resolve(org.everrest.core.Parameter parameter, ApplicationContext context) throws Exception {
        String param = this.formParam.value();
        TypeProducer typeProducer =
                ParameterHelper.createTypeProducer(parameter.getParameterClass(), parameter.getGenericType());

        MediaType contentType = context.getHttpHeaders().getMediaType();
        MessageBodyReader reader =
                context.getProviders().getMessageBodyReader(MultivaluedMap.class, FORM_TYPE, null, contentType);
        if (reader == null) {
            throw new IllegalStateException("Can't find appropriate entity reader for entity type "
                                            + MultivaluedMap.class.getName() + " and content-type " + contentType);
        }


        try (InputStream entityStream = context.getContainerRequest().getEntityStream()) {
            MultivaluedMap<String, String> form =
                    (MultivaluedMap<String, String>)reader.readFrom(MultivaluedMap.class, FORM_TYPE, null, contentType, context
                            .getHttpHeaders().getRequestHeaders(), entityStream);
            return typeProducer.createValue(param, form, parameter.getDefaultValue());
        }
    }
}
