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
package org.everrest.json.impl;

import org.everrest.json.JsonGenerator;
import org.everrest.json.impl.JsonUtils.Types;
import org.everrest.json.value.JsonValue;
import org.everrest.json.value.impl.ArrayValue;
import org.everrest.json.value.impl.BooleanValue;
import org.everrest.json.value.impl.DoubleValue;
import org.everrest.json.value.impl.LongValue;
import org.everrest.json.value.impl.NullValue;
import org.everrest.json.value.impl.ObjectValue;
import org.everrest.json.value.impl.StringValue;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: JsonGeneratorImpl.java 34417 2009-07-23 14:42:56Z dkatayev $
 */
public class JsonGeneratorImpl implements JsonGenerator
{

   /**
    * {@inheritDoc}
    */
   public JsonValue createJsonObject(Object object) throws JsonException
   {
      Method[] methods = object.getClass().getMethods();

      List<String> transientFields = getTransientFields(object.getClass());

      JsonValue jsonRootValue = new ObjectValue();

      for (Method method : methods)
      {
         String name = method.getName();

         /*
          * Method must be as follow: 1. Name starts from "get" plus at least one
          * character or starts from "is" plus one more character and return
          * boolean type; 2. Must be without parameters; 3. Not "getClass" method;
          */
         String key = null;
         if (name.startsWith("get") && name.length() > 3 && method.getParameterTypes().length == 0
            && !"getClass".equals(name))
         {
            key = name.substring(3);
         }
         else if (name.startsWith("is") && name.length() > 2
            && (method.getReturnType() == Boolean.class || method.getReturnType() == boolean.class)
            && method.getParameterTypes().length == 0)
         {
            key = name.substring(2);
         }

         if (key != null)
         {
            // First letter of key to lower case.
            key = (key.length() > 1) ? Character.toLowerCase(key.charAt(0)) + key.substring(1) : key.toLowerCase();
            // Check is this field in list of transient field.
            if (!transientFields.contains(key))
            {
               try
               {
                  // Get result of invoke method get...
                  Object invokeResult = method.invoke(object, new Object[0]);

                  if (JsonUtils.getType(invokeResult) != null)
                     jsonRootValue.addElement(key, createJsonValue(invokeResult));
                  else
                     jsonRootValue.addElement(key, createJsonObject(invokeResult));

               }
               catch (InvocationTargetException e)
               {
                  throw new JsonException(e);
               }
               catch (IllegalAccessException e)
               {
                  throw new JsonException(e);
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
   @SuppressWarnings("unchecked")
   protected JsonValue createJsonValue(Object object) throws JsonException
   {
      Types t = JsonUtils.getType(object);
      switch (t)
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
         case ARRAY_BOOLEAN : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
               jsonArray.addElement(new BooleanValue(Array.getBoolean(object, i)));
            return jsonArray;
         }
         case ARRAY_BYTE : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
               jsonArray.addElement(new LongValue(Array.getByte(object, i)));
            return jsonArray;
         }
         case ARRAY_SHORT : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
               jsonArray.addElement(new LongValue(Array.getShort(object, i)));
            return jsonArray;
         }
         case ARRAY_INT : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
               jsonArray.addElement(new LongValue(Array.getInt(object, i)));
            return jsonArray;
         }
         case ARRAY_LONG : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
               jsonArray.addElement(new LongValue(Array.getLong(object, i)));
            return jsonArray;
         }
         case ARRAY_FLOAT : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
               jsonArray.addElement(new DoubleValue(Array.getFloat(object, i)));
            return jsonArray;
         }
         case ARRAY_DOUBLE : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
               jsonArray.addElement(new DoubleValue(Array.getDouble(object, i)));
            return jsonArray;
         }
         case ARRAY_CHAR : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
               jsonArray.addElement(new StringValue(Character.toString(Array.getChar(object, i))));
            return jsonArray;
         }
         case ARRAY_STRING : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
               jsonArray.addElement(new StringValue((String)Array.get(object, i)));
            return jsonArray;
         }
         case ARRAY_OBJECT : {
            JsonValue jsonArray = new ArrayValue();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++)
            {
               Object el = Array.get(object, i);
               if (JsonUtils.getType(el) != null)
                  jsonArray.addElement(createJsonValue(el));
               else
                  jsonArray.addElement(createJsonObject(el));
            }

            return jsonArray;
         }
         case COLLECTION : {
            JsonValue jsonArray = new ArrayValue();
            List<Object> list = new ArrayList<Object>((Collection<?>)object);
            for (Object o : list)
            {
               if (JsonUtils.getType(o) != null)
                  jsonArray.addElement(createJsonValue(o));
               else
                  jsonArray.addElement(createJsonObject(o));
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
                  jsonObject.addElement(k, createJsonValue(o));
               else
                  jsonObject.addElement(k, createJsonObject(o));
            }

            return jsonObject;
         default :
            // Must not be here!
            return null;
      }

   }

   /**
    * Check fields in class which marked as 'transient'. Transient fields will
    * be not serialized in JSON representation.
    *
    * @param clazz the class.
    * @return list of fields which must be skiped.
    */
   private static List<String> getTransientFields(Class<?> clazz)
   {
      List<String> l = new ArrayList<String>();
      Field[] fields = clazz.getDeclaredFields();
      for (Field f : fields)
      {
         if (Modifier.isTransient(f.getModifiers()))
         {
            l.add(f.getName());
         }
      }
      return l;
   }

}
