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
package org.everrest.core.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * @author andrew00x
 */
public final class ParameterizedTypeImpl implements ParameterizedType {

    public static ParameterizedType newParameterizedType(Class<?> rawType, Type... typeArguments) {
        return new ParameterizedTypeImpl(rawType, typeArguments);
    }

    public static ParameterizedType newParameterizedType(Class<?> rawType, Type typeArgument) {
        return new ParameterizedTypeImpl(rawType, typeArgument);
    }

    private final Type     ownerType;
    private final Type[]   typeArguments;
    private final Class<?> rawType;

    private ParameterizedTypeImpl(Type ownerType, Class<?> rawType, Type typeArgument) {
        this.ownerType = ownerType; // always null for now
        this.rawType = rawType;
        this.typeArguments = new Type[]{typeArgument};
    }

    private ParameterizedTypeImpl(Class<?> rawType, Type typeArgument) {
        this(null, rawType, typeArgument);
    }

    private ParameterizedTypeImpl(Type ownerType, Class<?> rawType, Type[] typeArguments) {
        this.ownerType = ownerType; // always null for now
        this.rawType = rawType;
        this.typeArguments = new Type[typeArguments.length];
        System.arraycopy(typeArguments, 0, this.typeArguments, 0, this.typeArguments.length);
    }

    private ParameterizedTypeImpl(Class<?> rawType, Type[] typeArguments) {
        this(null, rawType, typeArguments);
    }

    @Override
    public Type[] getActualTypeArguments() {
        return typeArguments;
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (ownerType != null) {
            builder.append(ownerType instanceof Class ? ((Class<?>)ownerType).getName() : ownerType.toString());
            builder.append('.');
        }
        builder.append(rawType.getName());
        builder.append('<');
        for (int i = 0, length = typeArguments.length; i < length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(typeArguments[i] instanceof Class ? ((Class<?>)typeArguments[i]).getName() : typeArguments[i].toString());
        }
        builder.append('>');
        return builder.toString();
    }

    @Override
    public int hashCode() {
        int hashCode = 7;
        if (ownerType != null) {
            hashCode = 31 * hashCode + ownerType.hashCode();
        }
        hashCode = 31 * hashCode + rawType.hashCode();
        hashCode = 31 * hashCode + Arrays.hashCode(typeArguments);
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ParameterizedType)) {
            return false;
        }
        ParameterizedType other = (ParameterizedType)o;
        return (ownerType == null ? other.getOwnerType() == null : ownerType.equals(other.getOwnerType())) &&
               rawType.equals(other.getRawType()) && Arrays.equals(typeArguments, other.getActualTypeArguments());

    }
}
