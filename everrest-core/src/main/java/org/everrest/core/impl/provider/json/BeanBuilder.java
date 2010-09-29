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

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class BeanBuilder
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
    * Create Java Bean from Json Source.
    *
    * @param clazz the Class of target Object.
    * @param jsonValue the Json representation.
    * @return Object.
    * @throws Exception if any errors occurs.
    */
   @SuppressWarnings("unchecked")
   public Object createObject(Class<?> clazz, JsonValue jsonValue) throws Exception
   {
      if (JsonUtils.getType(clazz) == Types.ENUM)
      {
         // Enum is not instantiable via CLass.getInstance().
         // This is used when enum is member of array or collection.
         Class c = clazz;
         return Enum.valueOf(c, jsonValue.getStringValue());
      }
      Object object = clazz.newInstance();
      Method[] methods = clazz.getMethods();

      for (Method method : methods)
      {
         String methodName = method.getName();
         Class<?>[] parameterTypes = method.getParameterTypes();
         // 3 is length of prefix 'set'
         if (!SKIP_METHODS.contains(methodName) && methodName.startsWith("set") && parameterTypes.length == 1
            && methodName.length() > 3)
         {
            Class<?> methodParameterClazz = parameterTypes[0];
            // 3 is length of prefix 'set'
            String key = methodName.substring(3);
            // first letter to lower case
            key = (key.length() > 1) ? Character.toLowerCase(key.charAt(0)) + key.substring(1) : key.toLowerCase();
            // Bug : WS-53
            if (jsonValue.isNull())
            {
               return null;
            }
            if (!jsonValue.isObject())
            {
               throw new JsonException("Unsupported type of jsonValue for parameter of method " + clazz.getName() + "#"
                  + method.getName());
            }
            JsonValue childJsonValue = jsonValue.getElement(key);
            if (childJsonValue == null)
            {
               continue;
            }
            // if one of known primitive type or array of primitive type
            if (JsonUtils.isKnownType(methodParameterClazz))
            {
               method.invoke(object, new Object[]{createObjectKnownTypes(methodParameterClazz, childJsonValue)});
            }
            else
            {
               Types type = JsonUtils.getType(methodParameterClazz);
               // other type Collection, Map or Object[].
               if (type != null)
               {
                  switch (type)
                  {
                     case ENUM : {
                        Class c = methodParameterClazz;
                        Enum<?> en = Enum.valueOf(c, childJsonValue.getStringValue());
                        method.invoke(object, new Object[]{en});
                     }
                        break;
                     case ARRAY_OBJECT : {
                        Object array = createArray(methodParameterClazz, childJsonValue);
                        method.invoke(object, new Object[]{array});
                     }
                        break;
                     case COLLECTION : {
                        Type[] genericParameterTypes = method.getGenericParameterTypes();
                        Class<?> parameterizedTypeClass = null;
                        if (genericParameterTypes.length == 1)
                        {
                           if (genericParameterTypes[0] instanceof ParameterizedType)
                           {
                              ParameterizedType parameterizedType = (ParameterizedType)genericParameterTypes[0];
                              try
                              {
                                 // Collection can't be parameterized by other Collection,
                                 // Array, etc.
                                 parameterizedTypeClass = (Class<?>)parameterizedType.getActualTypeArguments()[0];
                              }
                              catch (ClassCastException e)
                              {
                                 throw new JsonException("Unsupported parameter in method " + clazz.getName() + "#"
                                    + method.getName()
                                    + ". This type of Collection can't be restored from JSON source.\n"
                                    + "Collection is parameterized by wrong Type: " + parameterizedType + ".");
                              }
                           }
                           else
                           {
                              throw new JsonException("Unsupported parameter in method " + clazz.getName() + "#"
                                 + method.getName() + ". Collection is not parameterized. "
                                 + "Collection<?> is not supported. \nCollection must be parameterized by"
                                 + " any types, or by JavaBean with 'get' and 'set' methods.");
                           }
                        }
                        Constructor<?> constructor = null;
                        if (methodParameterClazz.isInterface()
                           || Modifier.isAbstract(methodParameterClazz.getModifiers()))
                        {
                           try
                           {
                              constructor =
                                 ArrayList.class.asSubclass(methodParameterClazz).getConstructor(
                                    new Class[]{Collection.class});
                           }
                           catch (Exception e)
                           {
                              try
                              {
                                 constructor =
                                    HashSet.class.asSubclass(methodParameterClazz).getConstructor(
                                       new Class[]{Collection.class});
                              }
                              catch (Exception e1)
                              {
                                 try
                                 {
                                    constructor =
                                       LinkedList.class.asSubclass(methodParameterClazz).getConstructor(
                                          new Class[]{Collection.class});
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
                           constructor = methodParameterClazz.getConstructor(new Class[]{Collection.class});
                        }
                        if (constructor == null)
                           throw new JsonException("Can't find satisfied constructor for : " + methodParameterClazz
                              + ", method : " + clazz.getName() + "#" + method.getName());

                        ArrayList<Object> sourceCollection = new ArrayList<Object>(childJsonValue.size());

                        Iterator<JsonValue> values = childJsonValue.getElements();

                        while (values.hasNext())
                        {
                           JsonValue v = values.next();
                           if (!JsonUtils.isKnownType(parameterizedTypeClass))
                           {
                              sourceCollection.add(createObject(parameterizedTypeClass, v));
                           }
                           else
                           {
                              sourceCollection.add(createObjectKnownTypes(parameterizedTypeClass, v));
                           }
                        }
                        constructor.newInstance(sourceCollection);
                        method.invoke(object, constructor.newInstance(sourceCollection));
                     }
                        break;
                     case MAP : {
                        Type[] genericParameterTypes = method.getGenericParameterTypes();
                        Class<?> parameterizedTypeClass = null;
                        if (genericParameterTypes.length == 1)
                        {
                           if (genericParameterTypes[0] instanceof ParameterizedType)
                           {
                              ParameterizedType parameterizedType = (ParameterizedType)genericParameterTypes[0];
                              if (!String.class
                                 .isAssignableFrom((Class<?>)parameterizedType.getActualTypeArguments()[0]))
                              {
                                 throw new JsonException("Unsupported parameter in method " + clazz.getName() + "#"
                                    + method.getName() + ". Key of Map must be String.");
                              }
                              try
                              {
                                 parameterizedTypeClass = (Class<?>)parameterizedType.getActualTypeArguments()[1];
                              }
                              catch (Exception e)
                              {
                                 throw new JsonException("Unsupported parameter in method " + clazz.getName() + "#"
                                    + method.getName() + ". This type of Map can't be restored from JSON source.\n"
                                    + "Map is parameterized by wrong Type: " + parameterizedType + ".");
                              }
                           }
                           else
                           {
                              throw new JsonException("Unsupported parameter in method " + clazz.getName() + "#"
                                 + method.getName() + ". Map is not parameterized. "
                                 + "Map<Sting, ?> is not supported. \nMap must be parameterized by"
                                 + "String and any types or JavaBean with 'get' and 'set' methods.");
                           }
                        }
                        Constructor<?> constructor = null;
                        if (methodParameterClazz.isInterface()
                           || Modifier.isAbstract(methodParameterClazz.getModifiers()))
                        {
                           try
                           {
                              constructor =
                                 HashMap.class.asSubclass(methodParameterClazz).getConstructor(new Class[]{Map.class});
                           }
                           catch (Exception e)
                           {
                              try
                              {
                                 constructor =
                                    Hashtable.class.asSubclass(methodParameterClazz).getConstructor(
                                       new Class[]{Map.class});
                              }
                              catch (Exception e1)
                              {
                                 try
                                 {
                                    constructor =
                                       LinkedHashMap.class.asSubclass(methodParameterClazz).getConstructor(
                                          new Class[]{Map.class});
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
                           constructor = methodParameterClazz.getConstructor(new Class[]{Map.class});
                        }

                        if (constructor == null)
                        {
                           throw new JsonException("Can't find satisfied constructor for : " + methodParameterClazz
                              + ", method : " + clazz.getName() + "#" + method.getName());
                        }

                        HashMap<String, Object> sourceMap = new HashMap<String, Object>(childJsonValue.size());
                        Iterator<String> keys = childJsonValue.getKeys();
                        while (keys.hasNext())
                        {
                           String k = keys.next();
                           JsonValue v = childJsonValue.getElement(k);
                           if (!JsonUtils.isKnownType(parameterizedTypeClass))
                           {
                              sourceMap.put(k, createObject(parameterizedTypeClass, v));
                           }
                           else
                           {
                              sourceMap.put(k, createObjectKnownTypes(parameterizedTypeClass, v));
                           }
                        }
                        method.invoke(object, constructor.newInstance(sourceMap));
                     }
                        break;
                     default :
                        // it must never happen!
                        throw new JsonException("Can't restore parameter of method : " + clazz.getName() + "#"
                           + method.getName() + " from JSON source.");
                  }
               }
               else
               {
                  method.invoke(object, createObject(methodParameterClazz, childJsonValue));
               }
            }
         }
      }
      return object;
   }

   /**
    * Create array of Java Object from Json Source include multi-dimension
    * array.
    *
    * @param clazz the Class of target Object.
    * @param jsonValue the Json representation.
    * @return Object.
    * @throws Exception if any errors occurs.
    */
   private Object createArray(Class<?> clazz, JsonValue jsonValue) throws Exception
   {
      Class<?> componentType = clazz.getComponentType();
      Object array = Array.newInstance(componentType, jsonValue.size());
      Iterator<JsonValue> values = jsonValue.getElements();
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
         while (values.hasNext())
         {
            JsonValue v = values.next();
            Array.set(array, i++, createObject(componentType, v));
         }
      }
      return array;
   }

   /**
    * Create Objects of known types.
    *
    * @param clazz class.
    * @param jsonValue JsonValue , @see {@link JsonValue}
    * @return Object.
    * @throws JsonException if type is unknown.
    */
   private Object createObjectKnownTypes(Class<?> clazz, JsonValue jsonValue) throws JsonException
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
