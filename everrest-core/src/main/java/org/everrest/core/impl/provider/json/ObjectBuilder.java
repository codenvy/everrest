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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Throwables.propagateIfPossible;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.everrest.core.impl.provider.json.JsonUtils.Types.ARRAY_OBJECT;
import static org.everrest.core.impl.provider.json.JsonUtils.Types.COLLECTION;
import static org.everrest.core.impl.provider.json.JsonUtils.Types.ENUM;
import static org.everrest.core.impl.provider.json.JsonUtils.Types.MAP;
import static org.everrest.core.impl.provider.json.JsonUtils.createProxy;
import static org.everrest.core.impl.provider.json.JsonUtils.getFieldName;
import static org.everrest.core.impl.provider.json.JsonUtils.getTransientFields;
import static org.everrest.core.impl.provider.json.JsonUtils.getType;
import static org.everrest.core.impl.provider.json.JsonUtils.isKnownType;

/** @author andrew00x */
public class ObjectBuilder {
    private static final Collection<String> SKIP_METHODS = newHashSet("setMetaClass");

    private static LoadingCache<Class<?>, JsonMethod[]> methodsCache = CacheBuilder.newBuilder()
                                                                                   .concurrencyLevel(8)
                                                                                   .maximumSize(256)
                                                                                   .expireAfterAccess(10, MINUTES)
                                                                                   .build(new CacheLoader<Class<?>, JsonMethod[]>() {
                                                                                       @Override
                                                                                       public JsonMethod[] load(Class<?> aClass)
                                                                                               throws Exception {
                                                                                           return getJsonMethods(aClass);
                                                                                       }
                                                                                   });

    private static Cache<Class<?>, Constructor<?>> constructorsCache = CacheBuilder.newBuilder()
                                                                                   .concurrencyLevel(8)
                                                                                   .maximumSize(256)
                                                                                   .expireAfterAccess(10, MINUTES)
                                                                                   .build();

    private static JsonMethod[] getJsonMethods(Class<?> clazz) {
        Set<String> transientFieldNames = getTransientFields(clazz);
        List<JsonMethod> result = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
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
        return !SKIP_METHODS.contains(method.getName()) && isSetter(method);
    }

    private static boolean isSetter(Method method) {
        String methodName = method.getName();
        return methodName.startsWith("set") && methodName.length() > 3 && method.getParameterTypes().length == 1;
    }

    /* ------------------------------------------------------------------------------ */

