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
package org.everrest.core.impl.provider.json;

import org.everrest.core.impl.HelperCache;
import org.everrest.core.impl.provider.json.JsonUtils.Types;

import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author andrew00x */
public class JsonGenerator {
    private static final Collection<String> SKIP_METHODS = new HashSet<String>();

    private static final int CACHE_NUM  = 1 << 3;
    private static final int CACHE_MASK = CACHE_NUM - 1;

    @SuppressWarnings("unchecked")
    private static HelperCache<Class<?>, JsonMethod[]>[] methodsCache = new HelperCache[CACHE_NUM];

    static {
        // Prevent discovering of Java class.
        SKIP_METHODS.add("getClass");
        SKIP_METHODS.add("getMetaClass"); // for groovy
        for (int i = 0; i < CACHE_NUM; i++) {
            methodsCache[i] = new HelperCache<Class<?>, JsonMethod[]>(60 * 1000, 50);
        }
    }

    /**
     * Create JSON array from specified collection.
     *
     * @param collection
     *         source collection
     * @return JSON representation of collection
     * @throws JsonException
     *         if collection can't be transformed in JSON
     *         representation
     */
    public static JsonValue createJsonArray(Collection<?> collection) throws JsonException {
        return createJsonValue(collection);
    }

    /**
     * Create JSON array from specified object. Parameter <code>array</code> must
     * be array.
     *
     * @param array
     *         source array
     * @return JSON representation of array
     * @throws JsonException
     *         if array can't be transformed in JSON representation
     */
    public static JsonValue createJsonArray(Object array) throws JsonException {
        if (array == null) {
            return new NullValue();
        }
        Types t = JsonUtils.getType(array);
        if (t == Types.ARRAY_BOOLEAN || t == Types.ARRAY_BYTE || t == Types.ARRAY_SHORT || t == Types.ARRAY_INT
            || t == Types.ARRAY_LONG || t == Types.ARRAY_FLOAT || t == Types.ARRAY_DOUBLE || t == Types.ARRAY_CHAR
            || t == Types.ARRAY_STRING || t == Types.ARRAY_OBJECT) {
            return createJsonValue(array);
        } else {
            throw new JsonException("Invalid argument, must be array.");
        }
    }

    /**
     * Create JSON object from specified map.
     *
     * @param map
     *         source map
     * @return JSON representation of map
     * @throws JsonException
     *         if map can't be transformed in JSON representation
     */
    public static JsonValue createJsonObjectFromMap(Map<String, ?> map) throws JsonException {
        return createJsonValue(map);
    }

    /**
     * Create JSON object from specified string imply it is JSON object in String
     * format.
     *
     * @param s
     *         source string
     * @return JSON representation of map
     * @throws JsonException
     *         if map can't be transformed in JSON representation
     */
    public JsonValue createJsonObjectFromString(String s) throws JsonException {
        JsonParser parser = new JsonParser();
        parser.parse(new StringReader(s));
        return parser.getJsonObject();
    }

