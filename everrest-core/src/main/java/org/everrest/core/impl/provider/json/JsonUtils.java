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
package org.everrest.core.impl.provider.json;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public final class JsonUtils {
    /** Known types. */
    public enum Types {
        BYTE,
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        BOOLEAN,
        CHAR,
        STRING,
        NULL,
        ARRAY_BYTE,
        ARRAY_SHORT,
        ARRAY_INT,
        ARRAY_LONG,
        ARRAY_FLOAT,
        ARRAY_DOUBLE,
        ARRAY_BOOLEAN,
        ARRAY_CHAR,
        ARRAY_STRING,
        ARRAY_OBJECT,
        COLLECTION,
        MAP,
        ENUM,
        CLASS
    }

    /** Types of Json tokens. */
    public enum JsonToken {
        object,
        array,
        key,
        value
    }

    /** Map of known types. */
    private static final Map<String, Types> KNOWN_TYPES = new HashMap<>();

    static {
        KNOWN_TYPES.put(Boolean.class.getName(), Types.BOOLEAN);
        KNOWN_TYPES.put(Byte.class.getName(), Types.BYTE);
        KNOWN_TYPES.put(Short.class.getName(), Types.SHORT);
        KNOWN_TYPES.put(Integer.class.getName(), Types.INT);
        KNOWN_TYPES.put(Long.class.getName(), Types.LONG);
        KNOWN_TYPES.put(Float.class.getName(), Types.FLOAT);
        KNOWN_TYPES.put(Double.class.getName(), Types.DOUBLE);
        KNOWN_TYPES.put(Character.class.getName(), Types.CHAR);
        KNOWN_TYPES.put(String.class.getName(), Types.STRING);
        KNOWN_TYPES.put(Class.class.getName(), Types.CLASS);
        KNOWN_TYPES.put("boolean", Types.BOOLEAN);
        KNOWN_TYPES.put("byte", Types.BYTE);
        KNOWN_TYPES.put("short", Types.SHORT);
        KNOWN_TYPES.put("int", Types.INT);
        KNOWN_TYPES.put("long", Types.LONG);
        KNOWN_TYPES.put("float", Types.FLOAT);
        KNOWN_TYPES.put("double", Types.DOUBLE);
        KNOWN_TYPES.put("char", Types.CHAR);
        KNOWN_TYPES.put("null", Types.NULL);
        KNOWN_TYPES.put(boolean[].class.getName(), Types.ARRAY_BOOLEAN);
        KNOWN_TYPES.put(byte[].class.getName(), Types.ARRAY_BYTE);
        KNOWN_TYPES.put(short[].class.getName(), Types.ARRAY_SHORT);
        KNOWN_TYPES.put(int[].class.getName(), Types.ARRAY_INT);
        KNOWN_TYPES.put(long[].class.getName(), Types.ARRAY_LONG);
        KNOWN_TYPES.put(double[].class.getName(), Types.ARRAY_DOUBLE);
        KNOWN_TYPES.put(float[].class.getName(), Types.ARRAY_FLOAT);
        KNOWN_TYPES.put(char[].class.getName(), Types.ARRAY_CHAR);
        KNOWN_TYPES.put(String[].class.getName(), Types.ARRAY_STRING);
    }

    /**
     * Transform Java String to JSON string.
     *
     * @param string
     *         source String.
     * @return result.
     */
    public static String getJsonString(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }
        StringBuilder jsonString = new StringBuilder();
        jsonString.append("\"");
        char[] charArray = string.toCharArray();
        for (char c : charArray) {
            switch (c) {
                case '\n':
                    jsonString.append("\\n");
                    break;
                case '\r':
                    jsonString.append("\\r");
                    break;
                case '\t':
                    jsonString.append("\\t");
                    break;
                case '\b':
                    jsonString.append("\\b");
                    break;
                case '\f':
                    jsonString.append("\\f");
                    break;
                case '\\':
                    jsonString.append("\\\\");
                    break;
                case '"':
                    jsonString.append("\\\"");
                    break;
                default:
                    if (c < '\u0010') {
                        jsonString.append("\\u000").append(Integer.toHexString(c));
                    } else if ((c < '\u0020' && c > '\u0009') || (c >= '\u0080' && c < '\u00a0')) {
                        jsonString.append("\\u00").append(Integer.toHexString(c));
                    } else if (c >= '\u2000' && c < '\u2100') {
                        jsonString.append("\\u").append(Integer.toHexString(c));
                    } else {
                        jsonString.append(c);
                    }
                    break;
            }
        }
        jsonString.append("\"");
        return jsonString.toString();
    }

    /**
     * Check is given Class is known.
     *
     * @param clazz
     *         Class.
     * @return true if Class is known, false otherwise.
     */
    public static boolean isKnownType(Class<?> clazz) {
        return KNOWN_TYPES.get(clazz.getName()) != null;
    }

    /**
     * Get 'type' of Object. @see {@link #KNOWN_TYPES} .
     *
     * @param o
     *         Object.
     * @return 'type'.
     */
    public static Types getType(Object o) {
        if (o == null) {
            return Types.NULL;
        }
        if (KNOWN_TYPES.get(o.getClass().getName()) != null) {
            return KNOWN_TYPES.get(o.getClass().getName());
        }
        if (o instanceof Enum) {
            return Types.ENUM;
        }
        if (o instanceof Object[]) {
            return Types.ARRAY_OBJECT;
        }
        if (o instanceof Collection) {
            return Types.COLLECTION;
        }
        if (o instanceof Map) {
            return Types.MAP;
        }
        return null;
    }

    /**
     * Get 'type' of Class. @see {@link #KNOWN_TYPES} .
     *
     * @param clazz
     *         Class.
     * @return 'type'.
     */
    public static Types getType(Class<?> clazz) {
        if (KNOWN_TYPES.get(clazz.getName()) != null) {
            return KNOWN_TYPES.get(clazz.getName());
        }
        if (Enum.class.isAssignableFrom(clazz)) {
            return Types.ENUM;
        }
        if (clazz.isArray()) {
            return Types.ARRAY_OBJECT;
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            return Types.COLLECTION;
        }
        if (Map.class.isAssignableFrom(clazz)) {
            return Types.MAP;
        }
        return null;
    }

    /**
     * Check fields in class which marked as 'transient' or annotated with
     * {@link JsonTransient} annotation . Transient fields will be not serialized
     * in JSON representation.
     *
     * @param aClass
     *         the class.
     * @return set of fields which must be skipped.
     */
    public static Set<String> getTransientFields(Class<?> aClass) {
        Set<String> transientFields = new HashSet<>();
        Class<?> superClass = aClass;
        while (superClass != null && superClass != Object.class) {
            for (Field field : superClass.getDeclaredFields()) {
                if (Modifier.isTransient(field.getModifiers()) || field.isAnnotationPresent(JsonTransient.class)) {
                    transientFields.add(field.getName());
                }
            }
            superClass = superClass.getSuperclass();
        }
        return transientFields;
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T createProxy(Class<T> anInterface) {
        if (anInterface == null) {
            throw new IllegalArgumentException();
        }
        if (anInterface.isInterface()) {
            return (T)Proxy.newProxyInstance(anInterface.getClassLoader(), new Class[]{anInterface}, new ProxyObject(anInterface));
        }
        throw new IllegalArgumentException(String.format("Type '%s' is not interface. ", anInterface.getSimpleName()));
    }

    static String getFieldName(Method method) {
        String methodName = method.getName();
        Class<?> returnType = method.getReturnType();
        String fieldName = null;
        if (methodName.startsWith("set") && methodName.length() > 3) {
            fieldName = methodName.substring(3);
        } else if (methodName.startsWith("get") && methodName.length() > 3) {
            fieldName = methodName.substring(3);
        } else if (methodName.startsWith("is") && methodName.length() > 2 && (returnType == Boolean.class || returnType == boolean.class)) {
            fieldName = methodName.substring(2);
        }
        if (fieldName != null) {
            fieldName = (fieldName.length() > 1)
                        ? Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1)
                        : fieldName.toLowerCase();
        }
        return fieldName;
    }


    private static final class ProxyObject implements InvocationHandler {
        private static final Set<String> IGNORED_METHODS = newHashSet("getClass", "getMetaClass", "setMetaClass");

        private final Map<String, Object> values;
        private final Class<?> anInterface;

        private ProxyObject(Class<?> anInterface) {
            this.anInterface = anInterface;
            values = new HashMap<>();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String key = getKey(method);
            if (key != null) {
                if (args == null) {
                    return getValue(key, method);
                }
                values.put(key, args[0]);
            } else if ("toString".equals(method.getName()) && args == null) {
                return invokeToString();
            }
            return null;
        }

        private Object invokeToString() {
            Method[] allMethods = anInterface.getMethods();
            ToStringHelper toStringHelper = MoreObjects.toStringHelper(anInterface);
            for (Method method : allMethods) {
                String key;
                if ((key = getKey(method)) != null && method.getParameterTypes().length == 0) {
                    toStringHelper.add(key, getValue(key, method));
                }
            }
            return toStringHelper.toString();
        }

        private Object getValue(String key, Method method) {
            Object value = values.get(key);
            if (value == null && method.getReturnType().isPrimitive()) {
                value = getDefaultValue(method.getReturnType());
            }
            return value;
        }

        private Object getDefaultValue(Class<?> valueType) {
            if (Boolean.TYPE == valueType) {
                return false;
            } else if (Byte.TYPE == valueType) {
                return (byte)0;
            } else if (Short.TYPE == valueType) {
                return (short)0;
            } else if (Character.TYPE == valueType) {
                return (char)0;
            } else if (Integer.TYPE == valueType) {
                return 0;
            } else if (Long.TYPE == valueType) {
                return 0L;
            } else if (Float.TYPE == valueType) {
                return 0.0F;
            } else if (Double.TYPE == valueType) {
                return 0.0;
            }
            return null;
        }

        private String getKey(Method method) {
            if (isIgnoredMethod(method)) {
                return null;
            }
            return getFieldName(method);
        }

        private boolean isIgnoredMethod(Method method) {
            return IGNORED_METHODS.contains(method.getName()) || method.getParameterTypes().length > 1;
        }
    }

    private JsonUtils() {
    }
}
