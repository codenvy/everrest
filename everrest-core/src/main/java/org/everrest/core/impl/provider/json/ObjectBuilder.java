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

import org.everrest.core.impl.HelperCache;
import org.everrest.core.impl.provider.json.JsonUtils.Types;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author andrew00x */
public class ObjectBuilder {
    private static final Collection<String> SKIP_METHODS = new HashSet<String>();

    private static final int CACHE_NUM  = 1 << 3;
    private static final int CACHE_MASK = CACHE_NUM - 1;

    @SuppressWarnings("unchecked")
    private static HelperCache<Class<?>, Constructor<?>>[] constructorsCache = new HelperCache[CACHE_NUM];
    @SuppressWarnings("unchecked")
    private static HelperCache<Class<?>, JsonMethod[]>[]   methodsCache      = new HelperCache[CACHE_NUM];

    static {
        SKIP_METHODS.add("setMetaClass"); // for groovy
        for (int i = 0; i < CACHE_NUM; i++) {
            methodsCache[i] = new HelperCache<Class<?>, JsonMethod[]>(60 * 1000, 50);
            constructorsCache[i] = new HelperCache<Class<?>, Constructor<?>>(60 * 1000, 50);
        }
    }

    /**
     * Create array of Java Object from JSON source include multi-dimension
     * array.
     *
     * @param clazz
     *         the Class of target Object.
     * @param jsonArray
     *         the JSON representation of array
     * @return result array
     * @throws JsonException
     *         if any errors occurs
     */
    public static Object createArray(Class<?> clazz, JsonValue jsonArray) throws JsonException {
        Object array = null;
        if (jsonArray != null && !jsonArray.isNull()) {
            Class<?> componentType = clazz.getComponentType();
            array = Array.newInstance(componentType, jsonArray.size());
            Iterator<JsonValue> values = jsonArray.getElements();
            int i = 0;

            if (componentType.isArray()) {
                if (JsonUtils.isKnownType(componentType)) {
                    while (values.hasNext()) {
                        JsonValue v = values.next();
                        Array.set(array, i++, createObjectKnownTypes(componentType, v));
                    }
                } else {
                    while (values.hasNext()) {
                        JsonValue v = values.next();
                        Array.set(array, i++, createArray(componentType, v));
                    }
                }
            } else {
                if (JsonUtils.isKnownType(componentType)) {
                    while (values.hasNext()) {
                        JsonValue v = values.next();
                        Array.set(array, i++, createObjectKnownTypes(componentType, v));
                    }
                } else {
                    while (values.hasNext()) {
                        JsonValue v = values.next();
                        Array.set(array, i++, createObject(componentType, v));
                    }
                }
            }
        }
        return array;
    }

    /**
     * Create instance of <code>collectionClass</code> from JSON representation.
     * If <code>collectionClass</code> is interface then appropriate
     * implementation of interface will be returned.
     *
     * @param collectionClass
     *         collection type
     * @param genericType
     *         generic type of collection
     * @param jsonArray
     *         the JSON representation of collection
     * @return result collection
     * @throws JsonException
     *         if any errors occurs
     */
    @SuppressWarnings("unchecked")
    public static <T extends Collection<?>> T createCollection(Class<T> collectionClass, Type genericType, JsonValue jsonArray)
            throws JsonException {
        T collection = null;
        if (jsonArray != null && !jsonArray.isNull()) {
            Class elementClass;
            Type elementType;
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType)genericType;
                elementType = parameterizedType.getActualTypeArguments()[0];
                if (elementType instanceof Class) {
                    elementClass = (Class)elementType;
                } else if (elementType instanceof ParameterizedType) {
                    elementClass = (Class)((ParameterizedType)elementType).getRawType();
                } else {
                    throw new JsonException("This type of Collection can't be restored from JSON source. "
                                            + "\nCollection is parameterized by wrong Type: " + parameterizedType + ".");
                }
            } else {
                throw new JsonException("Collection is not parameterized. Collection<?> is not supported. ");
            }

            Constructor<? extends T> constructor = null;
            if (collectionClass.isInterface() || Modifier.isAbstract(collectionClass.getModifiers())) {
                Class impl = null;
                try {
                    impl = ArrayList.class.asSubclass(collectionClass);
                } catch (ClassCastException e1) {
                    try {
                        impl = HashSet.class.asSubclass(collectionClass);
                    } catch (ClassCastException e2) {
                        try {
                            impl = LinkedList.class.asSubclass(collectionClass);
                        } catch (ClassCastException ignored) {
                        }
                    }
                }
                if (impl != null) {
                    constructor = getConstructor(impl, Collection.class);
                }
            } else {
                constructor = getConstructor(collectionClass, Collection.class);
            }

            if (constructor == null) {
                throw new JsonException("Can't find satisfied constructor for : " + collectionClass);
            }

