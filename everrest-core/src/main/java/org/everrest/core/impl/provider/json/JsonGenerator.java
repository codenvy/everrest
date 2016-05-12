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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.everrest.core.impl.provider.json.JsonUtils.Types;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Throwables.propagateIfPossible;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.everrest.core.impl.provider.json.JsonUtils.Types.ARRAY_BOOLEAN;
import static org.everrest.core.impl.provider.json.JsonUtils.Types.ARRAY_BYTE;
import static org.everrest.core.impl.provider.json.JsonUtils.Types.ARRAY_CHAR;
import static org.everrest.core.impl.provider.json.JsonUtils.Types.ARRAY_DOUBLE;
import static org.everrest.core.impl.provider.json.JsonUtils.Types.ARRAY_FLOAT;
import static org.everrest.core.impl.provider.json.JsonUtils.Types.ARRAY_INT;
import static org.everrest.core.impl.provider.json.JsonUtils.Types.ARRAY_LONG;
import static org.everrest.core.impl.provider.json.JsonUtils.Types.ARRAY_OBJECT;
import static org.everrest.core.impl.provider.json.JsonUtils.Types.ARRAY_SHORT;
import static org.everrest.core.impl.provider.json.JsonUtils.Types.ARRAY_STRING;
import static org.everrest.core.impl.provider.json.JsonUtils.Types.COLLECTION;
import static org.everrest.core.impl.provider.json.JsonUtils.Types.MAP;
import static org.everrest.core.impl.provider.json.JsonUtils.getFieldName;
import static org.everrest.core.impl.provider.json.JsonUtils.getTransientFields;

public class JsonGenerator {
    private static final Collection<String> SKIP_METHODS = newHashSet("getClass", "getMetaClass");

    private static LoadingCache<Class<?>, JsonMethod[]> methodsCache = CacheBuilder.newBuilder()
                                                                                   .concurrencyLevel(8)
                                                                                   .maximumSize(256)
                                                                                   .expireAfterAccess(10, MINUTES)
                                                                                   .build(new CacheLoader<Class<?>, JsonMethod[]>() {
                                                                                       @Override
                                                                                       public JsonMethod[] load(Class<?> aClass)
                                                                                               throws Exception {
                                                                                           return getGetters(aClass);
                                                                                       }
                                                                                   });

    private static JsonMethod[] getGetters(Class<?> aClass) {
        Set<String> transientFieldNames = getTransientFields(aClass);
        List<JsonMethod> result = new ArrayList<>();
        for (Method method : aClass.getMethods()) {
            if (shouldBeProcessed(method)) {
                String field = getFieldName(method);
                if (!transientFieldNames.contains(field)) {
                    result.add(new JsonMethod(method, field));
                }
            }
        }
        return result.toArray(new JsonMethod[result.size()]);
    }

    private static boolean shouldBeProcessed(Method method) {
        return !SKIP_METHODS.contains(method.getName()) && isGetter(method);
    }

    private static boolean isGetter(Method method) {
        String methodName = method.getName();
        Class<?> returnType = method.getReturnType();
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return method.getParameterTypes().length == 0;
        } else if (methodName.startsWith("is") && methodName.length() > 2) {
            return method.getParameterTypes().length == 0
                   && (returnType == Boolean.class || returnType == boolean.class);
        }
        return false;
    }

    /* ------------------------------------------------------------------------------ */

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
        if (collection == null) {
            return new NullValue();
        }
        return createJsonValue(collection, COLLECTION);
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
        Types type = JsonUtils.getType(array);
        if (type == ARRAY_BOOLEAN
            || type == ARRAY_BYTE
            || type == ARRAY_SHORT
            || type == ARRAY_INT
            || type == ARRAY_LONG
            || type == ARRAY_FLOAT
            || type == ARRAY_DOUBLE
            || type == ARRAY_CHAR
            || type == ARRAY_STRING
            || type == ARRAY_OBJECT) {

            return createJsonValue(array, type);
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
        if (map == null) {
            return new NullValue();
        }
        return createJsonValue(map, MAP);
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
        if (object == null) {
            return new NullValue();
        }
        Class<?> aClass = object.getClass();
        JsonValue jsonRootValue = new ObjectValue();
        JsonMethod[] getters;
        try {
            getters = methodsCache.get(aClass);
        } catch (ExecutionException e) {
            propagateIfPossible(e.getCause());
            throw new JsonException(e.getCause());
        }
        for (JsonMethod getter : getters) {
            try {
                Object getterResult = getter.method.invoke(object);
                Types getterResultType = JsonUtils.getType(getterResult);
                if (getterResultType == null) {
                    jsonRootValue.addElement(getter.field, createJsonObject(getterResult));
                } else {
                    jsonRootValue.addElement(getter.field, createJsonValue(getterResult, getterResultType));
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new JsonException(e.getMessage(), e);
            }
        }
        return jsonRootValue;
    }

    @SuppressWarnings({"unchecked"})
    private static JsonValue createJsonValue(Object object, Types type) throws JsonException {
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
                    Object item = Array.get(object, i);
                    Types itemType = JsonUtils.getType(item);
                    if (itemType == null) {
                        jsonArray.addElement(createJsonObject(item));
                    } else {
                        jsonArray.addElement(createJsonValue(item, itemType));
                    }
                }
                return jsonArray;
            }
            case COLLECTION: {
                JsonValue jsonArray = new ArrayValue();
                List<Object> list = new ArrayList<>((Collection<?>)object);
                for (Object item : list) {
                    Types itemType = JsonUtils.getType(item);
                    if (itemType == null) {
                        jsonArray.addElement(createJsonObject(item));
                    } else {
                        jsonArray.addElement(createJsonValue(item, itemType));
                    }
                }
                return jsonArray;
            }
            case MAP:
                JsonValue jsonObject = new ObjectValue();
                Map<String, Object> map = (Map<String, Object>)object;
                Set<String> keys = map.keySet();
                for (String key : keys) {
                    Object item = map.get(key);
                    Types itemType = JsonUtils.getType(item);
                    if (itemType == null) {
                        jsonObject.addElement(key, createJsonObject(item));
                    } else {
                        jsonObject.addElement(key, createJsonValue(item, itemType));
                    }
                }
                return jsonObject;
            default:
                throw new IllegalStateException(String.format("Unsupported type %s", type));
        }
    }

}
