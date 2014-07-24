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

import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Construct a primitive type from string value.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class PrimitiveTypeProducer extends BaseTypeProducer {

    /**
     * Primitive types map, this map contains all primitive java types except
     * char because {@link Character} has not static method valueOf with String
     * parameter.
     */
    static final Map<String, Class<?>> PRIMITIVE_TYPES_MAP;

    /**
     * Default values for primitive types. This value will be used if not found
     * required parameter in request and default value
     * {@link javax.ws.rs.DefaultValue} is null.
     */
    private static final Map<String, Object> PRIMITIVE_TYPE_DEFAULTS;

    static {
        Map<String, Class<?>> m = new HashMap<String, Class<?>>(7);
        m.put("boolean", Boolean.class);
        m.put("byte", Byte.class);
        m.put("short", Short.class);
        m.put("int", Integer.class);
        m.put("long", Long.class);
        m.put("float", Float.class);
        m.put("double", Double.class);
        PRIMITIVE_TYPES_MAP = Collections.unmodifiableMap(m);
    }

    static {
        Map<String, Object> m = new HashMap<String, Object>(7);
        m.put("boolean", false);
        m.put("byte", (byte)0);
        m.put("short", (short)0);
        m.put("int", 0);
        m.put("long", 0l);
        m.put("float", 0.0f);
        m.put("double", 0.0d);
        PRIMITIVE_TYPE_DEFAULTS = Collections.unmodifiableMap(m);
    }

    /** Class of object which will be created. */
    private Class<?> clazz;

    /** This will be used if defaultValue is null. */
    private Object defaultDefaultValue;

    /**
     * Construct PrimitiveTypeProducer.
     *
     * @param clazz
     *         class of object
     */
    PrimitiveTypeProducer(Class<?> clazz) {
        this.clazz = clazz;

        /**
         * If class is represents primitive type then method
         * {@link Class#getName()} return name of primitive type. See
         * #PRIMITIVE_TYPES_MAP.
         */
        this.defaultDefaultValue = PRIMITIVE_TYPE_DEFAULTS.get(clazz.getName());
    }

    /** {@inheritDoc} */
    @Override
    protected Object createValue(String value) throws Exception {

        /**
         * If class is represents primitive type then method
         * {@link Class#getName()} return name of primitive type. See
         * #PRIMITIVE_TYPES_MAP.
         */
        Class<?> c = PRIMITIVE_TYPES_MAP.get(clazz.getName());
        Method method = ParameterHelper.getStringValueOfMethod(c);

        // invoke valueOf method for creation object
        return method.invoke(null, value);
    }

    /** {@inheritDoc} */
    @Override
    public Object createValue(String param, MultivaluedMap<String, String> values, String defaultValue) throws Exception {
        String value = values.getFirst(param);

        if (value != null) {
            return createValue(value);
        } else if (defaultValue != null) {
            return createValue(defaultValue);
        }

        return this.defaultDefaultValue;
    }
}
