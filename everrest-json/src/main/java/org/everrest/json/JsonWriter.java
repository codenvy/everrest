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
package org.everrest.json;

import org.everrest.json.impl.JsonException;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: JsonWriter.java 34417 2009-07-23 14:42:56Z dkatayev $
 */
public interface JsonWriter
{

   /**
    * Write the start of JSON object '{'.
    * 
    * @throws JsonException if any errors, include i/o errors occurs.
    */
   void writeStartObject() throws JsonException;

   /**
    * Write the end of JSON object '}'.
    * 
    * @throws JsonException if any errors, include i/o errors occurs.
    */
   void writeEndObject() throws JsonException;

   /**
    * Write the start of JSON array '['.
    * 
    * @throws JsonException if any errors, include i/o errors occurs.
    */
   void writeStartArray() throws JsonException;

   /**
    * Write the key. After key will go the value. In this way data represented
    * in JSON object.
    * 
    * @param key the key.
    * @throws JsonException if any errors, include i/o errors occurs.
    */
   void writeKey(String key) throws JsonException;

   /**
    * Write the end of JSON array ']'.
    * 
    * @throws JsonException if any errors, include i/o errors occurs.
    */
   void writeEndArray() throws JsonException;

   /**
    * Write the String to stream.
    * 
    * @param value the String.
    * @throws JsonException if any errors, include i/o errors occurs.
    */
   void writeString(String value) throws JsonException;

   /**
    * Write the value of long type to stream.
    * 
    * @param value the value of long type.
    * @throws JsonException if any errors, include i/o errors occurs.
    */
   void writeValue(long value) throws JsonException;

   /**
    * Write the value of double type to stream.
    * 
    * @param value the value of double type.
    * @throws JsonException if any errors, include i/o errors occurs.
    */
   void writeValue(double value) throws JsonException;

   /**
    * Write the value of boolean type to stream.
    * 
    * @param value the value of boolean type.
    * @throws JsonException if any errors, include i/o errors occurs.
    */
   void writeValue(boolean value) throws JsonException;

   /**
    * Write the null data to stream.
    * 
    * @throws JsonException if any errors, include i/o errors occurs.
    */
   void writeNull() throws JsonException;

   /**
    * Flush output writer.
    * 
    * @throws JsonException if any errors, include i/o errors occurs.
    */
   void flush() throws JsonException;

   /**
    * Close output writer.
    * 
    * @throws JsonException if any errors, include i/o errors occurs.
    */
   void close() throws JsonException;
}
