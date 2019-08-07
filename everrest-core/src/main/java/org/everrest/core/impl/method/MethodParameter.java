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

import com.google.common.base.MoreObjects;

import org.everrest.core.Parameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Describes the method's parameter.
 *
 * @author andrew00x
 */
public class MethodParameter implements Parameter {
    /**
     * External annotations for parameter, external it mind some other then
     * contains in {@link ParameterHelper#RESOURCE_METHOD_PARAMETER_ANNOTATIONS}.
     */
    private final Annotation[] additional;

    /**
     * One of annotations from
     * {@link ParameterHelper#RESOURCE_METHOD_PARAMETER_ANNOTATIONS}.
     */
    private final Annotation annotation;

    /**
     * Parameter type.
     * See {@link java.lang.reflect.Method#getGenericParameterTypes()} .
     */
    private final Type genericType;

    /** Parameter class. See {@link java.lang.reflect.Method#getParameterTypes()} */
    private final Class<?> clazz;

    /**
     * Default value for this parameter, default value can be used if there is no required parameter in request.
     * See {@link javax.ws.rs.DefaultValue}.
     */
    private final String defaultValue;

    /** See {@link javax.ws.rs.Encoded}. */
    private final boolean encoded;

    /**
     * Constructs new instance of MethodParameter.
     *
     * @param annotation
     *         see {@link #annotation}
     * @param additional
     *         see {@link #additional}
     * @param clazz
     *         parameter class
     * @param genericType
     *         generic parameter type
     * @param defaultValue
     *         default value for parameter. See
     *         {@link javax.ws.rs.DefaultValue}.
     * @param encoded
     *         true if parameter must not be decoded false otherwise
     */
    public MethodParameter(Annotation annotation, Annotation[] additional, Class<?> clazz, Type genericType,
                           String defaultValue, boolean encoded) {
        this.annotation = annotation;
        this.additional = additional;
        this.clazz = clazz;
        this.genericType = genericType;
        this.defaultValue = defaultValue;
        this.encoded = encoded;
    }


    @Override
    public Annotation[] getAnnotations() {
        return additional;
    }


    @Override
    public Annotation getAnnotation() {
        return annotation;
    }


    @Override
    public boolean isEncoded() {
        return encoded;
    }


    @Override
    public String getDefaultValue() {
        return defaultValue;
    }


    @Override
    public Type getGenericType() {
        return genericType;
    }


    @Override
    public Class<?> getParameterClass() {
        return clazz;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                          .add("annotation", annotation)
                          .add("type", clazz)
                          .add("genericType", genericType)
                          .add("defaultValue", defaultValue)
                          .add("isEncoded", encoded)
                          .omitNullValues()
                          .toString();
    }
}
