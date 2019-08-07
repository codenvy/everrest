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
package org.everrest.core.impl.provider;

import org.everrest.core.impl.provider.json.JsonException;
import org.everrest.core.impl.provider.json.JsonGenerator;
import org.everrest.core.impl.provider.json.JsonParser;
import org.everrest.core.impl.provider.json.JsonTransient;
import org.everrest.core.impl.provider.json.JsonUtils;
import org.everrest.core.impl.provider.json.JsonUtils.Types;
import org.everrest.core.impl.provider.json.JsonValue;
import org.everrest.core.impl.provider.json.JsonWriter;
import org.everrest.core.impl.provider.json.ObjectBuilder;
import org.everrest.core.provider.EntityProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * @author andrew00x
 */
@Provider
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class JsonEntityProvider<T> implements EntityProvider<T> {
    private static final Logger LOG = LoggerFactory.getLogger(JsonEntityProvider.class);

    /** Do not process via JSON "known" JAX-RS types and some other. */
    private static final Class<?>[] IGNORED = new Class<?>[]{byte[].class, char[].class, DataSource.class,
                                                             DOMSource.class, File.class, InputStream.class, OutputStream.class,
                                                             JAXBElement.class, MultivaluedMap.class,
                                                             Reader.class, Writer.class, SAXSource.class, StreamingOutput.class,
                                                             StreamSource.class, String.class};

    private static boolean isSupported(Class<?> type) {
        if (type.getAnnotation(JsonTransient.class) != null) {
            return false;
        }
        for (Class<?> c : IGNORED) {
            if (c.isAssignableFrom(type)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return isSupported(type);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public T readFrom(Class<T> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType,
                      MultivaluedMap<String, String> httpHeaders,
                      InputStream entityStream) throws IOException {
        try {
            JsonParser jsonParser = new JsonParser();
            jsonParser.parse(entityStream);
            JsonValue jsonValue = jsonParser.getJsonObject();

            if (JsonValue.class.isAssignableFrom(type)) {
                return (T)jsonValue;
            }

            Types jsonType = JsonUtils.getType(type);
            if (jsonType == Types.ARRAY_BOOLEAN || jsonType == Types.ARRAY_BYTE || jsonType == Types.ARRAY_SHORT
                || jsonType == Types.ARRAY_INT || jsonType == Types.ARRAY_LONG || jsonType == Types.ARRAY_FLOAT
                || jsonType == Types.ARRAY_DOUBLE || jsonType == Types.ARRAY_CHAR || jsonType == Types.ARRAY_STRING
                || jsonType == Types.ARRAY_OBJECT) {
                return (T)ObjectBuilder.createArray(type, jsonValue);
            }
            if (jsonType == Types.COLLECTION) {
                return (T)ObjectBuilder.createCollection((Class)type, genericType, jsonValue);
            }
            if (jsonType == Types.MAP) {
                return (T)ObjectBuilder.createObject((Class)type, genericType, jsonValue);
            }
            return ObjectBuilder.createObject(type, jsonValue);
        } catch (JsonException e) {
            LOG.debug(e.getMessage(), e);
            throw new IOException(String.format("Can't read from input stream %s", e), e);
        }
    }

    @Override
    public long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return isSupported(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writeTo(T t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        try {
            JsonValue jsonValue;
            if (t instanceof JsonValue) {
                jsonValue = (JsonValue)t;
            } else {
                Types jType = JsonUtils.getType(type);
                if (jType == Types.ARRAY_BOOLEAN || jType == Types.ARRAY_BYTE || jType == Types.ARRAY_SHORT
                    || jType == Types.ARRAY_INT || jType == Types.ARRAY_LONG || jType == Types.ARRAY_FLOAT
                    || jType == Types.ARRAY_DOUBLE || jType == Types.ARRAY_CHAR || jType == Types.ARRAY_STRING
                    || jType == Types.ARRAY_OBJECT) {
                    jsonValue = JsonGenerator.createJsonArray(t);
                } else if (jType == Types.COLLECTION) {
                    jsonValue = JsonGenerator.createJsonArray((Collection<?>)t);
                } else if (jType == Types.MAP) {
                    jsonValue = JsonGenerator.createJsonObjectFromMap((Map<String, ?>)t);
                } else {
                    jsonValue = JsonGenerator.createJsonObject(t);
                }
            }
            JsonWriter jsonWriter = new JsonWriter(entityStream);
            jsonValue.writeTo(jsonWriter);
            jsonWriter.flush();
        } catch (JsonException e) {
            LOG.debug(e.getMessage(), e);
            throw new IOException(String.format("Can't write to output stream. %s", e.getMessage()), e);
        }
    }
}
