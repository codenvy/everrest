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
package org.everrest.core.impl.provider;

import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.provider.EntityProvider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Provider
@Consumes({MediaType.APPLICATION_FORM_URLENCODED})
@Produces({MediaType.APPLICATION_FORM_URLENCODED})
public class MultivaluedMapEntityProvider implements EntityProvider<MultivaluedMap<String, String>> {

    private HttpServletRequest httpRequest;

    public MultivaluedMapEntityProvider(@Context HttpServletRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (type == MultivaluedMap.class) {
            try {
                ParameterizedType parameterizedType = (ParameterizedType)genericType;
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                return typeArguments.length == 2 && typeArguments[0] == String.class && typeArguments[1] == String.class;
            } catch (ClassCastException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MultivaluedMap<String, String> readFrom(Class<MultivaluedMap<String, String>> type,
                                                   Type genericType,
                                                   Annotation[] annotations,
                                                   MediaType mediaType,
                                                   MultivaluedMap<String, String> httpHeaders,
                                                   InputStream entityStream) throws IOException {
        ApplicationContext context = ApplicationContext.getCurrent();
        Object decodedMap = context.getAttributes().get("org.everrest.provider.entity.decoded.form");
        if (decodedMap != null) {
            return (MultivaluedMap<String, String>)decodedMap;
        }

        MultivaluedMap<String, String> encodedForm = new MultivaluedMapImpl();
        MultivaluedMap<String, String> decodedForm = new MultivaluedMapImpl();
        StringBuilder sb = new StringBuilder();
        int r;
        while ((r = entityStream.read()) != -1) {
            if (r != '&') {
                sb.append((char)r);
            } else {
                parseKeyValuePair(sb.toString().trim(), encodedForm, decodedForm);
                sb.setLength(0);
            }
        }
        parseKeyValuePair(sb.toString().trim(), encodedForm, decodedForm);

        if (decodedForm.isEmpty() && httpRequest != null) {
            httpRequest.getParameterMap()
                       .entrySet()
                       .stream()
                       .filter(e -> e.getValue() != null)
                       .forEach(e -> decodedForm.addAll(e.getKey(), e.getValue()));
        }

        context.getAttributes().put("org.everrest.provider.entity.decoded.form", decodedForm);
        context.getAttributes().put("org.everrest.provider.entity.encoded.form", encodedForm);

        return decodedForm;
    }

    /**
     * Parse string and add key/value pair in the {@link MultivaluedMap}.
     *
     * @param pair
     *         string for processing
     * @param encodedForm
     *         {@link MultivaluedMap} to add encoded result of parsing
     * @param decodedForm
     *         {@link MultivaluedMap} to add decoded result of parsing
     * @throws UnsupportedEncodingException
     *         if supplied string can't be decoded
     */
    private void parseKeyValuePair(String pair, MultivaluedMap<String, String> encodedForm, MultivaluedMap<String, String> decodedForm) throws UnsupportedEncodingException {
        if (pair.length() == 0) {
            return;
        }
        int eq = pair.indexOf('=');
        String encodedName;
        String encodedValue;
        String decodedName;
        String decodedValue;
        if (eq < 0) {
            encodedName = pair;
            encodedValue = "";
            decodedName = URLDecoder.decode(encodedName, "UTF-8");
            decodedValue = "";
        } else {
            encodedName = pair.substring(0, eq);
            encodedValue = pair.substring(eq + 1);
            decodedName = URLDecoder.decode(encodedName, "UTF-8");
            decodedValue = URLDecoder.decode(encodedValue, "UTF-8");
        }
        encodedForm.add(encodedName, encodedValue);
        decodedForm.add(decodedName, decodedValue);
    }

    @Override
    public long getSize(MultivaluedMap<String, String> multivaluedMap, Class<?> type, Type genericType, Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return MultivaluedMap.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(MultivaluedMap<String, String> multivaluedMap,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        int i = 0;
        for (Map.Entry<String, List<String>> e : multivaluedMap.entrySet()) {
            for (String value : e.getValue()) {
                if (i > 0) {
                    entityStream.write('&');
                }
                String name = URLEncoder.encode(e.getKey(), "UTF-8");
                entityStream.write(name.getBytes());
                i++;
                if (value != null) {
                    entityStream.write('=');
                    value = URLEncoder.encode(value, "UTF-8");
                    entityStream.write(value.getBytes());
                }
            }
        }
    }
}
