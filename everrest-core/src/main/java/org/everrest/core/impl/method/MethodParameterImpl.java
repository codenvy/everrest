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
package org.everrest.core.impl.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Describes the method's parameter.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class MethodParameterImpl implements org.everrest.core.method.MethodParameter {
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
     * Parameter type. See
     * {@link java.lang.reflect.Method#getGenericParameterTypes()} .
     */
    private final Type type;

    /** Parameter class. See {@link java.lang.reflect.Method#getParameterTypes()} */
    private final Class<?> clazz;

    /**
     * Default value for this parameter, default value can be used if there is
     * not found required parameter in request. See
     * {@link javax.ws.rs.DefaultValue}.
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
     * @param type
     *         generic parameter type
     * @param defaultValue
     *         default value for parameter. See
     *         {@link javax.ws.rs.DefaultValue}.
     * @param encoded
     *         true if parameter must not be decoded false otherwise
     */
    public MethodParameterImpl(Annotation annotation, Annotation[] additional, Class<?> clazz, Type type,
                               String defaultValue, boolean encoded) {
        this.annotation = annotation;
        this.additional = additional;
        this.clazz = clazz;
        this.type = type;
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
        return type;
    }


    @Override
    public Class<?> getParameterClass() {
        return clazz;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[ MethodParameter: ");
        sb.append("annotation: ");
        sb.append(getAnnotation());
        sb.append("; type: ");
        sb.append(getParameterClass());
        sb.append("; generic-type: ");
        sb.append(getGenericType());
        sb.append("; default-value: ");
        sb.append(getDefaultValue());
        sb.append("; encoded: ");
        sb.append(isEncoded());
        sb.append(" ]");
        return sb.toString();
    }
}
