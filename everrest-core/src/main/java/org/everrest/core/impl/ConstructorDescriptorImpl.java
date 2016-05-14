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
import com.google.common.base.MoreObjects.ToStringHelper;

import org.everrest.core.ApplicationContext;
import org.everrest.core.ConstructorDescriptor;
import org.everrest.core.DependencySupplier;
import org.everrest.core.Parameter;
import org.everrest.core.impl.method.ParameterResolver;
import org.everrest.core.impl.method.ParameterResolverFactory;
import org.everrest.core.util.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.everrest.core.impl.method.ParameterHelper.PROVIDER_CONSTRUCTOR_PARAMETER_ANNOTATIONS;
import static org.everrest.core.impl.method.ParameterHelper.RESOURCE_CONSTRUCTOR_PARAMETER_ANNOTATIONS;

public class ConstructorDescriptorImpl implements ConstructorDescriptor {
    private static final Logger LOG = LoggerFactory.getLogger(ConstructorDescriptorImpl.class);

    private final Constructor<?>           constructor;
    private final ParameterResolverFactory parameterResolverFactory;
    /** Collection of constructor's parameters. */
    private final List<Parameter>          parameters;

    public ConstructorDescriptorImpl(Constructor<?> constructor, ParameterResolverFactory parameterResolverFactory) {
        this.constructor = constructor;
        this.parameterResolverFactory = parameterResolverFactory;

        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Class<?> declaringClass = constructor.getDeclaringClass();
        final boolean isProvider = declaringClass.isAnnotationPresent(Provider.class);
        final boolean isEncodedOnClass = declaringClass.isAnnotationPresent(Encoded.class);

        List<ConstructorParameter> parameters = new ArrayList<>(parameterTypes.length);
        if (parameterTypes.length > 0) {
            Type[] genericParameterTypes = constructor.getGenericParameterTypes();
            Annotation[][] allAnnotations = constructor.getParameterAnnotations();

            for (int i = 0; i < parameterTypes.length; i++) {
                String defaultValue = null;
                Annotation parameterAnnotation = null;
                boolean isEncodedOnParameter = false;

                List<String> allowedAnnotation = isProvider ? PROVIDER_CONSTRUCTOR_PARAMETER_ANNOTATIONS : RESOURCE_CONSTRUCTOR_PARAMETER_ANNOTATIONS;

                for (Annotation annotation : allAnnotations[i]) {
                    Class<?> annotationType = annotation.annotationType();
                    if (allowedAnnotation.contains(annotationType.getName())) {
                        if (parameterAnnotation != null) {
                            throw new RuntimeException(
                                    String.format("JAX-RS annotations on one of constructor parameters are equivocality. Annotations: %s and %s can't be applied to one parameter",
                                                  parameterAnnotation, annotation));
                        }
                        parameterAnnotation = annotation;
                    } else if (!isProvider && annotationType == Encoded.class) {
                        isEncodedOnParameter = true;
                    } else if (!isProvider && annotationType == DefaultValue.class) {
                        defaultValue = ((DefaultValue)annotation).value();
                    } else {
                        LOG.debug("Constructor parameter contains unsupported annotation: {} . It will be ignored" + annotation);
                    }
                }

                ConstructorParameter parameter = new ConstructorParameter(parameterAnnotation,
                                                                          allAnnotations[i],
                                                                          parameterTypes[i],
                                                                          genericParameterTypes[i],
                                                                          defaultValue,
                                                                          isEncodedOnParameter || isEncodedOnClass);
                parameters.add(parameter);
            }
        }
        this.parameters = Collections.unmodifiableList(parameters);
    }

    @Override
    public Constructor<?> getConstructor() {
        return constructor;
    }

    @Override
    public List<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public Object createInstance(ApplicationContext context) {
        List<Object> parameterObjects = new ArrayList<>(parameters.size());
        for (Parameter parameter : parameters) {
            Annotation parameterAnnotation = parameter.getAnnotation();
            if (parameterAnnotation != null) {
                ParameterResolver<?> parameterResolver = parameterResolverFactory.createParameterResolver(parameterAnnotation);
                try {
                    parameterObjects.add(parameterResolver.resolve(parameter, context));
                } catch (Exception e) {
                    Class<?> parameterAnnotationType = parameterAnnotation.annotationType();
                    if (parameterAnnotationType == PathParam.class || parameterAnnotationType == QueryParam.class || parameterAnnotationType == MatrixParam.class) {
                        throw new WebApplicationException(e, Response.status(NOT_FOUND).build());
                    }
                    throw new WebApplicationException(e, Response.status(BAD_REQUEST).build());
                }
            } else {
                DependencySupplier dependencies = context.getDependencySupplier();
                if (dependencies == null) {
                    String errorMessage = String.format("Can't instantiate resource %s. DependencySupplier not found, constructor's parameter of type %s could not be injected. ",
                                                        constructor.getName(), parameter.getGenericType());
                    LOG.error(errorMessage);
                    if (Tracer.isTracingEnabled()) {
                        Tracer.trace(errorMessage);
                    }
                    throw new WebApplicationException(Response.status(INTERNAL_SERVER_ERROR).entity(errorMessage).type(TEXT_PLAIN).build());
                }

                Object dependencyInstance = dependencies.getInstance(parameter);
                if (dependencyInstance == null) {
                    String errorMessage = String.format("Can't instantiate resource %s. Constructor's parameter of type %s could not be injected. ", constructor.getName(),
                                                        parameter.getGenericType());
                    LOG.error(errorMessage);
                    if (Tracer.isTracingEnabled()) {
                        Tracer.trace(errorMessage);
                    }
                    throw new WebApplicationException(Response.status(INTERNAL_SERVER_ERROR).entity(errorMessage).type(TEXT_PLAIN).build());
                }

                parameterObjects.add(dependencyInstance);
            }
        }

        try {
            return constructor.newInstance(parameterObjects.toArray());
        } catch (IllegalArgumentException | InstantiationException | IllegalAccessException unexpectedException) {
            throw new InternalException(unexpectedException);
        } catch (InvocationTargetException invocationException) {
            Throwable cause = invocationException.getCause();
            if (cause instanceof WebApplicationException) {
                throw (WebApplicationException)cause;
            }
            if (cause instanceof InternalException) {
                throw (InternalException)cause;
            }

            throw new InternalException(cause);
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    @Override
    public String toString() {
        ToStringHelper toStringHelper = MoreObjects.toStringHelper(this)
                                                   .add("constructor", this.constructor.getName());
        parameters.forEach(value -> toStringHelper.addValue(value));
        return toStringHelper.toString();
    }
}
