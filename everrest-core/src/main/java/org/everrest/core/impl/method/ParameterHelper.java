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

import org.everrest.core.Property;
import org.everrest.core.method.TypeProducer;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class ParameterHelper {
    /**
     * Collections of annotation that allowed to be used on fields on any type of
     * Provider.
     *
     * @see javax.ws.rs.ext.Provider
     * @see javax.ws.rs.ext.Providers
     */
    public static final List<String> PROVIDER_FIELDS_ANNOTATIONS;

    /**
     * Collections of annotation than allowed to be used on constructor's
     * parameters of any type of Provider.
     *
     * @see javax.ws.rs.ext.Provider
     * @see javax.ws.rs.ext.Providers
     */
    public static final List<String> PROVIDER_CONSTRUCTOR_PARAMETER_ANNOTATIONS;

    /**
     * Collections of annotation that allowed to be used on fields of resource
     * class.
     */
    public static final List<String> RESOURCE_FIELDS_ANNOTATIONS;

    /**
     * Collections of annotation than allowed to be used on constructor's
     * parameters of resource class.
     */
    public static final List<String> RESOURCE_CONSTRUCTOR_PARAMETER_ANNOTATIONS;

    /**
     * Collections of annotation than allowed to be used on method's parameters
     * of resource class.
     */
    public static final List<String> RESOURCE_METHOD_PARAMETER_ANNOTATIONS;

    static {
        PROVIDER_FIELDS_ANNOTATIONS =
                Collections.unmodifiableList(Arrays.asList(Context.class.getName(), Property.class.getName()));

        PROVIDER_CONSTRUCTOR_PARAMETER_ANNOTATIONS =
                Collections.unmodifiableList(Arrays.asList(Context.class.getName(), Property.class.getName()));

        List<String> tmp1 = new ArrayList<String>(7);
        tmp1.add(CookieParam.class.getName());
        tmp1.add(Context.class.getName());
        tmp1.add(HeaderParam.class.getName());
        tmp1.add(MatrixParam.class.getName());
        tmp1.add(PathParam.class.getName());
        tmp1.add(QueryParam.class.getName());
        tmp1.add(Property.class.getName());
        RESOURCE_FIELDS_ANNOTATIONS = Collections.unmodifiableList(tmp1);
        RESOURCE_CONSTRUCTOR_PARAMETER_ANNOTATIONS = Collections.unmodifiableList(tmp1);

        List<String> tmp2 = new ArrayList<String>(tmp1);
        tmp2.add(FormParam.class.getName());
        RESOURCE_METHOD_PARAMETER_ANNOTATIONS = Collections.unmodifiableList(tmp2);
    }

    /**
     * @param parameterClass
     *         method parameter class
     * @param parameterType
     *         method parameter type
     * @return TypeProducer
     * @see TypeProducer
     * @see Method#getParameterTypes()
     * @see Method#getGenericParameterTypes()
     */
    static TypeProducer createTypeProducer(Class<?> parameterClass, Type parameterType) {

        if (parameterClass == List.class || parameterClass == Set.class || parameterClass == SortedSet.class) {
            // parameter is collection

            Class<?> clazz = null;
            if (parameterType != null) {
                clazz = getGenericType(parameterType);
            }
            Method methodValueOf;
            Constructor<?> constructor;

            // if not parameterized then by default collection of Strings.
            if (clazz == String.class || clazz == null) {
                // String
                return new CollectionStringProducer(parameterClass);
            } else if ((methodValueOf = getStringValueOfMethod(clazz)) != null) {
                // static method valueOf
                return new CollectionStringValueOfProducer(parameterClass, methodValueOf);
            } else if ((constructor = getStringConstructor(clazz)) != null) {
                // constructor with String
                return new CollectionStringConstructorProducer(parameterClass, constructor);
            }
        } else {
            // parameters is not collection
            Method methodValueOf;
            Constructor<?> constructor;

            if (parameterClass.isPrimitive()) {
                // primitive type
                return new PrimitiveTypeProducer(parameterClass);
            } else if (parameterClass == String.class) {
                // String
                return new StringProducer();
            } else if ((methodValueOf = getStringValueOfMethod(parameterClass)) != null) {
                // static valueOf method
                return new StringValueOfProducer(methodValueOf);
            } else if ((constructor = getStringConstructor(parameterClass)) != null) {
                // constructor with String
                return new StringConstructorProducer(constructor);
            }
        }

        return null;
    }

    /**
     * Get static {@link Method} with single string argument and name 'valueOf'
     * for supplied class.
     *
     * @param clazz
     *         class for discovering to have public static method with name
     *         'valueOf' and single string argument
     * @return valueOf method or null if class has not it
     */
    static Method getStringValueOfMethod(Class<?> clazz) {
        try {
            Method method = clazz.getDeclaredMethod("valueOf", String.class);
            return Modifier.isStatic(method.getModifiers()) ? method : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get constructor with single string argument for supplied class.
     *
     * @param clazz
     *         class for discovering to have constructor with single string
     *         argument
     * @return constructor or null if class has not constructor with single
     * string argument
     */
    static Constructor<?> getStringConstructor(Class<?> clazz) {
        try {
            return clazz.getConstructor(String.class);
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * Get generic type for supplied type.
     *
     * @param type
     *         See {@link Type}
     * @return generic type if type is {@link ParameterizedType}, null otherwise
     */
    static Class<?> getGenericType(Type type) {
        if (type instanceof ParameterizedType) {

            ParameterizedType pt = (ParameterizedType)type;
            Type[] genericTypes = pt.getActualTypeArguments();
            if (genericTypes.length == 1) {
                try {
                    // if can't be cast to java.lang.Class thrown Exception
                    return (Class<?>)genericTypes[0];
                } catch (ClassCastException e) {
                    throw new RuntimeException("Unsupported type");
                }
            }
        }
        // not parameterized type
        return null;
    }
}
