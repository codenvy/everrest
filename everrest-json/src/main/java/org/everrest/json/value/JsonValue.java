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
package org.everrest.json.value;

import org.everrest.json.JsonException;
import org.everrest.json.JsonWriter;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: JsonValue.java 34417 2009-07-23 14:42:56Z dkatayev $
 */
public abstract class JsonValue
{

   //  set defaults for specific types
   // It will be overridden.

   /**
    * @return true if value is 'object', false otherwise. Should be overridden.
    */
   public boolean isObject()
   {
      return false;
   }

   /**
    * @return true if value is 'array', false otherwise. Should be overridden.
    */
   public boolean isArray()
   {
      return false;
   }

   /**
    * @return true if value is 'numeric', false otherwise. Should be overridden.
    */
   public boolean isNumeric()
   {
      return false;
   }

   /**
    * @return true if value is 'long', false otherwise. Should be overridden.
    */
   public boolean isLong()
   {
      return false;
   }

   /**
    * @return true if value is 'double', false otherwise. Should be overridden.
    */
   public boolean isDouble()
   {
      return false;
   }

   /**
    * @return true if value is 'String', false otherwise. Should be overridden.
    */
   public boolean isString()
   {
      return false;
   }

   /**
    * @return true if value is 'boolean', false otherwise. Should be overridden.
    */
   public boolean isBoolean()
   {
      return false;
   }

   /**
    * @return true if value is 'null', false otherwise. Should be overridden.
    */
   public boolean isNull()
   {
      return false;
   }

   /**
    * Add child value. This method must be used if isArray() gives true.
    * 
    * @param child the child value.
    */
   public void addElement(JsonValue child)
   {
      throw new UnsupportedOperationException("This type of JsonValue can't have child.");
   }

   /**
    * Add child value. This method must be used if isObject() gives true.
    * 
    * @param key the key.
    * @param child the child value.
    */
   public void addElement(String key, JsonValue child)
   {
      throw new UnsupportedOperationException("This type of JsonValue can't have child.");
   }

   /**
    * Get all element of this value.
    * 
    * @return Iterator.
    */
   public Iterator<JsonValue> getElements()
   {
      return new ArrayList<JsonValue>().iterator();
   }

   /**
    * Get all keys for access values.
    * 
    * @return Iterator.
    */
   public Iterator<String> getKeys()
   {
      return new ArrayList<String>().iterator();
   }

   /**
    * Get value by key.
    * 
    * @param key the key.
    * @return JsonVAlue with specified key.
    */
   public JsonValue getElement(String key)
   {
      return null;
   }

   /**
    * @return number of child elements.
    */
   public int size()
   {
      return 0;
   }

   // Prepared values of know type.
   // It will be overridden.

   /**
    * @return string value. Should be overridden.
    */
   public String getStringValue()
   {
      return null;
   }

   /**
    * @return boolean value. Should be overridden.
    */
   public boolean getBooleanValue()
   {
      return false;
   }

   /**
    * @return Number value. Should be overridden.
    */
   public Number getNumberValue()
   {
      return Integer.valueOf(getIntValue());
   }

   /**
    * @return byte value. Should be overridden.
    */
   public byte getByteValue()
   {
      return 0;
   }

   /**
    * @return short Value. Should be overridden.
    */
   public short getShortValue()
   {
      return 0;
   }

   /**
    * @return int value. Should be overridden.
    */
   public int getIntValue()
   {
      return 0;
   }

   /**
    * @return long value. Should be overridden.
    */
   public long getLongValue()
   {
      return 0L;
   }

   /**
    * @return float value. Should be overridden.
    */
   public float getFloatValue()
   {
      return 0.0F;
   }

   /**
    * @return double value. Should be overridden.
    */
   public double getDoubleValue()
   {
      return 0.0;
   }

   //  must be implemented

   /**
    * {@inheritDoc}
    */
   @Override
   public abstract String toString();

   /**
    * Write value in given writer.
    * 
    * @param writer Writer.
    * @throws JsonException if any errors occurs.
    */
   public abstract void writeTo(JsonWriter writer) throws JsonException;

}
