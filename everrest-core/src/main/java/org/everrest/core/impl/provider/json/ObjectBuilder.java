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
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class ObjectBuilder
{
   static final Collection<String> SKIP_METHODS = new HashSet<String>();
   static
   {
      // Since we need support for Groovy must skip this.
      // All "Groovy Objects" implements interface groovy.lang.GroovyObject
      // and has method setMetaClass. Not need to process it.
      SKIP_METHODS.add("setMetaClass");
   }

   /**
    * Create array of Java Object from JSON source include multi-dimension
    * array.
    *
    * @param clazz the Class of target Object.
    * @param jsonArray the JSON representation of array
    * @return result array
    * @throws JsonException if any errors occurs
    */
   public static Object createArray(Class<?> clazz, JsonValue jsonArray) throws JsonException
   {
      Object array = null;
      if (jsonArray != null && !jsonArray.isNull())
      {
         Class<?> componentType = clazz.getComponentType();
         array = Array.newInstance(componentType, jsonArray.size());
         Iterator<JsonValue> values = jsonArray.getElements();
         int i = 0;

         if (componentType.isArray())
         {
            if (JsonUtils.isKnownType(componentType))
            {
               while (values.hasNext())
               {
                  JsonValue v = values.next();
                  Array.set(array, i++, createObjectKnownTypes(componentType, v));
               }
            }
            else
            {
               while (values.hasNext())
               {
                  JsonValue v = values.next();
                  Array.set(array, i++, createArray(componentType, v));
               }
            }
         }
         else
         {
            if (JsonUtils.isKnownType(componentType))
            {
               while (values.hasNext())
               {
                  JsonValue v = values.next();
                  Array.set(array, i++, createObjectKnownTypes(componentType, v));
               }
            }
            else
            {
               while (values.hasNext())
               {
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
    * @param collectionClass collection type
    * @param genericType generic type of collection
    * @param jsonArray the JSON representation of collection
    * @return result collection
    * @throws JsonException if any errors occurs
    */
   public static <T extends Collection<?>> T createCollection(Class<T> collectionClass, Type genericType,
      JsonValue jsonArray) throws JsonException
   {
      T collection = null;
      if (jsonArray != null && !jsonArray.isNull())
      {
         Class<?> actualType = null;
         if (genericType instanceof ParameterizedType)
         {
            // Collection can't be parameterized by other Collection, Array, etc.
            ParameterizedType parameterizedType = (ParameterizedType)genericType;
            try
            {
               actualType = (Class<?>)parameterizedType.getActualTypeArguments()[0];
            }
            catch (ClassCastException e)
            {
               throw new JsonException("This type of Collection can't be restored from JSON source. "
                  + "\nCollection is parameterized by wrong Type: " + parameterizedType + ".");
            }
         }
         else
         {
            throw new JsonException("Collection is not parameterized. Collection<?> is not supported. "
               + "\nCollection must be parameterized by any types, or by JavaBean with 'get' and 'set' methods.");
         }

         Constructor<? extends T> constructor = null;
         if (collectionClass.isInterface() || Modifier.isAbstract(collectionClass.getModifiers()))
         {
            try
            {
               constructor = ArrayList.class.asSubclass(collectionClass).getConstructor(new Class[]{Collection.class});
            }
            catch (Exception e)
            {
               try
               {
                  constructor = HashSet.class.asSubclass(collectionClass).getConstructor(new Class[]{Collection.class});
               }
               catch (Exception e1)
               {
                  try
                  {
                     constructor =
                        LinkedList.class.asSubclass(collectionClass).getConstructor(new Class[]{Collection.class});
                  }
                  catch (Exception e2)
                  {
                     // ignore exception here
                  }
               }
            }
         }
         else
         {
            try
            {
               constructor = collectionClass.getConstructor(new Class[]{Collection.class});
            }
            catch (Exception e)
            {
               throw new JsonException(e.getMessage(), e);
            }
         }

         if (constructor == null)
         {
            throw new JsonException("Can't find satisfied constructor for : " + collectionClass);
         }

         ArrayList<Object> sourceCollection = new ArrayList<Object>(jsonArray.size());
         Iterator<JsonValue> values = jsonArray.getElements();
         while (values.hasNext())
         {
            JsonValue v = values.next();
            if (!JsonUtils.isKnownType(actualType))
            {
               sourceCollection.add(createObject(actualType, v));
            }
            else
            {
               sourceCollection.add(createObjectKnownTypes(actualType, v));
            }
         }
         try
         {
            collection = constructor.newInstance(sourceCollection);
         }
         catch (Exception e)
         {
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
    * @param mapClass map type
    * @param genericType actual type of map
    * @param jsonObject source JSON object
    * @return map
    * @throws JsonException if any errors occurs
    */
   public static <T extends Map<String, ?>> T createObject(Class<T> mapClass, Type genericType, JsonValue jsonObject)
      throws JsonException
   {
      T map = null;
      if (jsonObject != null && !jsonObject.isNull())
      {
         Class<?> valueActualType = null;
         if (genericType instanceof ParameterizedType)
         {
            ParameterizedType parameterizedType = (ParameterizedType)genericType;
            if (!String.class.isAssignableFrom((Class<?>)parameterizedType.getActualTypeArguments()[0]))
            {
               throw new JsonException("Key of Map must be String. ");
            }
            try
            {
               valueActualType = (Class<?>)parameterizedType.getActualTypeArguments()[1];
            }
            catch (ClassCastException e)
            {
               throw new JsonException("This type of Map can't be restored from JSON source."
                  + "\nMap is parameterized by wrong Type: " + parameterizedType + ".");
            }
         }
         else
         {
            throw new JsonException("Map is not parameterized. Map<Sting, ?> is not supported."
               + "\nMap must be parameterized by String and any types or JavaBean with 'get' and 'set' methods.");
         }
         Constructor<? extends T> constructor = null;
         if (mapClass.isInterface() || Modifier.isAbstract(mapClass.getModifiers()))
         {
            try
            {
               constructor = HashMap.class.asSubclass(mapClass).getConstructor(new Class[]{Map.class});
            }
            catch (Exception e)
            {
               try
               {
                  constructor = Hashtable.class.asSubclass(mapClass).getConstructor(new Class[]{Map.class});
               }
               catch (Exception e1)
               {
                  try
                  {
                     constructor = LinkedHashMap.class.asSubclass(mapClass).getConstructor(new Class[]{Map.class});
                  }
                  catch (Exception e2)
                  {
                     // ignore exception here
                  }
               }
            }
         }
         else
         {
            try
            {
               constructor = mapClass.getConstructor(new Class[]{Map.class});
            }
            catch (Exception e)
            {
               throw new JsonException(e.getMessage(), e);
            }
         }

         if (constructor == null)
         {
            throw new JsonException("Can't find satisfied constructor for : " + mapClass);
         }

         HashMap<String, Object> sourceMap = new HashMap<String, Object>(jsonObject.size());
         Iterator<String> keys = jsonObject.getKeys();
         while (keys.hasNext())
         {
            String k = keys.next();
            JsonValue v = jsonObject.getElement(k);
            if (!JsonUtils.isKnownType(valueActualType))
            {
               sourceMap.put(k, createObject(valueActualType, v));
            }
            else
            {
               sourceMap.put(k, createObjectKnownTypes(valueActualType, v));
            }
         }
         try
         {
            map = constructor.newInstance(sourceMap);
         }
         catch (Exception e)
         {
            throw new JsonException(e.getMessage(), e);
         }
      }
      return map;
   }

   /**
    * Create Java Bean from Json Source.
    *
    * @param clazz the Class of target Object.
    * @param jsonValue the Json representation.
    * @return Object.
    * @throws JsonException if any errors occurs.
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   public static <T> T createObject(Class<T> clazz, JsonValue jsonValue) throws JsonException
   {
      if (jsonValue == null || jsonValue.isNull())
      {
         return null;
      }

      Types type = JsonUtils.getType(clazz);
      if (type == Types.ENUM)
      {
         // Enum is not instantiable via CLass.getInstance().
         // This is used when enum is member of array or collection.
         Class c = clazz;
         return (T)Enum.valueOf(c, jsonValue.getStringValue());
      }

      if (!jsonValue.isObject())
      {
         throw new JsonException("Unsupported type of jsonValue. ");
      }

      T object = null;
      if (clazz.isInterface())
      {
         object = JsonUtils.createProxy(clazz);
      }
      else
      {
         try
         {
            object = clazz.newInstance();
         }
         catch (Exception e)
         {
            throw new JsonException("Unable instantiate object. " + e.getMessage(), e);
         }
      }

      Method[] methods = clazz.getMethods();
      Set<String> transientFieldNames = JsonUtils.getTransientFields(clazz);

      for (Method method : methods)
      {
         String methodName = method.getName();
         Class<?>[] parameterTypes = method.getParameterTypes();
         // 3 is length of prefix 'set'
         if (!SKIP_METHODS.contains(methodName) && methodName.startsWith("set") && parameterTypes.length == 1
            && methodName.length() > 3)
         {
            Class<?> methodParameterClass = parameterTypes[0];
            // 3 is length of prefix 'set'
            String key = methodName.substring(3);
            // first letter to lower case
            key = (key.length() > 1) ? Character.toLowerCase(key.charAt(0)) + key.substring(1) : key.toLowerCase();

            if (!transientFieldNames.contains(key))
            {
               JsonValue childJsonValue = jsonValue.getElement(key);
               if (childJsonValue == null)
               {
                  continue;
               }
               // if one of known primitive type or array of primitive type
               try
               {

                  if (JsonUtils.isKnownType(methodParameterClass))
                  {
                     method.invoke(object, new Object[]{createObjectKnownTypes(methodParameterClass, childJsonValue)});
                  }
                  else
                  {
                     Types parameterType = JsonUtils.getType(methodParameterClass);
                     // other type Collection, Map or Object[].
                     if (parameterType != null)
                     {
                        if (parameterType == Types.ENUM)
                        {
                           Class c = methodParameterClass;
                           Enum<?> en = Enum.valueOf(c, childJsonValue.getStringValue());
                           method.invoke(object, new Object[]{en});
                        }
                        else if (parameterType == Types.ARRAY_OBJECT)
                        {
                           Object array = createArray(methodParameterClass, childJsonValue);
                           method.invoke(object, new Object[]{array});
                        }
                        else if (parameterType == Types.COLLECTION)
                        {
                           Class c = methodParameterClass;
                           method.invoke(object, createCollection(c, method.getGenericParameterTypes()[0],
                              childJsonValue));
                        }
                        else if (parameterType == Types.MAP)
                        {
                           Class c = methodParameterClass;
                           method.invoke(object, createObject(c, method.getGenericParameterTypes()[0], childJsonValue));
                        }
                        else
                        {
                           // it must never happen!
                           throw new JsonException("Can't restore parameter of method : " + clazz.getName() + "#"
                              + method.getName() + " from JSON source.");
                        }
                     }
                     else
                     {
                        method.invoke(object, createObject(methodParameterClass, childJsonValue));
                     }
                  }
               }
               catch (Exception e)
               {
                  throw new JsonException("Unable restore parameter via method " + clazz.getName() + "#"
                     + method.getName() + ". " + e.getMessage(), e);
               }
            }
         }
      }
      return object;
   }

   /**
    * Create Objects of known types.
    *
    * @param clazz class.
    * @param jsonValue JsonValue , @see {@link JsonValue}
    * @return Object.
    * @throws JsonException if type is unknown.
    */
   private static Object createObjectKnownTypes(Class<?> clazz, JsonValue jsonValue) throws JsonException
   {
      Types t = JsonUtils.getType(clazz);
      switch (t)
      {
         case NULL :
            return null;
         case BOOLEAN :
            return jsonValue.getBooleanValue();
         case BYTE :
            return jsonValue.getByteValue();
         case SHORT :
            return jsonValue.getShortValue();
         case INT :
            return jsonValue.getIntValue();
         case LONG :
            return jsonValue.getLongValue();
         case FLOAT :
            return jsonValue.getFloatValue();
         case DOUBLE :
            return jsonValue.getDoubleValue();
         case CHAR :
            // TODO check String length
            return jsonValue.getStringValue().charAt(0);
         case STRING :
            return jsonValue.getStringValue();
         case CLASS :
            try
            {
               return Class.forName(jsonValue.getStringValue());
            }
            catch (ClassNotFoundException e)
            {
               return null;
            }
         case ARRAY_BOOLEAN : {
            boolean[] params = new boolean[jsonValue.size()];
            Iterator<JsonValue> values = jsonValue.getElements();
            int i = 0;
            while (values.hasNext())
            {
               params[i++] = values.next().getBooleanValue();
            }
            return params;
         }
         case ARRAY_BYTE : {
            byte[] params = new byte[jsonValue.size()];
            Iterator<JsonValue> values = jsonValue.getElements();
            int i = 0;
            while (values.hasNext())
            {
               params[i++] = values.next().getByteValue();
            }
            return params;
         }
         case ARRAY_SHORT : {
            short[] params = new short[jsonValue.size()];
            Iterator<JsonValue> values = jsonValue.getElements();
            int i = 0;
            while (values.hasNext())
            {
               params[i++] = values.next().getShortValue();
            }
            return params;
         }
         case ARRAY_INT : {
            int[] params = new int[jsonValue.size()];
            Iterator<JsonValue> values = jsonValue.getElements();
            int i = 0;
            while (values.hasNext())
            {
               params[i++] = values.next().getIntValue();
            }
            return params;
         }
         case ARRAY_LONG : {
            long[] params = new long[jsonValue.size()];
            Iterator<JsonValue> values = jsonValue.getElements();
            int i = 0;
            while (values.hasNext())
            {
               params[i++] = values.next().getLongValue();
            }
            return params;
         }
         case ARRAY_FLOAT : {
            float[] params = new float[jsonValue.size()];
            Iterator<JsonValue> values = jsonValue.getElements();
            int i = 0;
            while (values.hasNext())
            {
               params[i++] = values.next().getFloatValue();
            }
            return params;
         }
         case ARRAY_DOUBLE : {
            double[] params = new double[jsonValue.size()];
            Iterator<JsonValue> values = jsonValue.getElements();
            int i = 0;
            while (values.hasNext())
            {
               params[i++] = values.next().getDoubleValue();
            }
            return params;
         }
         case ARRAY_CHAR : {
            char[] params = new char[jsonValue.size()];
            Iterator<JsonValue> values = jsonValue.getElements();
            int i = 0;
            // TODO better checking an transformation string to char
            while (values.hasNext())
            {
               params[i++] = values.next().getStringValue().charAt(0);
            }
            return params;
         }
         case ARRAY_STRING : {
            String[] params = new String[jsonValue.size()];
            Iterator<JsonValue> values = jsonValue.getElements();
            int i = 0;
            while (values.hasNext())
            {
               params[i++] = values.next().getStringValue();
            }
            return params;
         }
         default :
            // Nothing to do for other type. Exception will be thrown.
            break;
      }
      throw new JsonException("Unknown type " + clazz.getName());
   }

}
