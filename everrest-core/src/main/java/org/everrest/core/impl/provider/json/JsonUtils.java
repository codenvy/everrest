/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.core.impl.provider.json;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** @author andrew00x */
public final class JsonUtils {

    static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    /** Known types. */
    public enum Types {

        /** Byte. */
        BYTE,

        /** Short. */
        SHORT,

        /** Integer. */
        INT,

        /** Long. */
        LONG,

        /** Float. */
        FLOAT,

        /** Double. */
        DOUBLE,

        /** Boolean. */
        BOOLEAN,

        /** Char. */
        CHAR,

        /** String. */
        STRING,

        /** Corresponding to null value. */
        NULL,

        /** Array of Bytes. */
        ARRAY_BYTE,

        /** Array of Shorts. */
        ARRAY_SHORT,

        /** Array of Integers. */
        ARRAY_INT,

        /** Array of Longs. */
        ARRAY_LONG,

        /** Array of Floats. */
        ARRAY_FLOAT,

        /** Array of Doubles. */
        ARRAY_DOUBLE,

        /** Array of Boolean. */
        ARRAY_BOOLEAN,

        /** Array of Chars. */
        ARRAY_CHAR,

        /** Array of Strings. */
        ARRAY_STRING,

        /** Array of Java Objects (beans). */
        ARRAY_OBJECT,

        /** Collection. */
        COLLECTION,

        /** Map. */
        MAP,

        /** Enum. */
        ENUM,

        /** java.lang.Class */
        CLASS
    }

    /** Types of Json tokens. */
    public enum JsonToken {
        /** JSON object, "key":{value1, ... } . */
        object,

        /** JSON array "key":[value1, ... ] . */
        array,

        /** Key. */
        key,

        /** Value. */
        value
    }

    /** Map of known types. */
    private static final Map<String, Types> KNOWN_TYPES = new HashMap<String, Types>();

    static {
        // wrappers for primitive types
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

        // primitive types
        KNOWN_TYPES.put("boolean", Types.BOOLEAN);

        KNOWN_TYPES.put("byte", Types.BYTE);
        KNOWN_TYPES.put("short", Types.SHORT);
        KNOWN_TYPES.put("int", Types.INT);
        KNOWN_TYPES.put("long", Types.LONG);
        KNOWN_TYPES.put("float", Types.FLOAT);
        KNOWN_TYPES.put("double", Types.DOUBLE);

        KNOWN_TYPES.put("char", Types.CHAR);

        KNOWN_TYPES.put("null", Types.NULL);

        // arrays
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
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        char[] charArray = string.toCharArray();
        for (char c : charArray) {
            switch (c) {
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                default:
                    if (c < '\u0010') {
                        sb.append("\\u000").append(Integer.toHexString(c));
                    } else if ((c < '\u0020' && c > '\u0009') || (c >= '\u0080' && c < '\u00a0')) {
                        sb.append("\\u00").append(Integer.toHexString(c));
                    } else if (c >= '\u2000' && c < '\u2100') {
                        sb.append("\\u").append(Integer.toHexString(c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    /**
     * Check is given Object is known.
     *
     * @param o
     *         Object.
     * @return true if Object is known, false otherwise.
     */
    public static boolean isKnownType(Object o) {
        return (o == null) || isKnownType(o.getClass());
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
     * @param clazz
     *         the class.
     * @return set of fields which must be skipped.
     */
    public static Set<String> getTransientFields(Class<?> clazz) {
        Set<String> set = new HashSet<String>();
        Class<?> myClazz = clazz;
        while (myClazz != null && myClazz != Object.class) {
            for (Field f : myClazz.getDeclaredFields()) {
                if (Modifier.isTransient(f.getModifiers()) || f.getAnnotation(JsonTransient.class) != null) {
                    set.add(f.getName());
                }
            }
            myClazz = myClazz.getSuperclass();
        }
        return set;
    }

    public static <T> T createProxy(Class<T> interf) {
        if (interf == null) {
            throw new NullPointerException();
        }
        if (!interf.isInterface()) {
            throw new IllegalArgumentException("Type '" + interf.getSimpleName() + "' is not interface. ");
        }
        return (T)Proxy.newProxyInstance(interf.getClassLoader(), new Class[]{interf}, new ProxyObject(interf));
    }

    private static final class ProxyObject implements InvocationHandler {
        private final Map<String, Object> values = new HashMap<String, Object>();
        private final Class<?> interf;

        private ProxyObject(Class<?> interf) {
            this.interf = interf;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String key = key(method);
            if (key != null) {
                // It is getter if there is no argument.
                if (args == null) {
                    return value(key, method);
                }

                // Setter.
                values.put(key, args[0]);
                return null;
            } else if ("toString".equals(method.getName())) {
                // EVERREST-41
                // Simplify viewing all properties of object.
                StringBuilder buf = new StringBuilder();
                buf.append('{');
                Method[] allMethods = interf.getMethods();
                for (int i = 0, length = allMethods.length; i < length; i++) {
                    // Check all 'getters'. Such method must have not parameters
                    // and must have name 'getXXX' or 'isXXX' (for boolean only).
                    if (allMethods[i].getParameterTypes().length == 0 && (key = key(allMethods[i])) != null) {
                        if (i > 0) {
                            buf.append(',');
                            buf.append(' ');
                        }
                        buf.append(key);
                        buf.append('=');
                        buf.append(value(key, allMethods[i]));
                    }
                }
                return buf.append('}').toString();
            }
            // Neither toString nor getter nor setter. Cannot process such methods.
            return null;
        }

        private Object value(String key, Method method) {
            Object value = values.get(key);
            Class<?> valueType;
            if (value != null) {
                return value;
            } else if ((valueType = method.getReturnType()).isPrimitive()) {
                // Cannot return null for primitive types return default value instead.
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
                    return 0l;
                } else if (Float.TYPE == valueType) {
                    return 0.0f;
                } else if (Double.TYPE == valueType) {
                    return 0.0d;
                }
            }
            return null;
        }

        private String key(Method method) {
            String name = method.getName();
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length > 1 || "getClass".equals(name) || "getMetaClass".equals(name) || "setMetaClass".equals(name)) {
                /* Neither getter nor setter if has more then one argument. */
                return null;
            }

            String key = null;
            if (parameters.length == 1) {
                if (name.startsWith("set") && name.length() > 3) {
                    key = name.substring(3);
                }
            } else {
                if (name.startsWith("get") && name.length() > 3) {
                    key = name.substring(3);
                } else if (name.startsWith("is") && name.length() > 2
                           && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
                    key = name.substring(2);
                }
            }
            if (key != null) {
                key = key.length() > 1 ? Character.toLowerCase(key.charAt(0)) + key.substring(1) : key.toLowerCase();
            }
            return key;
        }
    }

    /** Must not be created. */
    private JsonUtils() {
    }
}