    /**
     * Create array of Java Object from JSON source include multi-dimension
     * array.
     *
     * @param aClass
     *         the Class of target Object.
     * @param jsonArray
     *         the JSON representation of array
     * @return result array
     * @throws JsonException
     *         if any errors occurs
     */
    public static Object createArray(Class<?> aClass, JsonValue jsonArray) throws JsonException {
        if (jsonArray == null || jsonArray.isNull()) {
            return null;
        }
        Class<?> componentType = aClass.getComponentType();
        Object array = Array.newInstance(componentType, jsonArray.size());
        Iterator<JsonValue> values = jsonArray.getElements();
        int i = 0;

        if (componentType.isArray()) {
            if (isKnownType(componentType)) {
                while (values.hasNext()) {
                    Array.set(array, i++, createObjectKnownTypes(componentType, values.next()));
                }
            } else {
                while (values.hasNext()) {
                    Array.set(array, i++, createArray(componentType, values.next()));
                }
            }
        } else {
            if (isKnownType(componentType)) {
                while (values.hasNext()) {
                    Array.set(array, i++, createObjectKnownTypes(componentType, values.next()));
                }
            } else {
                while (values.hasNext()) {
                    Array.set(array, i++, createObject(componentType, values.next()));
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
        if (jsonArray == null || jsonArray.isNull()) {
            return null;
        }
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
                throw new JsonException(String.format(
                        "This type of Collection can't be restored from JSON source.\nCollection is parameterized by wrong Type: %s",
                        parameterizedType));
            }
        } else {
            throw new JsonException("Collection is not parameterized. Collection<?> is not supported");
        }

        Constructor<? extends T> constructor;
        if (collectionClass.isInterface() || Modifier.isAbstract(collectionClass.getModifiers())) {
            constructor = getConstructor(findAcceptableCollectionImplementation(collectionClass), Collection.class);
        } else {
            constructor = getConstructor(collectionClass, Collection.class);
        }

        ArrayList<Object> sourceCollection = new ArrayList<>(jsonArray.size());
        Iterator<JsonValue> values = jsonArray.getElements();
        Types jsonElementType = getType(elementClass);
        while (values.hasNext()) {
            JsonValue value = values.next();
            if (jsonElementType == null) {
                sourceCollection.add(createObject(elementClass, value));
            } else {
                switch (jsonElementType) {
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
                    case CLASS:
                        sourceCollection.add(createObjectKnownTypes(elementClass, value));
                        break;
                    case ARRAY_OBJECT:
                        sourceCollection.add(createArray(elementClass, value));
                        break;
                    case COLLECTION:
                        sourceCollection.add(createCollection(elementClass, elementType, value));
                        break;
                    case MAP:
                        sourceCollection.add(createObject(elementClass, elementType, value));
                        break;
                    case ENUM:
                        sourceCollection.add(createEnum(elementClass, value));
                        break;
                }
            }
        }
        try {
            return constructor.newInstance(sourceCollection);
        } catch (Exception e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    private static <T extends Collection<?>> Class findAcceptableCollectionImplementation(Class<T> collectionClass) throws JsonException {
        Class impl = null;
        if (collectionClass.isAssignableFrom(ArrayList.class)) {
            impl = ArrayList.class.asSubclass(collectionClass);
        } else if (collectionClass.isAssignableFrom(HashSet.class)) {
            impl = HashSet.class.asSubclass(collectionClass);
        } else if (collectionClass.isAssignableFrom(TreeSet.class)) {
            impl = TreeSet.class.asSubclass(collectionClass);
        } else if (collectionClass.isAssignableFrom(LinkedList.class)) {
            impl = LinkedList.class.asSubclass(collectionClass);
        }
        if (impl == null) {
            throw new JsonException(String.format("Can't find proper implementation for collection %s", collectionClass));
        }
        return impl;
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
        if (jsonObject == null || jsonObject.isNull()) {
            return null;
        }
        Class mapValueClass;
        Type mapValueType;
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)genericType;
            if (!String.class.isAssignableFrom((Class)parameterizedType.getActualTypeArguments()[0])) {
                throw new JsonException("Key of Map must be String. ");
            }
            mapValueType = parameterizedType.getActualTypeArguments()[1];
            if (mapValueType instanceof Class) {
                mapValueClass = (Class)mapValueType;
            } else if (mapValueType instanceof ParameterizedType) {
                mapValueClass = (Class)((ParameterizedType)mapValueType).getRawType();
            } else {
                throw new JsonException(
                        String.format("This type of Map can't be restored from JSON source.\nMap is parameterized by wrong Type: %s",
                                      parameterizedType));
            }
        } else {
            throw new JsonException("Map is not parameterized. Map<Sting, ?> is not supported.");
        }
        Constructor<? extends T> constructor;
        if (mapClass.isInterface() || Modifier.isAbstract(mapClass.getModifiers())) {
            constructor = getConstructor(findAcceptableMapImplementation(mapClass), Map.class);
        } else {
            constructor = getConstructor(mapClass, Map.class);
        }

        Types jsonMapValueType = getType(mapValueClass);
        HashMap<String, Object> sourceMap = new HashMap<>(jsonObject.size());
        Iterator<String> keys = jsonObject.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            JsonValue childJsonValue = jsonObject.getElement(key);
            if (jsonMapValueType == null) {
                sourceMap.put(key, createObject(mapValueClass, childJsonValue));
            } else {
                switch (jsonMapValueType) {
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
                    case CLASS:
                        sourceMap.put(key, createObjectKnownTypes(mapValueClass, childJsonValue));
                        break;
                    case ARRAY_OBJECT:
                        sourceMap.put(key, createArray(mapValueClass, childJsonValue));
                        break;
                    case COLLECTION:
                        sourceMap.put(key, createCollection(mapValueClass, mapValueType, childJsonValue));
                        break;
                    case MAP:
                        sourceMap.put(key, createObject(mapValueClass, mapValueType, childJsonValue));
                        break;
                    case ENUM:
                        sourceMap.put(key, createEnum(mapValueClass, childJsonValue));
                        break;
                }
            }
        }
        try {
            return constructor.newInstance(sourceMap);
        } catch (Exception e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    private static <T extends Map<String, ?>> Class findAcceptableMapImplementation(Class<T> mapClass) throws JsonException {
        Class impl = null;
        if (mapClass.isAssignableFrom(HashMap.class)) {
            impl = HashMap.class.asSubclass(mapClass);
        } else if (mapClass.isAssignableFrom(TreeMap.class)) {
            impl = TreeMap.class.asSubclass(mapClass);
        } else if (mapClass.isAssignableFrom(Hashtable.class)) {
            impl = Hashtable.class.asSubclass(mapClass);
        }
        if (impl == null) {
            throw new JsonException(String.format("Can't find proper implementation for map %s", mapClass));
        }
        return impl;
    }

    /**
     * Create Java Bean from Json Source.
     *
     * @param aClass
     *         the Class of target Object.
     * @param jsonValue
     *         the Json representation.
     * @return Object.
     * @throws JsonException
     *         if any errors occurs.
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T createObject(Class<T> aClass, JsonValue jsonValue) throws JsonException {
        if (jsonValue == null || jsonValue.isNull()) {
            return null;
        }

        if (getType(aClass) == ENUM) {
            return (T)createEnum(aClass, jsonValue);
        }

        if (!jsonValue.isObject()) {
            throw new JsonException("Unsupported type of jsonValue. ");
        }

        T object;
        if (aClass.isInterface()) {
            object = createProxy(aClass);
        } else {
            try {
                object = getConstructor(aClass).newInstance();
            } catch (JsonException e) {
                throw e;
            } catch (Exception e) {
                throw new JsonException(String.format("Unable instantiate object. %s", e.getMessage()), e);
            }
        }

        JsonMethod[] setters;
        try {
            setters = methodsCache.get(aClass);
        } catch (ExecutionException e) {
            propagateIfPossible(e.getCause());
            throw new JsonException(e.getCause());
        }

        for (JsonMethod setter : setters) {
            JsonValue childJsonValue = jsonValue.getElement(setter.field);
            if (childJsonValue != null) {
                try {
                    final Class paramClass = setter.method.getParameterTypes()[0];
                    if (isKnownType(paramClass)) {
                        setter.method.invoke(object, createObjectKnownTypes(paramClass, childJsonValue));
                    } else {
                        Types parameterType = getType(paramClass);
                        if (parameterType != null) {
                            if (parameterType == ENUM) {
                                setter.method.invoke(object, createEnum(paramClass, childJsonValue));
                            } else if (parameterType == ARRAY_OBJECT) {
                                setter.method.invoke(object, createArray(paramClass, childJsonValue));
                            } else if (parameterType == COLLECTION) {
                                setter.method.invoke(object, createCollection(paramClass, setter.method.getGenericParameterTypes()[0], childJsonValue));
                            } else if (parameterType == MAP) {
                                setter.method.invoke(object, createObject(paramClass, setter.method.getGenericParameterTypes()[0], childJsonValue));
                            } else {
                                throw new JsonException(String.format("Can't restore parameter of method : %s#%s from JSON source.",
                                                                      aClass.getName(), setter.method.getName()));
                            }
                        } else {
                            setter.method.invoke(object, createObject(paramClass, childJsonValue));
                        }
                    }
                } catch (Exception e) {
                    String msg = String.format("Unable restore parameter via method %s#%s", aClass.getName(), setter.method.getName());
                    if (e instanceof JsonException) {
                        StringBuilder msgBuilder = new StringBuilder(msg);
                        mergeMessagesFromCausalJsonExceptions(e, msgBuilder);
                        throw new JsonException(msgBuilder.toString(), e);
                    } else {
                        throw new JsonException(msg + e.toString(), e);
                    }
                }
            }
        }
        return object;
    }

    private static void mergeMessagesFromCausalJsonExceptions(Throwable error, StringBuilder msg) {
        int indent = 4;
        do {
            msg.append('\n');
            for (int i = 0; i < indent; i++) {
                msg.append(' ');
            }
            indent += 4;
            msg.append(error.getMessage());
            error = error.getCause();
        } while (error instanceof JsonException);
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> getConstructor(Class<T> aClass, Class<?>... parameters) throws JsonException {
        try {
            return (Constructor<T>)constructorsCache.get(aClass, (Callable<Constructor<T>>)() -> {
                try {
                    return aClass.getConstructor(parameters);
                } catch (NoSuchMethodException e) {
                    throw new JsonException(String.format("Can't find satisfied constructor for : %s", aClass));
                }
            });
        } catch (ExecutionException e) {
            propagateIfPossible(e.getCause(), JsonException.class);
            throw new JsonException(e.getCause());
        }
    }

    @SuppressWarnings("unchecked")
    private static Enum<?> createEnum(Class enumClass, JsonValue jsonValue) {
        String name = jsonValue.getStringValue();
        if (isNullOrEmpty(name)) {
            return null;
        }
        return Enum.valueOf(enumClass, name);
    }

    /**
     * Create Objects of known types.
     *
     * @param aClass
     *         class.
     * @param jsonValue
     *         JsonValue , @see {@link JsonValue}
     * @return Object.
     * @throws JsonException
     *         if type is unknown.
     */
    private static Object createObjectKnownTypes(Class<?> aClass, JsonValue jsonValue) throws JsonException {
        switch (getType(aClass)) {
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
        }
        throw new JsonException(String.format("Unknown type %s", aClass.getName()));
    }

    private ObjectBuilder() {
    }
}