    /**
     * Create JSON object from specified object. Object must be conform with java
     * bean structure.
     *
     * @param object
     *         source object
     * @return JSON representation of object
     * @throws JsonException
     *         if map can't be transformed in JSON representation
     */
    public static JsonValue createJsonObject(Object object) throws JsonException {
        Class<?> clazz = object.getClass();
        JsonValue jsonRootValue = new ObjectValue();
        JsonMethod[] jsonMethods = getJsonMethods(clazz);
        if (jsonMethods != null && jsonMethods.length > 0) {
            for (JsonMethod getMethod : jsonMethods) {
                try {
                    // Get result of invoke method get...
                    Object invokeResult = getMethod.method.invoke(object);
                    if (JsonUtils.getType(invokeResult) != null) {
                        jsonRootValue.addElement(getMethod.field, createJsonValue(invokeResult));
                    } else {
                        jsonRootValue.addElement(getMethod.field, createJsonObject(invokeResult));
                    }
                } catch (InvocationTargetException e) {
                    throw new JsonException(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    throw new JsonException(e.getMessage(), e);
                }
            }
        }
        return jsonRootValue;
    }

    private static JsonMethod[] getJsonMethods(Class<?> clazz) {
         /*
          * Method must be as follow:
          * 1. Name starts from "get" plus at least one character or
          * starts from "is" plus one more character and return boolean type;
          * 2. Must be without parameters;
          * 3. Not be in SKIP_METHODS set.
          */
        HelperCache<Class<?>, JsonMethod[]> partition = methodsCache[clazz.hashCode() & CACHE_MASK];
        synchronized (partition) {
            JsonMethod[] methods = partition.get(clazz);
            if (methods == null) {
                Set<String> transientFieldNames = JsonUtils.getTransientFields(clazz);
                List<JsonMethod> result = new ArrayList<JsonMethod>();
                for (Method method : clazz.getMethods()) {
                    String methodName = method.getName();
                    if (!SKIP_METHODS.contains(methodName) && method.getParameterTypes().length == 0) {
                        Class<?> returnType = method.getReturnType();
                        String field = null;
                        if (methodName.startsWith("get") && methodName.length() > 3) {
                            field = methodName.substring(3);
                        } else if (methodName.startsWith("is") && methodName.length() > 2
                                   && (returnType == Boolean.class || returnType == boolean.class)) {
                            field = methodName.substring(2);
                        }
                        if (field != null) {
                            field = (field.length() > 1) ? Character.toLowerCase(field.charAt(0)) + field.substring(1)
                                                         : field.toLowerCase();
                            if (!transientFieldNames.contains(field)) {
                                result.add(new JsonMethod(method, field));
                            }
                        }
                    }
                }
                partition.put(clazz, methods = result.toArray(new JsonMethod[result.size()]));
            }
            return methods;
        }
    }

    /**
     * Create JsonValue corresponding to Java object.
     *
     * @param object
     *         source object.
     * @return JsonValue.
     * @throws JsonException
     *         if any errors occurs.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static JsonValue createJsonValue(Object object) throws JsonException {
        Types type = JsonUtils.getType(object);
        switch (type) {
            case NULL:
                return new NullValue();
            case BOOLEAN:
                return new BooleanValue((Boolean)object);
            case BYTE:
                return new LongValue((Byte)object);
            case SHORT:
                return new LongValue((Short)object);
            case INT:
                return new LongValue((Integer)object);
            case LONG:
                return new LongValue((Long)object);
            case FLOAT:
                return new DoubleValue((Float)object);
            case DOUBLE:
                return new DoubleValue((Double)object);
            case CHAR:
                return new StringValue(Character.toString((Character)object));
            case STRING:
                return new StringValue((String)object);
            case ENUM:
                return new StringValue(((Enum)object).name());
            case CLASS:
                return new StringValue(((Class)object).getName());
            case ARRAY_BOOLEAN: {
                JsonValue jsonArray = new ArrayValue();
                int length = Array.getLength(object);
                for (int i = 0; i < length; i++) {
                    jsonArray.addElement(new BooleanValue(Array.getBoolean(object, i)));
                }
                return jsonArray;
            }
            case ARRAY_BYTE: {
                JsonValue jsonArray = new ArrayValue();
                int length = Array.getLength(object);
                for (int i = 0; i < length; i++) {
                    jsonArray.addElement(new LongValue(Array.getByte(object, i)));
                }
                return jsonArray;
            }
            case ARRAY_SHORT: {
                JsonValue jsonArray = new ArrayValue();
                int length = Array.getLength(object);
                for (int i = 0; i < length; i++) {
                    jsonArray.addElement(new LongValue(Array.getShort(object, i)));
                }
                return jsonArray;
            }
            case ARRAY_INT: {
                JsonValue jsonArray = new ArrayValue();
                int length = Array.getLength(object);
                for (int i = 0; i < length; i++) {
                    jsonArray.addElement(new LongValue(Array.getInt(object, i)));
                }
                return jsonArray;
            }
            case ARRAY_LONG: {
                JsonValue jsonArray = new ArrayValue();
                int length = Array.getLength(object);
                for (int i = 0; i < length; i++) {
                    jsonArray.addElement(new LongValue(Array.getLong(object, i)));
                }
                return jsonArray;
            }
            case ARRAY_FLOAT: {
                JsonValue jsonArray = new ArrayValue();
                int length = Array.getLength(object);
                for (int i = 0; i < length; i++) {
                    jsonArray.addElement(new DoubleValue(Array.getFloat(object, i)));
                }
                return jsonArray;
            }
            case ARRAY_DOUBLE: {
                JsonValue jsonArray = new ArrayValue();
                int length = Array.getLength(object);
                for (int i = 0; i < length; i++) {
                    jsonArray.addElement(new DoubleValue(Array.getDouble(object, i)));
                }
                return jsonArray;
            }
            case ARRAY_CHAR: {
                JsonValue jsonArray = new ArrayValue();
                int length = Array.getLength(object);
                for (int i = 0; i < length; i++) {
                    jsonArray.addElement(new StringValue(Character.toString(Array.getChar(object, i))));
                }
                return jsonArray;
            }
            case ARRAY_STRING: {
                JsonValue jsonArray = new ArrayValue();
                int length = Array.getLength(object);
                for (int i = 0; i < length; i++) {
                    jsonArray.addElement(new StringValue((String)Array.get(object, i)));
                }
                return jsonArray;
            }
            case ARRAY_OBJECT: {
                JsonValue jsonArray = new ArrayValue();
                int length = Array.getLength(object);
                for (int i = 0; i < length; i++) {
                    Object el = Array.get(object, i);
                    if (JsonUtils.getType(el) != null) {
                        jsonArray.addElement(createJsonValue(el));
                    } else {
                        jsonArray.addElement(createJsonObject(el));
                    }
                }
                return jsonArray;
            }
            case COLLECTION: {
                JsonValue jsonArray = new ArrayValue();
                List<Object> list = new ArrayList<Object>((Collection<?>)object);
                for (Object o : list) {
                    if (JsonUtils.getType(o) != null) {
                        jsonArray.addElement(createJsonValue(o));
                    } else {
                        jsonArray.addElement(createJsonObject(o));
                    }
                }
                return jsonArray;
            }
            case MAP:
                JsonValue jsonObject = new ObjectValue();
                Map<String, Object> map = (Map<String, Object>)object;
                Set<String> keys = map.keySet();
                for (String k : keys) {
                    Object o = map.get(k);
                    if (JsonUtils.getType(o) != null) {
                        jsonObject.addElement(k, createJsonValue(o));
                    } else {
                        jsonObject.addElement(k, createJsonObject(o));
                    }
                }
                return jsonObject;
            default:
                // Must not be here!
                return null;
        }
    }

}