            ArrayList<Object> sourceCollection = new ArrayList<Object>(jsonArray.size());
            Iterator<JsonValue> values = jsonArray.getElements();
            Types jsonValueType = JsonUtils.getType(elementClass);
            while (values.hasNext()) {
                JsonValue v = values.next();
                if (jsonValueType == null) {
                    sourceCollection.add(createObject(elementClass, v));
                } else {
                    switch (jsonValueType) {
                        case BYTE:
                        case SHORT:
                        case INT:
                        case LONG:
                        case FLOAT:
                        case DOUBLE:
                        case BOOLEAN:
                        case CHAR:
                        case STRING:
                        case NULL:
                        case ARRAY_BYTE:
                        case ARRAY_SHORT:
                        case ARRAY_INT:
                        case ARRAY_LONG:
                        case ARRAY_FLOAT:
                        case ARRAY_DOUBLE:
                        case ARRAY_BOOLEAN:
                        case ARRAY_CHAR:
                        case ARRAY_STRING:
                        case ARRAY_OBJECT:
                        case CLASS:
                            sourceCollection.add(createObjectKnownTypes(elementClass, v));
                            break;
                        case COLLECTION:
                            sourceCollection.add(createCollection(elementClass, elementType, v));
                            break;
                        case MAP:
                            sourceCollection.add(createObject(elementClass, elementType, v));
                            break;
                        case ENUM:
                            sourceCollection.add(createEnum(elementClass, v));
                            break;
                    }
                }
            }
            try {
                collection = constructor.newInstance(sourceCollection);
            } catch (Exception e) {
                throw new JsonException(e.getMessage(), e);
            }
        }
        return collection;
    }

    /**
     * Create instance of <code>mapClass</code> from JSON representation. If
     * <code>mapClass</code> is interface then appropriate implementation of
     * interface will be returned.
     *
     * @param mapClass
     *         map type
     * @param genericType
     *         actual type of map
     * @param jsonObject
     *         source JSON object
     * @return map
     * @throws JsonException
     *         if any errors occurs
     */
    @SuppressWarnings("unchecked")
    public static <T extends Map<String, ?>> T createObject(Class<T> mapClass, Type genericType, JsonValue jsonObject)
            throws JsonException {
        T map = null;
        if (jsonObject != null && !jsonObject.isNull()) {
            Class valueClass;
            Type valueType;
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType)genericType;
                if (!String.class.isAssignableFrom((Class)parameterizedType.getActualTypeArguments()[0])) {
                    throw new JsonException("Key of Map must be String. ");
                }
                valueType = parameterizedType.getActualTypeArguments()[1];
                if (valueType instanceof Class) {
                    valueClass = (Class)valueType;
                } else if (valueType instanceof ParameterizedType) {
                    valueClass = (Class)((ParameterizedType)valueType).getRawType();
                } else {
                    throw new JsonException("This type of Map can't be restored from JSON source."
                                            + "\nMap is parameterized by wrong Type: " + parameterizedType + ".");
                }
            } else {
                throw new JsonException("Map is not parameterized. Map<Sting, ?> is not supported.");
            }
            Constructor<? extends T> constructor = null;
            if (mapClass.isInterface() || Modifier.isAbstract(mapClass.getModifiers())) {
                Class impl = null;
                try {
                    impl = HashMap.class.asSubclass(mapClass);
                } catch (ClassCastException e1) {
                    try {
                        impl = LinkedHashMap.class.asSubclass(mapClass);
                    } catch (ClassCastException e2) {
                        try {
                            impl = Hashtable.class.asSubclass(mapClass);
                        } catch (ClassCastException ignored) {
                        }
                    }
                }
                if (impl != null) {
                    constructor = getConstructor(impl, Map.class);
                }
            } else {
                constructor = getConstructor(mapClass, Map.class);
            }

            if (constructor == null) {
                throw new JsonException("Can't find satisfied constructor for : " + mapClass);
            }

            Types jsonValueType = JsonUtils.getType(valueClass);
            HashMap<String, Object> sourceMap = new HashMap<String, Object>(jsonObject.size());
            Iterator<String> keys = jsonObject.getKeys();
            while (keys.hasNext()) {
                String k = keys.next();
                JsonValue v = jsonObject.getElement(k);
                if (jsonValueType == null) {
                    sourceMap.put(k, createObject(valueClass, v));
                } else {
                    switch (jsonValueType) {
                        case BYTE:
                        case SHORT:
                        case INT:
                        case LONG:
                        case FLOAT:
                        case DOUBLE:
                        case BOOLEAN:
                        case CHAR:
                        case STRING:
                        case NULL:
                        case ARRAY_BYTE:
                        case ARRAY_SHORT:
                        case ARRAY_INT:
                        case ARRAY_LONG:
                        case ARRAY_FLOAT:
                        case ARRAY_DOUBLE:
                        case ARRAY_BOOLEAN:
                        case ARRAY_CHAR:
                        case ARRAY_STRING:
                        case ARRAY_OBJECT:
                        case CLASS:
                            sourceMap.put(k, createObjectKnownTypes(valueClass, v));
                            break;
                        case COLLECTION:
                            sourceMap.put(k, createCollection(valueClass, valueType, v));
                            break;
                        case MAP:
                            sourceMap.put(k, createObject(valueClass, valueType, v));
                            break;
                        case ENUM:
                            sourceMap.put(k, createEnum(valueClass, v));
                            break;
                    }
                }
            }
            try {
                map = constructor.newInstance(sourceMap);
            } catch (Exception e) {
                throw new JsonException(e.getMessage(), e);
            }
        }
        return map;
    }

    /**
     * Create Java Bean from Json Source.
     *
     * @param clazz
     *         the Class of target Object.
     * @param jsonValue
     *         the Json representation.
     * @return Object.
     * @throws JsonException
     *         if any errors occurs.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T createObject(Class<T> clazz, JsonValue jsonValue) throws JsonException {
        if (jsonValue == null || jsonValue.isNull()) {
            return null;
        }

        Types type = JsonUtils.getType(clazz);
        if (type == Types.ENUM) {
            // Enum is not instantiable via CLass.getInstance().
            // This is used when enum is member of array or collection.
            return (T)createEnum(clazz, jsonValue);
        }

        if (!jsonValue.isObject()) {
            throw new JsonException("Unsupported type of jsonValue. ");
        }

        T object;
        if (clazz.isInterface()) {
            object = JsonUtils.createProxy(clazz);
        } else {
            final Constructor<T> constructor = getConstructor(clazz);
            if (constructor == null) {
                throw new JsonException("Can't find satisfied constructor for : " + clazz);
            }
            try {
                object = constructor.newInstance();
            } catch (Exception e) {
                throw new JsonException("Unable instantiate object. " + e.getMessage(), e);
            }
        }

        JsonMethod[] methods = getJsonMethods(clazz);
        if (methods != null && methods.length > 0) {
            for (JsonMethod setMethod : methods) {
                JsonValue childJsonValue = jsonValue.getElement(setMethod.field);
                if (childJsonValue != null) {
                    try {
                        final Class paramClass = setMethod.method.getParameterTypes()[0];
                        if (JsonUtils.isKnownType(paramClass)) {
                            setMethod.method.invoke(object, createObjectKnownTypes(paramClass, childJsonValue));
                        } else {
                            Types parameterType = JsonUtils.getType(paramClass);
                            // other type Collection, Map or Object[].
                            if (parameterType != null) {
                                if (parameterType == Types.ENUM) {
                                    setMethod.method.invoke(object, createEnum(paramClass, childJsonValue));
                                } else if (parameterType == Types.ARRAY_OBJECT) {
                                    setMethod.method.invoke(object, createArray(paramClass, childJsonValue));
                                } else if (parameterType == Types.COLLECTION) {
                                    setMethod.method.invoke(object, createCollection(paramClass,
                                                                                     setMethod.method.getGenericParameterTypes()[0],
                                                                                     childJsonValue));
                                } else if (parameterType == Types.MAP) {
                                    setMethod.method.invoke(object, createObject(paramClass,
                                                                                 setMethod.method.getGenericParameterTypes()[0],
                                                                                 childJsonValue));
                                } else {
                                    throw new JsonException("Can't restore parameter of method : " + clazz.getName() + "#"
                                                            + setMethod.method.getName() + " from JSON source.");
                                }
                            } else {
                                setMethod.method.invoke(object, createObject(paramClass, childJsonValue));
                            }
                        }
                    } catch (Exception e) {
                        throw new JsonException("Unable restore parameter via method " + clazz.getName() + "#"
                                                + setMethod.method.getName() + ". " + e.getMessage(), e);
                    }
                }
            }
        }
        return object;
    }

    private static JsonMethod[] getJsonMethods(Class<?> clazz) {
        HelperCache<Class<?>, JsonMethod[]> partition = methodsCache[clazz.hashCode() & CACHE_MASK];
        synchronized (partition) {
            JsonMethod[] methods = partition.get(clazz);
            if (methods == null) {
                Set<String> transientFieldNames = JsonUtils.getTransientFields(clazz);
                List<JsonMethod> result = new ArrayList<JsonMethod>();
                for (Method method : clazz.getMethods()) {
                    String methodName = method.getName();
                    String field;
                    if (!SKIP_METHODS.contains(methodName)
                        && methodName.startsWith("set")
                        && methodName.length() > 3
                        && !transientFieldNames.contains(field = methodName.length() > 4
                                                                 ? Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4)
                                                                 : methodName.substring(3).toLowerCase())
                        && method.getParameterTypes().length == 1) {
                        result.add(new JsonMethod(method, field));
                    }
                }
                partition.put(clazz, methods = result.toArray(new JsonMethod[result.size()]));
            }
            return methods;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameters) {
        HelperCache<Class<?>, Constructor<?>> partition = constructorsCache[clazz.hashCode() & CACHE_MASK];
        synchronized (partition) {
            Constructor<?> constructor = partition.get(clazz);
            if (constructor == null) {
                try {
                    partition.put(clazz, constructor = clazz.getConstructor(parameters));
                } catch (NoSuchMethodException ignored) {
                }
            }
            return (Constructor<T>)constructor;
        }
    }

    @SuppressWarnings("unchecked")
    private static Enum<?> createEnum(Class c, JsonValue v) {
        String json = v.getStringValue();
        if (json == null || json.isEmpty()) {
            return null;
        }
        return Enum.valueOf(c, json);
    }

    /**
     * Create Objects of known types.
     *
     * @param clazz
     *         class.
     * @param jsonValue
     *         JsonValue , @see {@link JsonValue}
     * @return Object.
     * @throws JsonException
     *         if type is unknown.
     */

    private static Object createObjectKnownTypes(Class<?> clazz, JsonValue jsonValue) throws JsonException {
        Types t = JsonUtils.getType(clazz);
        switch (t) {
            case NULL:
                return null;
            case BOOLEAN:
                return jsonValue.getBooleanValue();
            case BYTE:
                return jsonValue.getByteValue();
            case SHORT:
                return jsonValue.getShortValue();
            case INT:
                return jsonValue.getIntValue();
            case LONG:
                return jsonValue.getLongValue();
            case FLOAT:
                return jsonValue.getFloatValue();
            case DOUBLE:
                return jsonValue.getDoubleValue();
            case CHAR:
                return jsonValue.getStringValue().charAt(0);
            case STRING:
                return jsonValue.getStringValue();
            case CLASS:
                try {
                    return Class.forName(jsonValue.getStringValue());
                } catch (ClassNotFoundException e) {
                    return null;
                }
            case ARRAY_BOOLEAN: {
                boolean[] params = new boolean[jsonValue.size()];
                Iterator<JsonValue> values = jsonValue.getElements();
                int i = 0;
                while (values.hasNext()) {
                    params[i++] = values.next().getBooleanValue();
                }
                return params;
            }
            case ARRAY_BYTE: {
                byte[] params = new byte[jsonValue.size()];
                Iterator<JsonValue> values = jsonValue.getElements();
                int i = 0;
                while (values.hasNext()) {
                    params[i++] = values.next().getByteValue();
                }
                return params;
            }
            case ARRAY_SHORT: {
                short[] params = new short[jsonValue.size()];
                Iterator<JsonValue> values = jsonValue.getElements();
                int i = 0;
                while (values.hasNext()) {
                    params[i++] = values.next().getShortValue();
                }
                return params;
            }
            case ARRAY_INT: {
                int[] params = new int[jsonValue.size()];
                Iterator<JsonValue> values = jsonValue.getElements();
                int i = 0;
                while (values.hasNext()) {
                    params[i++] = values.next().getIntValue();
                }
                return params;
            }
            case ARRAY_LONG: {
                long[] params = new long[jsonValue.size()];
                Iterator<JsonValue> values = jsonValue.getElements();
                int i = 0;
                while (values.hasNext()) {
                    params[i++] = values.next().getLongValue();
                }
                return params;
            }
            case ARRAY_FLOAT: {
                float[] params = new float[jsonValue.size()];
                Iterator<JsonValue> values = jsonValue.getElements();
                int i = 0;
                while (values.hasNext()) {
                    params[i++] = values.next().getFloatValue();
                }
                return params;
            }
            case ARRAY_DOUBLE: {
                double[] params = new double[jsonValue.size()];
                Iterator<JsonValue> values = jsonValue.getElements();
                int i = 0;
                while (values.hasNext()) {
                    params[i++] = values.next().getDoubleValue();
                }
                return params;
            }
            case ARRAY_CHAR: {
                char[] params = new char[jsonValue.size()];
                Iterator<JsonValue> values = jsonValue.getElements();
                int i = 0;
                while (values.hasNext()) {
                    params[i++] = values.next().getStringValue().charAt(0);
                }
                return params;
            }
            case ARRAY_STRING: {
                String[] params = new String[jsonValue.size()];
                Iterator<JsonValue> values = jsonValue.getElements();
                int i = 0;
                while (values.hasNext()) {
                    params[i++] = values.next().getStringValue();
                }
                return params;
            }
            default:
                // Nothing to do for other type. Exception will be thrown.
                break;
        }
        throw new JsonException("Unknown type " + clazz.getName());
    }

    private ObjectBuilder() {
    }
}
