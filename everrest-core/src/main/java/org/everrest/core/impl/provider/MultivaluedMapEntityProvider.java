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

import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.provider.EntityProvider;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
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

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: MultivaluedMapEntityProvider.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
 */
@Provider
@Consumes({MediaType.APPLICATION_FORM_URLENCODED})
@Produces({MediaType.APPLICATION_FORM_URLENCODED})
public class MultivaluedMapEntityProvider implements EntityProvider<MultivaluedMap<String, String>> {
    /** {@inheritDoc} */
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (type == MultivaluedMap.class) {
            try {
                ParameterizedType t = (ParameterizedType)genericType;
                Type[] ta = t.getActualTypeArguments();
                return ta.length == 2 && ta[0] == String.class && ta[1] == String.class;
            } catch (ClassCastException e) {
                return false;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public MultivaluedMap<String, String> readFrom(Class<MultivaluedMap<String, String>> type,
                                                   Type genericType,
                                                   Annotation[] annotations,
                                                   MediaType mediaType,
                                                   MultivaluedMap<String, String> httpHeaders,
                                                   InputStream entityStream) throws IOException {
        ApplicationContext context = ApplicationContextImpl.getCurrent();
        Object o = context.getAttributes().get("org.everrest.provider.entity.form");
        if (o != null) {
            return (MultivaluedMap<String, String>)o;
        }

        MultivaluedMap<String, String> form = new MultivaluedMapImpl();
        StringBuilder sb = new StringBuilder();
        try {
            int r;
            while ((r = entityStream.read()) != -1) {
                if (r != '&') {
                    sb.append((char)r);
                } else {
                    addPair(sb.toString().trim(), form);
                    sb.setLength(0);
                }
            }
            // keep the last part
            addPair(sb.toString(), form);

            context.getAttributes().put("org.everrest.provider.entity.form", form);

            return form;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse string and add key/value pair in the {@link MultivaluedMap}.
     *
     * @param s
     *         string for processing
     * @param f
     *         {@link MultivaluedMap} to add result of parsing
     * @throws UnsupportedEncodingException
     *         if supplied string can't be decoded
     */
    private static void addPair(String s, MultivaluedMap<String, String> f) throws UnsupportedEncodingException {
        if (s.length() == 0) {
            return;
        }
        int eq = s.indexOf('=');
        String name;
        String value;
        if (eq < 0) {
            name = URLDecoder.decode(s, "UTF-8");
            value = "";
        } else {
            name = URLDecoder.decode(s.substring(0, eq), "UTF-8");
            value = URLDecoder.decode(s.substring(eq + 1), "UTF-8");
        }
        f.add(name, value);
    }

    /** {@inheritDoc} */
    public long getSize(MultivaluedMap<String, String> t, Class<?> type, Type genericType, Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }

    /** {@inheritDoc} */
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return MultivaluedMap.class.isAssignableFrom(type);
    }

    /** {@inheritDoc} */
    public void writeTo(MultivaluedMap<String, String> t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        int i = 0;
        for (Map.Entry<String, List<String>> e : t.entrySet()) {
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
