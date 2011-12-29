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

import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class JsonGenerator
{

   static final Collection<String> SKIP_METHODS = new HashSet<String>();

   static
   {
      // Prevent discovering of Java class.
      SKIP_METHODS.add("getClass");
      // Since we need support for Groovy must skip this.
      // All "Groovy Objects" implements interface groovy.lang.GroovyObject
      // and has method getMetaClass. Not need to discover it.
      SKIP_METHODS.add("getMetaClass");
   }

   /**
    * Create JSON array from specified collection.
    *
    * @param collection source collection
    * @return JSON representation of collection
    * @throws JsonException if collection can't be transformed in JSON
    *            representation
    */
   public static JsonValue createJsonArray(Collection<?> collection) throws JsonException
   {
      return createJsonValue(collection);
   }

   /**
    * Create JSON array from specified object. Parameter <code>array</code> must
    * be array.
    *
    * @param array source array
    * @return JSON representation of array
    * @throws JsonException if array can't be transformed in JSON representation
    */
   public static JsonValue createJsonArray(Object array) throws JsonException
   {
      if (array == null)
      {
         return new NullValue();
      }
      Types t = JsonUtils.getType(array);
      if (t == Types.ARRAY_BOOLEAN || t == Types.ARRAY_BYTE || t == Types.ARRAY_SHORT || t == Types.ARRAY_INT
         || t == Types.ARRAY_LONG || t == Types.ARRAY_FLOAT || t == Types.ARRAY_DOUBLE || t == Types.ARRAY_CHAR
         || t == Types.ARRAY_STRING || t == Types.ARRAY_OBJECT)
      {
         return createJsonValue(array);
      }
      else
      {
         throw new JsonException("Invalid argument, must be array.");
      }
   }

   /**
    * Create JSON object from specified map.
    *
    * @param map source map
    * @return JSON representation of map
    * @throws JsonException if map can't be transformed in JSON representation
    */
   public static JsonValue createJsonObjectFromMap(Map<String, ?> map) throws JsonException
   {
      return createJsonValue(map);
   }

   /**
    * Create JSON object from specified string imply it is JSON object in String
    * format.
    *
    * @param s source string
    * @return JSON representation of map
    * @throws JsonException if map can't be transformed in JSON representation
    */
   public JsonValue createJsonObjectFromString(String s) throws JsonException
   {
      JsonParser parser = new JsonParser();
      parser.parse(new StringReader(s));
      return parser.getJsonObject();
   }

   /**
    * Create JSON object from specified object. Object must be conform with java
    * bean structure.
    *
    * @param object source object
    * @return JSON representation of object
    * @throws JsonException if map can't be transformed in JSON representation
    */
   public static JsonValue createJsonObject(Object object) throws JsonException
   {
      Class<?> clazz = object.getClass();
      Method[] methods = clazz.getMethods();
      Set<String> transientFieldNames = JsonUtils.getTransientFields(clazz);
      JsonValue jsonRootValue = new ObjectValue();
      for (Method method : methods)
      {
         String methodName = method.getName();
         /*
          * Method must be as follow:
          * 1. Name starts from "get" plus at least one character or
          * starts from "is" plus one more character and return boolean type;
          * 2. Must be without parameters;
          * 3. Not be in SKIP_METHODS set.
          */
         String key = null;
         if (!SKIP_METHODS.contains(methodName) && method.getParameterTypes().length == 0)
         {
            if (methodName.startsWith("get") && methodName.length() > 3)
            {
               key = methodName.substring(3);
            }
            else if (methodName.startsWith("is") && methodName.length() > 2
               && (method.getReturnType() == Boolean.class || method.getReturnType() == boolean.class))
            {
               key = methodName.substring(2);
            }
         }
         if (key != null)
         {
            // First letter of key to lower case.
            key = (key.length() > 1) ? Character.toLowerCase(key.charAt(0)) + key.substring(1) : key.toLowerCase();
            // Check is this field in list of transient field.
            if (!transientFieldNames.contains(key))
            {
               try
               {
                  // Get result of invoke method get...
                  Object invokeResult = method.invoke(object);
                  if (JsonUtils.getType(invokeResult) != null)
                  {
                     jsonRootValue.addElement(key, createJsonValue(invokeResult));
                  }
                  else
                  {
                     jsonRootValue.addElement(key, createJsonObject(invokeResult));
                  }
               }
               catch (InvocationTargetException e)
               {
                  throw new JsonException(e.getMessage(), e);
               }
               catch (IllegalAccessException e)
               {
                  throw new JsonException(e.getMessage(), e);
               }
            }
         }
      }
      return jsonRootValue;
   }

   /**
    * Create JsonValue corresponding to Java object.
    *
    * @param object source object.
    * @return JsonValue.
    * @throws JsonException if any errors occurs.
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   private static JsonValue createJsonValue(Object object) throws JsonException
   {
      Types type = JsonUtils.getType(object);
      switch (type)
      {
         case NULL :
            return new NullValue();
         case BOOLEAN :
            return new BooleanValue((Boolean)object);
         case BYTE :
            return new LongValue((Byte)object);
         case SHORT :
            return new LongValue((Short)object);
         case INT :
            return new LongValue((Integer)object);
         case LONG :
            return new LongValue((Long)object);
         case FLOAT :
            return new DoubleValue((Float)object);
         case DOUBLE :
            return new DoubleValue((Double)object);
         case CHAR :
            return new StringValue(Character.toString((Character)object));
         case STRING :
            return new StringValue((String)object);
         case ENUM :
            return new StringValue(((Enum)object).name());
         case CLASS :
            return new StringValue(((Class)object).getName());
         case ARRAY_BOOLEAN : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
            {
               jsonArray.addElement(new BooleanValue(Array.getBoolean(object, i)));
            }
            return jsonArray;
         }
         case ARRAY_BYTE : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
            {
               jsonArray.addElement(new LongValue(Array.getByte(object, i)));
            }
            return jsonArray;
         }
         case ARRAY_SHORT : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
            {
               jsonArray.addElement(new LongValue(Array.getShort(object, i)));
            }
            return jsonArray;
         }
         case ARRAY_INT : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
            {
               jsonArray.addElement(new LongValue(Array.getInt(object, i)));
            }
            return jsonArray;
         }
         case ARRAY_LONG : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
            {
               jsonArray.addElement(new LongValue(Array.getLong(object, i)));
            }
            return jsonArray;
         }
         case ARRAY_FLOAT : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
            {
               jsonArray.addElement(new DoubleValue(Array.getFloat(object, i)));
            }
            return jsonArray;
         }
         case ARRAY_DOUBLE : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
            {
               jsonArray.addElement(new DoubleValue(Array.getDouble(object, i)));
            }
            return jsonArray;
         }
         case ARRAY_CHAR : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
            {
               jsonArray.addElement(new StringValue(Character.toString(Array.getChar(object, i))));
            }
            return jsonArray;
         }
         case ARRAY_STRING : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
            {
               jsonArray.addElement(new StringValue((String)Array.get(object, i)));
            }
            return jsonArray;
         }
         case ARRAY_OBJECT : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
            {
               Object el = Array.get(object, i);
               if (JsonUtils.getType(el) != null)
               {
                  jsonArray.addElement(createJsonValue(el));
               }
               else
               {
                  jsonArray.addElement(createJsonObject(el));
               }
            }
            return jsonArray;
         }
         case COLLECTION : {
            JsonValue jsonArray = new ArrayValue();
            List<Object> list = new ArrayList<Object>((Collection<?>)object);
            for (Object o : list)
            {
               if (JsonUtils.getType(o) != null)
               {
                  jsonArray.addElement(createJsonValue(o));
               }
               else
               {
                  jsonArray.addElement(createJsonObject(o));
               }
            }
            return jsonArray;
         }
         case MAP :
            JsonValue jsonObject = new ObjectValue();
            Map<String, Object> map = new HashMap<String, Object>((Map<String, Object>)object);
            Set<String> keys = map.keySet();
            for (String k : keys)
            {
               Object o = map.get(k);
               if (JsonUtils.getType(o) != null)
               {
                  jsonObject.addElement(k, createJsonValue(o));
               }
               else
               {
                  jsonObject.addElement(k, createJsonObject(o));
               }
            }
            return jsonObject;
         default :
            // Must not be here!
            return null;
      }
   }

}
