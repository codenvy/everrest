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
package org.everrest.core.impl;

import com.google.common.base.MoreObjects;

import org.everrest.core.ApplicationContext;
import org.everrest.core.DependencySupplier;
import org.everrest.core.FieldInjector;
import org.everrest.core.impl.method.ParameterResolverFactory;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.everrest.core.impl.method.ParameterHelper.PROVIDER_FIELDS_ANNOTATIONS;
import static org.everrest.core.impl.method.ParameterHelper.RESOURCE_FIELDS_ANNOTATIONS;

/**
 * @author andrew00x
 */
public class FieldInjectorImpl implements FieldInjector {
    /** All annotations including JAX-RS annotation. */
    private final Annotation[] annotations;
    /** JAX-RS annotation. */
    private final Annotation annotation;
    /**
     * Default value for this parameter, default value can be used if there is not found required parameter in request. See {@link
     * javax.ws.rs.DefaultValue}.
     */
    private final String defaultValue;
    /** See {@link javax.ws.rs.Encoded}. */
    private final boolean encoded;
    private final Field  field;
    private final ParameterResolverFactory parameterResolverFactory;
    /**
     * Setter for field. If setter available it will be used for field initialization. Otherwise field initialized directly.
     */
    private final Method setter;

    /**
     * @param field
     *         java.lang.reflect.Field
     */
    public FieldInjectorImpl(Field field, ParameterResolverFactory parameterResolverFactory) {
        this.field = field;
        this.parameterResolverFactory = parameterResolverFactory;
        this.annotations = field.getDeclaredAnnotations();

        final Class<?> declaringClass = field.getDeclaringClass();

        this.setter = getSetter(declaringClass, field);

        Annotation annotation = null;
        String defaultValue = null;
        boolean encoded = false;

        final boolean isProvider = declaringClass.getAnnotation(Provider.class) != null;
        final List<String> allowedAnnotation = isProvider ? PROVIDER_FIELDS_ANNOTATIONS : RESOURCE_FIELDS_ANNOTATIONS;

        for (int i = 0, length = annotations.length; i < length; i++) {
            Class<?> annotationType = annotations[i].annotationType();
            if (allowedAnnotation.contains(annotationType.getName())) {
                if (annotation != null) {
                    throw new RuntimeException(
                            String.format("JAX-RS annotations on one of fields %s are equivocality. Annotations: %s and %s can't be applied to one field. ",
                                          field, annotation, annotations[i]));
                }
                annotation = annotations[i];
            } else if (annotationType == Encoded.class && !isProvider) {
                encoded = true;
            } else if (annotationType == DefaultValue.class && !isProvider) {
                defaultValue = ((DefaultValue)annotations[i]).value();
            }
        }
        this.defaultValue = defaultValue;
        this.annotation = annotation;
        this.encoded = encoded || declaringClass.getAnnotation(Encoded.class) != null;
    }

    private Method getSetter(Class<?> clazz, Field field) {
        Method setter = null;
        try {
            String name = field.getName();
            String setterName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            setter = clazz.getMethod(setterName, field.getType());
        } catch (NoSuchMethodException ignored) {
        }
        return setter;
    }

    @Override
    public Annotation getAnnotation() {
        return annotation;
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public Class<?> getParameterClass() {
        return field.getType();
    }

    @Override
    public Type getGenericType() {
        return field.getGenericType();
    }

    @Override
    public boolean isEncoded() {
        return encoded;
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public void inject(Object resource, ApplicationContext context) {
        try {
            Object value = null;
            if (annotation != null) {
                value = parameterResolverFactory.createParameterResolver(annotation).resolve(this, context);
            } else {
                DependencySupplier dependencies = context.getDependencySupplier();
                if (dependencies != null) {
                    value = dependencies.getInstance(this);
                }
            }

            if (value != null) {
                if (setter != null) {
                    setter.invoke(resource, value);
                } else {
                    if (!Modifier.isPublic(field.getModifiers())) {
                        field.setAccessible(true);
                    }
                    field.set(resource, value);
                }
            }
        } catch (Exception e) {
            if (annotation != null) {
                Class<?> annotationType = annotation.annotationType();
                if (annotationType == PathParam.class || annotationType == QueryParam.class || annotationType == MatrixParam.class) {
                    throw new WebApplicationException(e, Response.status(NOT_FOUND).build());
                }
                throw new WebApplicationException(e, Response.status(BAD_REQUEST).build());
            }
            throw new WebApplicationException(e, Response.status(INTERNAL_SERVER_ERROR).build());
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("name", getName())
                          .add("annotation", annotation)
                          .add("type", getParameterClass())
                          .add("encoded", encoded)
                          .add("defaultValue", defaultValue)
                          .omitNullValues()
                          .toString();
    }
}
