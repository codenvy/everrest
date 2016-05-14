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

import org.everrest.core.method.TypeProducer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import static org.everrest.core.util.ReflectionUtils.getStringConstructor;
import static org.everrest.core.util.ReflectionUtils.getStringValueOfMethod;

public class TypeProducerFactory {
    /**
     * @param aClass
     *         method parameter class
     * @param genericType
     *         method parameter type
     * @return TypeProducer
     * @see TypeProducer
     * @see Method#getParameterTypes()
     * @see Method#getGenericParameterTypes()
     */
    public TypeProducer createTypeProducer(Class<?> aClass, Type genericType) {
        if (aClass == List.class || aClass == Set.class || aClass == SortedSet.class) {
            Class<?> actualTypeArgument = null;
            if (genericType != null) {
                actualTypeArgument = getActualTypeArgument(genericType);
            }
            Method methodValueOf;
            Constructor<?> constructor;

            if (actualTypeArgument == String.class || actualTypeArgument == null) {
                return new CollectionStringProducer(aClass);
            } else if ((methodValueOf = getStringValueOfMethod(actualTypeArgument)) != null) {
                return new CollectionStringValueOfProducer(aClass, methodValueOf);
            } else if ((constructor = getStringConstructor(actualTypeArgument)) != null) {
                return new CollectionStringConstructorProducer(aClass, constructor);
            }
        } else {
            Method methodValueOf;
            Constructor<?> constructor;

            if (aClass.isPrimitive()) {
                return new PrimitiveTypeProducer(aClass);
            } else if (aClass == String.class) {
                return new StringProducer();
            } else if ((methodValueOf = getStringValueOfMethod(aClass)) != null) {
                return new StringValueOfProducer(methodValueOf);
            } else if ((constructor = getStringConstructor(aClass)) != null) {
                return new StringConstructorProducer(constructor);
            }
        }

        throw new IllegalArgumentException(String.format("Unsupported type %s", aClass));
    }

    /**
     * Get actual type argument for supplied type.
     *
     * @param type
     *         See {@link Type}
     * @return first actual type argument if type is {@link ParameterizedType}, {@code null} otherwise
     */
    private Class<?> getActualTypeArgument(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length == 1) {
                try {
                    return (Class<?>)actualTypeArguments[0];
                } catch (ClassCastException e) {
                    throw new RuntimeException(String.format("Unsupported type %s", actualTypeArguments[0]));
                }
            }
        }
        return null;
    }
}
