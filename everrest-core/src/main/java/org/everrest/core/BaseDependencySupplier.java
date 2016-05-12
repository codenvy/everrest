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
package org.everrest.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author andrew00x
 */
public abstract class BaseDependencySupplier implements DependencySupplier {
    protected final Class<? extends Annotation> injectAnnotationClass;

    protected final DependencyNameDetector nameDetector;

    public BaseDependencySupplier(Class<? extends Annotation> injectAnnotationClass, DependencyNameDetector nameDetector) {
        if (injectAnnotationClass == null) {
            throw new IllegalArgumentException("Inject annotation class may not be null. ");
        }
        this.injectAnnotationClass = injectAnnotationClass;
        this.nameDetector = nameDetector;
    }

    public BaseDependencySupplier(Class<? extends Annotation> injectAnnotationClass) {
        if (injectAnnotationClass == null) {
            throw new IllegalArgumentException("Inject annotation class may not be null. ");
        }
        this.injectAnnotationClass = injectAnnotationClass;
        nameDetector = null;
    }

    public BaseDependencySupplier() {
        this(javax.inject.Inject.class, new DependencyNameDetector() {
            @Override
            public String getName(Parameter parameter) {
                for (Annotation a : parameter.getAnnotations()) {
                    if (javax.inject.Named.class.isInstance(a)) {
                        String name = ((javax.inject.Named)a).value();
                        if (!name.isEmpty()) {
                            return name;
                        }
                    }
                }
                return null;
            }
        });
    }

    @Override
    public final Object getInstance(Parameter parameter) {
        boolean injectable = false;
        if (parameter instanceof FieldInjector) {
            for (Annotation a : parameter.getAnnotations()) {
                if (injectAnnotationClass.isInstance(a)) {
                    injectable = true;
                    break;
                }
            }
        } else {
            // Annotation required for fields only.
            injectable = true;
        }
        if (injectable) {
            String name = nameDetector != null ? nameDetector.getName(parameter) : null;
            if (name != null) {
                return getInstanceByName(name);
            }
            Class<?> parameterClass = parameter.getParameterClass();
            if (isProvider(parameterClass)) {
                return getProvider(parameter.getGenericType());
            }
            return getInstance(parameterClass);
        }
        return null;
    }

    /**
     * Get instance of dependency by name. This is optional capability so by default this method returns {@code null}. Override it if
     * back-end (e.g. IoC container) supports getting components by key (name).
     *
     * @param name
     *         of dependency
     * @return object of required type or null if instance described by {@code name} may not be produced
     * @throws RuntimeException
     *         if any error occurs while creating instance of {@code name}
     */
    public Object getInstanceByName(String name) {
        return null;
    }

    /**
     * Check is {@code clazz} is javax.inject.Provider (not subclass of it).
     *
     * @param clazz
     *         class to be checked
     * @return {@code true} if {@code clazz} is javax.inject.Provider and {@code false} otherwise
     */
    protected final boolean isProvider(Class<?> clazz) {
        return javax.inject.Provider.class == clazz;
    }

    /**
     * Get Provider of type {@code providerType}.
     *
     * @param providerType
     *         parameterized javax.inject.Provider type
     * @return Provider the instance of javax.inject.Provider for specified {@code providerType}
     */
    public javax.inject.Provider<?> getProvider(Type providerType) {
        if (!(providerType instanceof ParameterizedType)) {
            throw new RuntimeException("Cannot inject provider without type parameter. ");
        }
        if (((ParameterizedType)providerType).getRawType() != javax.inject.Provider.class) {
            throw new RuntimeException(String.format("Type %s is not javax.inject.Provider. ", providerType));
        }
        final Type actualType = ((ParameterizedType)providerType).getActualTypeArguments()[0];
        final Class<?> componentType;
        if (actualType instanceof Class) {
            componentType = (Class<?>)actualType;
        } else if (actualType instanceof ParameterizedType) {
            componentType = (Class<?>)((ParameterizedType)actualType).getRawType();
        } else {
            throw new RuntimeException(String.format("Unsupported type %s. ", actualType));
        }

        // javax.inject.Provider#get() may return null. Such behavior may be unexpected by caller.
        // May be overridden if back-end (e.g. IoC container) provides better solution.
        return new javax.inject.Provider<Object>() {
            @Override
            public Object get() {
                return getInstance(componentType);
            }
        };
    }

    /**
     * Implementation of this interface determine name of dependency required for constructors or fields of Resource or
     * Provider. Typically the name of the dependency may be set by annotation with value as component's qualifier, e.g.
     * &#064javax.inject.Named.
     */
    public interface DependencyNameDetector {
        /**
         * Get name of parameter.
         *
         * @param parameter
         *         the Parameter
         * @return name of {@code parameter} or {@code null} if name can't be detected
         */
        String getName(Parameter parameter);
    }
}
