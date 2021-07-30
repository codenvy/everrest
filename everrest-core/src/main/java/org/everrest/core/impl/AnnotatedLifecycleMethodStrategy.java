/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.everrest.core.LifecycleMethodStrategy;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Implementation of LifecycleComponent.LifecycleMethodStrategy that uses {@link PostConstruct} and {@link PreDestroy}
 * annotation to find "initialize" and "destroy" methods.
 */
public final class AnnotatedLifecycleMethodStrategy implements LifecycleMethodStrategy {

    private static class MethodFilter {
        private final Class<? extends Annotation> annotation;

        MethodFilter(Class<? extends Annotation> annotation) {
            this.annotation = annotation;
        }

        /**
         * Check is method may be used as PostConstruct/PreDestroy method. There are some limitations according to
         * requirements usage of {@link PostConstruct} and {@link PreDestroy} annotation :
         * <ul>
         * <li>Method is annotated with {@link #annotation}.</li>
         * <li>Method must not be static.</li>
         * <li>Method has not have any parameters.</li>
         * <li>The return type of the method must be void.</li>
         * <li>Method must not throw checked exception.</li>
         * </ul>
         *
         * @param method
         *         the method
         * @return <code>true</code> if method is matched to requirements above and false otherwise
         * @see PostConstruct
         * @see PreDestroy
         */
        boolean accept(Method method) {
            return (!Modifier.isStatic(method.getModifiers()))
                   && (method.getReturnType() == void.class || method.getReturnType() == Void.class)
                   && method.getParameterTypes().length == 0
                   && noCheckedException(method)
                   && method.getAnnotation(annotation) != null;
        }

        private boolean noCheckedException(Method method) {
            Class<?>[] exceptions = method.getExceptionTypes();
            if (exceptions.length > 0) {
                for (Class<?> exception : exceptions) {
                    if (!RuntimeException.class.isAssignableFrom(exception)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    private static final MethodFilter POST_CONSTRUCT_METHOD_FILTER = new MethodFilter(PostConstruct.class);
    private static final MethodFilter PRE_DESTROY_METHOD_FILTER    = new MethodFilter(PreDestroy.class);

    private final LoadingCache<Class<?>, Method[]> initializeMethodsCache;
    private final LoadingCache<Class<?>, Method[]> destroyMethodsCache;

    public AnnotatedLifecycleMethodStrategy() {
        initializeMethodsCache = CacheBuilder.newBuilder()
                                             .concurrencyLevel(8)
                                             .maximumSize(256)
                                             .expireAfterAccess(10, MINUTES)
                                             .build(new CacheLoader<Class<?>, Method[]>() {
                                                 @Override
                                                 public Method[] load(Class<?> aClass) {
                                                     return getLifecycleMethods(aClass, POST_CONSTRUCT_METHOD_FILTER);
                                                 }
                                             });
        destroyMethodsCache = CacheBuilder.newBuilder()
                                          .concurrencyLevel(8)
                                          .maximumSize(256)
                                          .expireAfterAccess(10, MINUTES)
                                          .build(new CacheLoader<Class<?>, Method[]>() {
                                              @Override
                                              public Method[] load(Class<?> aClass) {
                                                  return getLifecycleMethods(aClass, PRE_DESTROY_METHOD_FILTER);
                                              }
                                          });
    }

    /** @see LifecycleMethodStrategy#invokeInitializeMethods(java.lang.Object) */
    @Override
    public void invokeInitializeMethods(Object o) {
        final Class<?> aClass = o.getClass();
        Method[] initMethods = null;
        try {
            initMethods = initializeMethodsCache.get(aClass);
        } catch (ExecutionException e) {
            Throwables.propagate(e);
        }
        if (initMethods != null && initMethods.length > 0) {
            doInvokeLifecycleMethods(o, initMethods);
        }
    }

    /** @see LifecycleMethodStrategy#invokeDestroyMethods(java.lang.Object) */
    @Override
    public void invokeDestroyMethods(Object o) {
        final Class<?> aClass = o.getClass();
        Method[] destroyMethods = null;
        try {
            destroyMethods = destroyMethodsCache.get(aClass);
        } catch (ExecutionException e) {
            Throwables.propagate(e);
        }
        if (destroyMethods != null && destroyMethods.length > 0) {
            doInvokeLifecycleMethods(o, destroyMethods);
        }
    }

    private Method[] getLifecycleMethods(Class<?> cl, MethodFilter filter) {
        try {
            List<Method> result = new LinkedList<>();
            Set<String> names = new HashSet<>();
            for (; cl != Object.class; cl = cl.getSuperclass()) {
                Method[] methods = cl.getDeclaredMethods();
                for (Method method : methods) {
                    if (filter.accept(method) && names.add(method.getName())) {
                        if (!Modifier.isPublic(method.getModifiers())) {
                            method.setAccessible(true);
                        }
                        result.add(method);
                    }
                }
            }
            return result.toArray(new Method[result.size()]);
        } catch (SecurityException e) {
            throw new InternalException(e);
        }
    }

    private void doInvokeLifecycleMethods(Object o, Method[] lifecycleMethods) {
        for (Method method : lifecycleMethods) {
            try {
                method.invoke(o);
            } catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                throw new InternalException(t);
            } catch (Exception e) {
                throw new InternalException(e);
            }
        }
    }
}
